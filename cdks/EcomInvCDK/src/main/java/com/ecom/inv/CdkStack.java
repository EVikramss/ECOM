package com.ecom.inv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.HttpNoneAuthorizer;
import software.amazon.awscdk.services.ec2.IInterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpointAttributes;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroupProps;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.ClusterInstance;
import software.amazon.awscdk.services.rds.DBClusterStorageType;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.rds.DatabaseProxy;
import software.amazon.awscdk.services.rds.DatabaseProxyOptions;
import software.amazon.awscdk.services.rds.IClusterEngine;
import software.amazon.awscdk.services.rds.IClusterInstance;
import software.amazon.awscdk.services.rds.ParameterGroup;
import software.amazon.awscdk.services.rds.ParameterGroupProps;
import software.amazon.awscdk.services.rds.ProvisionedClusterInstanceProps;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

public class CdkStack extends Stack {

	private Stack stack;
	private String stackName;
	private Construct scope;
	private Properties additionalProperties;

	private IVpc vpc;
	private IInterfaceVpcEndpoint endpoint;
	private DatabaseProxy dbProxy;

	ISecret dbSecret;
	ISecurityGroup dbprxysg;
	ISecurityGroup smepsg;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
		this.scope = scope;
		stack = this;
		this.stackName = id;
		this.additionalProperties = additionalProperties;

		int dbreadRplCnt = 0;
		String readReplicaCount = System.getenv("dbreadRplCnt");
		if (readReplicaCount != null && readReplicaCount.trim().length() > 0)
			dbreadRplCnt = Integer.parseInt(readReplicaCount);

		lookupNetwork();
		lookupEndpoints();
		createAuroraPostgresDB(dbreadRplCnt);
		checkAndSetupLambdaBackedAPIs();
		setupOutputVariables();
	}

	private void checkAndSetupLambdaBackedAPIs() {
		String baseDir = "/home/ec2-user/deploymentWorkspace/modules/InventoryModule/";
		setupLambdaBackedAPI("AvailabilityOp", baseDir, false, false);
		setupLambdaBackedAPI("SupplyDemandOp", baseDir, false, false);
	}

	private void setupLambda(String lambdaName, String baseDir) {

		String relLambdaName = stackName + lambdaName;

		SecurityGroup sg = new SecurityGroup(stack, relLambdaName + "SecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());
		dbprxysg.addIngressRule(sg, Port.POSTGRES);
		smepsg.addIngressRule(sg, Port.HTTPS);
		sg.addEgressRule(dbprxysg, Port.POSTGRES);
		sg.addEgressRule(smepsg, Port.HTTPS);

		Function function = Function.Builder.create(this, relLambdaName).runtime(Runtime.PYTHON_3_11)
				.functionName(relLambdaName).code(Code.fromAsset(baseDir + lambdaName + ".zip"))
				.handler("lambda_function.lambda_handler").securityGroups(Arrays.asList(sg)).vpc(vpc)
				.environment(Map.of("SECRET_ARN", dbSecret.getSecretArn(), "DBPRX_EP", dbProxy.getEndpoint())).build();

		// need to update lambda function to read
		dbSecret.grantRead(function);
	}

	private void setupLambdaBackedAPI(String lambdaName, String baseDir, boolean deployAPI, boolean enableFunctionURL) {

		String relLambdaName = stackName + lambdaName;

		// create sg for lambda
		SecurityGroup sg = new SecurityGroup(stack, relLambdaName + "SecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());

		// add ingress and egress rules
		dbprxysg.addIngressRule(sg, Port.POSTGRES);
		smepsg.addIngressRule(sg, Port.HTTPS);
		sg.addEgressRule(dbprxysg, Port.POSTGRES);
		sg.addEgressRule(smepsg, Port.HTTPS);

		// create function builder
		software.amazon.awscdk.services.lambda.Function.Builder functionBuilder = Function.Builder
				.create(this, relLambdaName).runtime(Runtime.PYTHON_3_11).functionName(relLambdaName)
				.code(Code.fromAsset(baseDir + lambdaName + ".zip")).handler("lambda_function.lambda_handler")
				.securityGroups(Arrays.asList(sg)).vpc(vpc)
				.environment(Map.of("SECRET_ARN", dbSecret.getSecretArn(), "DBPRX_EP", dbProxy.getEndpoint()));

		// update reservedConcurrecy if greater than 0
		Integer reservedConcurrecy = (Integer) additionalProperties.get(lambdaName + "ResvConcurrency");
		if (reservedConcurrecy > 0) {
			functionBuilder = functionBuilder.reservedConcurrentExecutions(reservedConcurrecy);
		}
		Function function = functionBuilder.build();

		// update provisionedConcurrecy if greater than 0
		Integer provisionedConcurrecy = (Integer) additionalProperties.get(lambdaName + "ProvConcurrency");
		if (provisionedConcurrecy > 0) {
			Alias alias = Alias.Builder.create(this, relLambdaName + "Alias").aliasName(relLambdaName + "Alias")
					.provisionedConcurrentExecutions(provisionedConcurrecy).version(function.getCurrentVersion())
					.build();
		}

		// need to update lambda function to read
		dbSecret.grantRead(function);

		if (enableFunctionURL) {
			// output lambda https function url
			FunctionUrl functionUrl = function
					.addFunctionUrl(FunctionUrlOptions.builder().authType(FunctionUrlAuthType.NONE).build());
			CfnOutput.Builder.create(this, lambdaName + "URL").value(functionUrl.getUrl()).build();
		}

		if (deployAPI) {
			String pathName = lambdaName.substring(0, 1).toLowerCase() + lambdaName.substring(1);

			// when invoking from api gateway retrieve payload as json.loads(event["body"])
			HttpApi api = new HttpApi(this, relLambdaName + "API",
					HttpApiProps.builder().apiName(relLambdaName + "API").build());
			List<HttpMethod> mlist = new ArrayList<HttpMethod>();
			mlist.add(HttpMethod.POST);
			api.addRoutes(AddRoutesOptions.builder().path("/" + pathName).methods(mlist)
					.integration(HttpLambdaIntegration.Builder.create(lambdaName + "Integ", function).build())
					.authorizer(new HttpNoneAuthorizer()).build());
		}
	}

	private void createAuroraPostgresDB(int dbreadRplCnt) {
		// create DB - start

		// aurora properties
		AuroraPostgresClusterEngineProps dbProps = AuroraPostgresClusterEngineProps.builder()
				.version(AuroraPostgresEngineVersion.VER_16_3).build();
		IClusterEngine engine = DatabaseClusterEngine.auroraPostgres(dbProps);
		ParameterGroup pg = new ParameterGroup(stack, "aurorapg", ParameterGroupProps.builder().engine(engine).build());

		// writer instance
		ProvisionedClusterInstanceProps instanceProps = ProvisionedClusterInstanceProps.builder()
				.instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM)).parameterGroup(pg)
				.publiclyAccessible(false).enablePerformanceInsights(false).build();
		IClusterInstance writer = ClusterInstance.provisioned("writer", instanceProps);

		// create cluster
		ISecurityGroup dbsg = new SecurityGroup(stack, "auroraSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).build());
		DatabaseCluster dbCluster = DatabaseCluster.Builder.create(stack, "auroradb").engine(engine).parameterGroup(pg)
				.clusterIdentifier("ecominvdb").writer(writer).securityGroups(Arrays.asList(dbsg)).vpc(vpc)
				.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
				.storageType(DBClusterStorageType.AURORA).build();

		// setup db password rotation
		// keep id short to prevent rotation secret from exceeding 64 characters
		dbSecret = dbCluster.getSecret();
		/*
		 * dbSecret.addRotationSchedule("rs",
		 * RotationScheduleOptions.builder().hostedRotation(HostedRotation.
		 * postgreSqlSingleUser())
		 * .automaticallyAfter(Duration.days(30)).rotateImmediatelyOnUpdate(true).build(
		 * ));
		 */

		// create rds proxy
		dbprxysg = new SecurityGroup(stack, "rdsproxySecurityGroup", SecurityGroupProps.builder().vpc(vpc).build());
		dbProxy = dbCluster.addProxy("RDSProxy", DatabaseProxyOptions.builder().securityGroups(List.of(dbprxysg))
				.secrets(Arrays.asList(dbCluster.getSecret())).vpc(vpc).iamAuth(false).requireTls(true).build());
		// create DB - end

		// add permissions for RunDDLQuery Lambda
		IRole lambdaRole = Role.fromRoleArn(this, "ImportedLambdaRole", System.getenv("RunDDLFUNCRLID"));
		ISecurityGroup sg = SecurityGroup.fromLookupById(this, "RunDDLQuerySG", System.getenv("RunDDLSGID"));
		dbSecret.grantRead(lambdaRole);
		dbsg.addIngressRule(sg, Port.POSTGRES);
		smepsg.addIngressRule(sg, Port.HTTPS);
		sg.addEgressRule(dbsg, Port.POSTGRES);
		sg.addEgressRule(smepsg, Port.HTTPS);
	}

	private void lookupNetwork() {
		// fetch ECOM vpc if already existing, otherwise create it
		String vpcID = System.getenv("VPCID");
		vpc = Vpc.fromLookup(this, vpcID, VpcLookupOptions.builder().vpcId(vpcID).build());
	}

	private void lookupEndpoints() {
		String secretsManagerEndpointID = System.getenv("SMEPID");
		endpoint = InterfaceVpcEndpoint.fromInterfaceVpcEndpointAttributes(this, "secretsManagerEP",
				InterfaceVpcEndpointAttributes.builder().vpcEndpointId(secretsManagerEndpointID).port(443).build());

		String secretsManagerEndpointSGID = System.getenv("SMSGID");
		smepsg = SecurityGroup.fromLookupById(this, "secretsManagerEPSG", secretsManagerEndpointSGID);
	}

	/**
	 * Output values from CDK to be used later.
	 */
	private void setupOutputVariables() {
		CfnOutput.Builder.create(this, "DBSECARN").value(dbSecret.getSecretArn()).build();
		CfnOutput.Builder.create(this, "SSMEPID").value(endpoint.getVpcEndpointId()).build();
		CfnOutput.Builder.create(this, "DBPRXYSGID").value(dbprxysg.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "DBPRXYEP").value(dbProxy.getEndpoint()).build();
	}
}
