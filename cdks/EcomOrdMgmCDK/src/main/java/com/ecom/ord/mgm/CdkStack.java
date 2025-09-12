package com.ecom.ord.mgm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.HttpNoneAuthorizer;
import software.amazon.awscdk.services.appsync.AuthorizationConfig;
import software.amazon.awscdk.services.appsync.AuthorizationMode;
import software.amazon.awscdk.services.appsync.AuthorizationType;
import software.amazon.awscdk.services.appsync.BaseResolverProps;
import software.amazon.awscdk.services.appsync.Definition;
import software.amazon.awscdk.services.appsync.FunctionRuntime;
import software.amazon.awscdk.services.appsync.GraphqlApi;
import software.amazon.awscdk.services.appsync.LambdaDataSource;
import software.amazon.awscdk.services.appsync.UserPoolConfig;
import software.amazon.awscdk.services.appsync.UserPoolDefaultAction;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.CfnUserPoolClient;
import software.amazon.awscdk.services.cognito.CognitoDomainOptions;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
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
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterAttributes;
import software.amazon.awscdk.services.ecs.ContainerDefinition;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.Ec2Service;
import software.amazon.awscdk.services.ecs.Ec2TaskDefinition;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.NetworkMode;
import software.amazon.awscdk.services.ecs.PortMapping;
import software.amazon.awscdk.services.ecs.Secret;
import software.amazon.awscdk.services.iam.AnyPrincipal;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
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
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

public class CdkStack extends Stack {

	private Stack stack;
	private String stackName;
	private Properties additionalProperties;

	private IVpc vpc;
	private IInterfaceVpcEndpoint endpoint;
	private DatabaseProxy dbProxy;
	private DatabaseCluster dbCluster;

	ISecret dbSecret;
	ISecurityGroup dbprxysg;
	ISecurityGroup smepsg;
	Role amplifyRole;

	private UserPoolDomain userPoolDomain;
	private UserPoolClient userPoolClient;
	private UserPool userPool;

	private CfnApp amplifyApp;
	private GraphqlApi appSyncAPI;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
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
		setupECSJobs();

		// record output variables
		setupOutputVariables();
	}

	private void checkAndSetupLambdaBackedAPIs() {
		String baseDir = "/home/ec2-user/deploymentWorkspace2/modules/OrderManagementModule/";
		setupOrderConsole(baseDir);
	}

	private void setupOrderConsole(String baseDir) {
		baseDir = baseDir + "/Console/";

		// setup lambda between app sync and rds
		Function appSyncDatasource = setupLambda("dbEndpoint", baseDir);

		// setup cognito for both appsync and ui auth
		setupCognito();

		// create app sync api
		appSyncAPI = setupAppSync(appSyncDatasource, baseDir);

		// create ui on amplify
		setupAmplify();
	}

	private void setupAmplify() {

		// create app
		amplifyRole = Role.Builder.create(this, "amplifyRole").assumedBy(new ServicePrincipal("amplify.amazonaws.com"))
				.build();
		amplifyApp = CfnApp.Builder.create(this, "ordMgmApp").name("ordMgmApp").iamServiceRole(amplifyRole.getRoleArn())
				.build();

		// S3 Source Configuration (prefix-based)
		CfnBranch amplifyBranch = CfnBranch.Builder.create(this, "ordMgmBranch").appId(amplifyApp.getAttrAppId())
				.branchName("main").build();

		// get s3 code bucket
		String bucketArn = System.getenv("ECOMBKTARN");
		IBucket bucket = Bucket.fromBucketArn(this, bucketArn, bucketArn);
		bucket.grantRead(amplifyRole);
		// addBucketPolicyForAmplify(bucket, amplifyApp);
	}

	/**
	 * Apart from IAM role, amplify access needs S3 bucket policy to be set as well
	 * 
	 * @param bucket
	 * @param amplifyApp
	 */
	private void addBucketPolicyForAmplify(IBucket bucket, CfnApp amplifyApp) {

		String amplifyAppArn = amplifyApp.getAttrArn();
		String accountId = additionalProperties.getProperty("accountID");

		PolicyStatement listBucketStatement = PolicyStatement.Builder.create().effect(Effect.ALLOW)
				.principals(List.of(new ServicePrincipal("amplify.amazonaws.com"))).actions(List.of("s3:ListBucket"))
				.resources(List.of(bucket.getBucketArn())).conditions(Map.of("StringEquals", Map.of("aws:SourceArn",
						amplifyAppArn, "aws:SourceAccount", accountId, "s3:prefix", "ordmgm/ui/")))
				.build();

		PolicyStatement getObjectStatement = PolicyStatement.Builder.create().effect(Effect.ALLOW)
				.principals(List.of(new ServicePrincipal("amplify.amazonaws.com"))).actions(List.of("s3:GetObject"))
				.resources(List.of(bucket.getBucketArn() + "/ordmgm/ui/*"))
				.conditions(
						Map.of("StringEquals", Map.of("aws:SourceArn", amplifyAppArn, "aws:SourceAccount", accountId)))
				.build();

		PolicyStatement denyUnsecureTransport = PolicyStatement.Builder.create().effect(Effect.DENY)
				.principals(List.of(new AnyPrincipal())).actions(List.of("s3:*"))
				.resources(List.of(bucket.getBucketArn() + "/*"))
				.conditions(Map.of("Bool", Map.of("aws:SecureTransport", "false"))).build();

		// Attach policy to bucket
		bucket.addToResourcePolicy(listBucketStatement);
		bucket.addToResourcePolicy(getObjectStatement);
		bucket.addToResourcePolicy(denyUnsecureTransport);
	}

	private GraphqlApi setupAppSync(Function appSyncDatasource, String baseDir) {

		GraphqlApi api = GraphqlApi.Builder.create(this, "ordMgmAPI").name("ordMgmAPI")
				.authorizationConfig(AuthorizationConfig.builder()
						.defaultAuthorization(AuthorizationMode.builder().authorizationType(AuthorizationType.USER_POOL)
								.userPoolConfig(UserPoolConfig.builder().userPool(userPool)
										.defaultAction(UserPoolDefaultAction.ALLOW).build())
								.build())
						.build())
				.definition(Definition.fromFile(baseDir + "/schemas/ordMgm")).build();

		// setup lambda as datasource
		LambdaDataSource lambdaDs = api.addLambdaDataSource("LambdaDataSource", appSyncDatasource);
		createResolver("getOrder", baseDir + "/resolvers/resolver.js", "Query", lambdaDs);

		return api;
	}

	private void createResolver(String fieldName, String codePath, String typeName, LambdaDataSource lambdaDs) {
		lambdaDs.createResolver(fieldName + typeName + "Resolver",
				BaseResolverProps.builder().runtime(FunctionRuntime.JS_1_0_0).typeName(typeName).fieldName(fieldName)
						.code(software.amazon.awscdk.services.appsync.Code.fromAsset(codePath)).build());
	}

	private void setupCognito() {
		// create cognito user pool
		userPool = UserPool.Builder.create(this, "OrderConsoleUserPool").selfSignUpEnabled(false)
				.signInAliases(SignInAliases.builder().username(true).build()).build();

		// create cognito domain
		userPoolDomain = UserPoolDomain.Builder.create(this, "OrderConsolePoolDomain").userPool(userPool)
				.cognitoDomain(CognitoDomainOptions.builder().domainPrefix("orderconsoleaccess").build()).build();

		// create cognito client - without client secret
		userPoolClient = UserPoolClient.Builder.create(this, "OrderConsoleClient").userPool(userPool)
				.generateSecret(false).authFlows(AuthFlow.builder().userPassword(true).build())
				.accessTokenValidity(Duration.hours(1))
				.oAuth(OAuthSettings.builder().flows(OAuthFlows.builder().authorizationCodeGrant(true).build())
						.scopes(List.of(OAuthScope.EMAIL, OAuthScope.OPENID, OAuthScope.PHONE))
						.callbackUrls(List.of("https://")).build())
				.build();

		CfnUserPoolClient cfnClient = (CfnUserPoolClient) userPoolClient.getNode().getDefaultChild();
		cfnClient.addPropertyOverride("RefreshTokenValidity", 1);
		cfnClient.addPropertyOverride("SupportedIdentityProviders", List.of("COGNITO"));
	}

	private Function setupLambda(String lambdaName, String baseDir) {

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

		return function;
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
		dbCluster = DatabaseCluster.Builder.create(stack, "auroradb").engine(engine).parameterGroup(pg)
				.clusterIdentifier("ecomordmgmdb").writer(writer).securityGroups(Arrays.asList(dbsg)).vpc(vpc)
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

	private void setupECSJobs() {
		String baseDir = "/home/ec2-user/deploymentWorkspace2/modules/OrderManagementModule/";
		IRole asgRole = Role.fromRoleArn(this, "ECSASGROLE", System.getenv("ECSASGROLE"));
		setupECSJob(baseDir, "CreateOrder", asgRole);
		setupECSJob(baseDir, "ScheduleOrder", asgRole);
		setupECSJob(baseDir, "ShipOrder", asgRole);
		setupECSJob(baseDir, "GetData", asgRole);
	}

	private void setupECSJob(String baseDir, String jobName, IRole asgRole) {
		// create roles for ecs job
		Role executionRole = Role.Builder.create(this, jobName + "TskExecRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
				.managedPolicies(
						List.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonECSTaskExecutionRolePolicy"),
								ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite")))
				.build();

		Role taskRole = Role.Builder.create(this, jobName + "TskRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
				.managedPolicies(List.of(ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
						ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSDataFullAccess")))
				.build();

		// create task definition along with log group
		Ec2TaskDefinition taskDefinition = Ec2TaskDefinition.Builder.create(this, jobName + "TskDef")
				.networkMode(NetworkMode.AWS_VPC).executionRole(executionRole).taskRole(taskRole).build();

		// create log group
		LogGroup logGroup = LogGroup.Builder.create(this, jobName + "LogGroup").logGroupName("/ecs/" + jobName)
				.retention(RetentionDays.ONE_DAY).build();
		logGroup.grantWrite(asgRole);

		// define container along with rds secret
		String imageURI = System.getenv("ECRREPO") + ":" + jobName.toLowerCase();
		ContainerDefinition container = taskDefinition.addContainer(jobName + "Container", ContainerDefinitionOptions
				.builder().image(ContainerImage.fromRegistry(imageURI))
				.memoryLimitMiB(Integer.parseInt(System.getenv(jobName + "MEM"))).cpu(Integer.parseInt(System.getenv(jobName + "CPU")))
				.portMappings(List.of(PortMapping.builder().containerPort(8080).build()))
				.logging(LogDriver.awsLogs(AwsLogDriverProps.builder().logGroup(logGroup).streamPrefix("ecom").build()))
				.secrets(Map.of("SECRET", Secret.fromSecretsManager(dbSecret)))
				.environment(Map.of("DBPRX_EP", dbProxy.getEndpoint(), "INVAVLURL", System.getenv("INVAVLURL")))
				.build());

		// fetch cluster & create service for task
		ICluster cluster = Cluster.fromClusterAttributes(this, "ECOMECSCluster",
				ClusterAttributes.builder().clusterName(System.getenv("ECSARN")).vpc(vpc).build());
		Ec2Service service = Ec2Service.Builder.create(this, jobName + "Service").cluster(cluster)
				.serviceName(jobName + "Service")
				.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
				.taskDefinition(taskDefinition).desiredCount(1).build();
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
		// no underscores in output name
		CfnOutput.Builder.create(this, "DBSECARN").value(dbSecret.getSecretArn()).build();
		CfnOutput.Builder.create(this, "SSMEPID").value(endpoint.getVpcEndpointId()).build();
		CfnOutput.Builder.create(this, "DBPRXYSGID").value(dbprxysg.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "DBPRXYEP").value(dbProxy.getEndpoint()).build();
		CfnOutput.Builder.create(this, "AMPEP").value(amplifyApp.getAttrAppId()).build();
		CfnOutput.Builder.create(this, "ORDMGMUSERPOOLID").value(userPool.getUserPoolId()).build();
		CfnOutput.Builder.create(this, "ORDMGMCLIENTID").value(userPoolClient.getUserPoolClientId()).build();
		CfnOutput.Builder.create(this, "APPSYNCAPIID").value(appSyncAPI.getApiId()).build();
		CfnOutput.Builder.create(this, "AMPLIFYROLEARN").value(amplifyRole.getRoleArn()).build();
	}
}