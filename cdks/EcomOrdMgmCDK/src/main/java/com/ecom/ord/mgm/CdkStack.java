package com.ecom.ord.mgm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpUserPoolAuthorizer;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpAlbIntegration;
import software.amazon.awscdk.services.amplify.CfnApp;
import software.amazon.awscdk.services.amplify.CfnBranch;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod;
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.VpcLink;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.CfnUserPoolClient;
import software.amazon.awscdk.services.cognito.CognitoDomainOptions;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
import software.amazon.awscdk.services.ec2.IInterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.ISubnet;
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
import software.amazon.awscdk.services.ecs.CloudMapOptions;
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
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancerAttributes;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationTargetGroup;
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseApplicationListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.TargetType;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.ClusterInstance;
import software.amazon.awscdk.services.rds.DBClusterStorageType;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.rds.IClusterEngine;
import software.amazon.awscdk.services.rds.IClusterInstance;
import software.amazon.awscdk.services.rds.ParameterGroup;
import software.amazon.awscdk.services.rds.ParameterGroupProps;
import software.amazon.awscdk.services.rds.ProvisionedClusterInstanceProps;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;
import software.amazon.awscdk.services.servicediscovery.IPrivateDnsNamespace;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespaceAttributes;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class CdkStack extends Stack {

	private Stack stack;
	private String stackName;
	private Properties additionalProperties;

	private IVpc vpc;
	private IInterfaceVpcEndpoint endpoint;
	private DatabaseCluster dbCluster;

	private ISecret dbSecret;
	private ISecret invdbSecret;
	private ISecurityGroup smepsg;
	private Role amplifyRole;
	private ISecurityGroup asgsg;
	private ISecurityGroup dbsg;

	private UserPoolDomain userPoolDomain;
	private UserPoolClient userPoolClient;
	private IUserPool userPool;

	private Queue createOrderQ;

	private CfnApp amplifyApp;

	private Ec2Service getDataService;
	private HttpApi ecsHTTPApi;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
		stack = this;
		this.stackName = id;
		this.additionalProperties = additionalProperties;

		int dbreadRplCnt = 0;
		String readReplicaCount = System.getenv("dbreadRplCnt");
		if (readReplicaCount != null && readReplicaCount.trim().length() > 0)
			dbreadRplCnt = Integer.parseInt(readReplicaCount);

		String baseDir = "/home/ec2-user/deploymentWorkspace2/modules/OrderManagementModule/";

		lookupNetwork();
		lookupEndpoints();
		lookupSecrets();

		setupCognito();
		createAuroraPostgresDB(dbreadRplCnt);
		setupSQS();
		setupOrderConsole(baseDir);
		setupECSJobs();
		exposeECSWithHttpApi();

		// record output variables
		setupOutputVariables();
	}

	private void setupSQS() {
		createOrderQ = Queue.Builder.create(this, "CreateOrderQ").queueName("CreateOrderQ")
				.retentionPeriod(Duration.days(1)).build();
	}

	private void setupOrderConsole(String baseDir) {
		baseDir = baseDir + "/Console/";

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
		dbsg = new SecurityGroup(stack, "auroraSecurityGroup", SecurityGroupProps.builder().vpc(vpc).build());
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
		// lookup needed resources
		IRole asgRole = Role.fromRoleArn(this, "ECSASGROLE", System.getenv("ECSASGROLE"));
		asgsg = SecurityGroup.fromLookupById(this, "ECSASGSG", System.getenv("ECSASGSG"));
		ICluster cluster = Cluster.fromClusterAttributes(this, "ECOMECSCluster",
				ClusterAttributes.builder().clusterName(System.getenv("ECSARN")).vpc(vpc).build());
		IPrivateDnsNamespace pdn = PrivateDnsNamespace.fromPrivateDnsNamespaceAttributes(this, "EcomNamespace",
				PrivateDnsNamespaceAttributes.builder().namespaceArn(System.getenv("ECSNMSPARN"))
						.namespaceId(System.getenv("ECSNMSPID")).namespaceName("ecom.internal").build());

		// grant consume msg from q
		createOrderQ.grantConsumeMessages(asgRole);

		// add asg role to db sg
		smepsg.addIngressRule(asgsg, Port.HTTPS);
		asgsg.addEgressRule(smepsg, Port.HTTPS);
		dbsg.addIngressRule(asgsg, Port.POSTGRES);
		asgsg.addEgressRule(dbsg, Port.POSTGRES);

		// add access to inv db if available
		String invdbsgStr = System.getenv("INVDBSG");
		if (invdbsgStr != null && invdbsgStr.trim().length() > 0) {
			try {
				ISecurityGroup invdbsg = SecurityGroup.fromLookupById(this, "ECSASGSG", invdbsgStr);
				invdbsg.addIngressRule(asgsg, Port.POSTGRES);
				asgsg.addEgressRule(invdbsg, Port.POSTGRES);
			} catch (Exception e) {

			}
		}

		String baseDir = "/home/ec2-user/deploymentWorkspace2/modules/OrderManagementModule/";
		setupECSJob(baseDir, "CreateOrder", asgRole, asgsg, cluster, pdn);
		setupECSJob(baseDir, "ScheduleOrder", asgRole, asgsg, cluster, pdn);
		setupECSJob(baseDir, "ShipOrder", asgRole, asgsg, cluster, pdn);
		getDataService = setupECSJob(baseDir, "GetData", asgRole, asgsg, cluster, pdn);
	}

	private Ec2Service setupECSJob(String baseDir, String jobName, IRole asgRole, ISecurityGroup asgsg,
			ICluster cluster, IPrivateDnsNamespace pdn) {
		// create roles for ecs job
		Role executionRole = Role.Builder.create(this, jobName + "TskExecRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
				.managedPolicies(
						List.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonECSTaskExecutionRolePolicy"),
								ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite")))
				.build();

		List<IManagedPolicy> taskPolicies = null;
		if ("CreateOrder".equals(jobName)) {
			taskPolicies = List.of(ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
					ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSDataFullAccess"),
					ManagedPolicy.fromAwsManagedPolicyName("AmazonSQSFullAccess"));
		} else if ("ScheduleOrder".equals(jobName)) {
			taskPolicies = List.of(ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
					ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSDataFullAccess"),
					ManagedPolicy.fromAwsManagedPolicyName("AmazonSQSFullAccess"),
					ManagedPolicy.fromAwsManagedPolicyName("AWSLambda_FullAccess"));
		} else {
			taskPolicies = List.of(ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
					ManagedPolicy.fromAwsManagedPolicyName("AmazonRDSDataFullAccess"));
		}
		Role taskRole = Role.Builder.create(this, jobName + "TskRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com")).managedPolicies(taskPolicies).build();

		// create task definition along with log group
		Ec2TaskDefinition taskDefinition = Ec2TaskDefinition.Builder.create(this, jobName + "TskDef")
				.networkMode(NetworkMode.AWS_VPC).executionRole(executionRole).taskRole(taskRole).build();

		// create log group
		LogGroup logGroup = LogGroup.Builder.create(this, jobName + "LogGroup").logGroupName("/ecs/" + jobName)
				.retention(RetentionDays.ONE_DAY).build();
		logGroup.grantWrite(asgRole);

		Map<String, Secret> secrets;
		Map<String, String> envVariables;
		if ("CreateOrder".equals(jobName)) {
			envVariables = Map.of("DBPRX_EP", dbCluster.getClusterEndpoint().getHostname(), "CreateOrderQURL",
					createOrderQ.getQueueUrl());
			secrets = Map.of("SECRET", Secret.fromSecretsManager(dbSecret));
		} else if ("GetData".equals(jobName)) {
			envVariables = Map.of("DBPRX_EP", dbCluster.getClusterEndpoint().getHostname(), "INVDB_EP",
					System.getenv("INVDBEP"));
			if (invdbSecret != null) {
				secrets = Map.of("SECRET", Secret.fromSecretsManager(dbSecret), "INVDBSECRET",
						Secret.fromSecretsManager(invdbSecret));
			} else {
				secrets = Map.of("SECRET", Secret.fromSecretsManager(dbSecret));
			}
		} else {
			envVariables = Map.of("DBPRX_EP", dbCluster.getClusterEndpoint().getHostname());
			secrets = Map.of("SECRET", Secret.fromSecretsManager(dbSecret));
		}

		// define container along with rds secret
		String imageURI = System.getenv("ECRREPO") + ":" + jobName.toLowerCase();
		ContainerDefinition container = taskDefinition.addContainer(jobName + "Container", ContainerDefinitionOptions
				.builder().image(ContainerImage.fromRegistry(imageURI))
				.memoryLimitMiB(Integer.parseInt(System.getenv(jobName + "MEM")))
				.cpu(Integer.parseInt(System.getenv(jobName + "CPU")))
				.portMappings(List.of(PortMapping.builder().containerPort(8080).build()))
				.logging(LogDriver.awsLogs(AwsLogDriverProps.builder().logGroup(logGroup).streamPrefix("ecom").build()))
				.secrets(secrets).environment(envVariables).build());

		// create service for task - use same sg as asg
		// expose service with cloud map and dns A records
		// set count as 0 and start service after initDB
		Ec2Service service = Ec2Service.Builder.create(this, jobName + "Service").cluster(cluster)
				.serviceName(jobName + "Service").securityGroups(List.of(asgsg))
				.cloudMapOptions(CloudMapOptions.builder().cloudMapNamespace(pdn).name(jobName.toLowerCase())
						.dnsRecordType(DnsRecordType.A).build())
				.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
				.taskDefinition(taskDefinition).desiredCount(0).build();

		service.getNode().addDependency(dbCluster);

		return service;
	}

	private void exposeECSWithHttpApi() {
		// fetch alb created in common stack
		String albARNStr = System.getenv("ALBARN");
		String albSGStr = System.getenv("ALBSG");

		ISecurityGroup albSecurityGroup = SecurityGroup.fromLookupById(this, albSGStr, albSGStr);
		IApplicationLoadBalancer alb = ApplicationLoadBalancer.fromApplicationLoadBalancerAttributes(this, stackName,
				ApplicationLoadBalancerAttributes.builder().loadBalancerArn(albARNStr).vpc(vpc)
						.securityGroupId(albSGStr).build());

		// point to getDataService
		ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "ALBTargetGroup").vpc(vpc)
				.healthCheck(null).port(8080).protocol(ApplicationProtocol.HTTP).targetType(TargetType.IP)
				.targets(List.of(getDataService)).build();

		// listener for data service
		ApplicationListener listener = alb.addListener("GetDataListener",
				BaseApplicationListenerProps.builder().port(80).defaultTargetGroups(List.of(targetGroup)).build());

		// select first 2 subnets
		List<ISubnet> privateSubnets = vpc.getIsolatedSubnets();
		List<ISubnet> subPrivateSubnets = List.of(privateSubnets.get(0), privateSubnets.get(1));

		// create vpc link
		SecurityGroup vpcLinkSecurityGroup = new SecurityGroup(this, "LinkSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());
		VpcLink vpcLink = VpcLink.Builder.create(this, "VpcLink").vpc(vpc)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build())
				.securityGroups(List.of(vpcLinkSecurityGroup)).build();

		// update SG permissions
		albSecurityGroup.addIngressRule(vpcLinkSecurityGroup, Port.allTraffic());
		albSecurityGroup.addEgressRule(vpcLinkSecurityGroup, Port.allTraffic());
		albSecurityGroup.addEgressRule(asgsg, Port.allTraffic());
		asgsg.addIngressRule(albSecurityGroup, Port.allTraffic());
		vpcLinkSecurityGroup.addEgressRule(albSecurityGroup, Port.allTraffic());
		vpcLinkSecurityGroup.addIngressRule(albSecurityGroup, Port.allTraffic());

		// integrate ALB listener with vpc link -
		HttpAlbIntegration albIntegration = HttpAlbIntegration.Builder.create("ALBLinkIntegration", listener)
				.method(HttpMethod.ANY).vpcLink(vpcLink).build();

		// create authorizer for api gateway - with same client use for Amplify frontend
		HttpUserPoolAuthorizer apiGatewayAuthorizer = HttpUserPoolAuthorizer.Builder
				.create("ECSAPIAuthorizer", userPool).authorizerName("ECSAPIAuthorizer")
				.userPoolClients(List.of(userPoolClient)).build();

		// Create HTTP API using albIntegration - allow cors
		ecsHTTPApi = HttpApi.Builder.create(this, "GetDataHttpApi")
				.corsPreflight(CorsPreflightOptions.builder().allowCredentials(false).allowOrigins(List.of("*"))
						.allowMethods(List.of(CorsHttpMethod.GET, CorsHttpMethod.POST))
						.allowHeaders(List.of("Content-Type", "origin", "accept", "Authorization"))
						.maxAge(Duration.days(10)).build())
				.build();
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/getOrder").methods(List.of(HttpMethod.GET))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/searchOrder").methods(List.of(HttpMethod.POST))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/searchError").methods(List.of(HttpMethod.POST))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/getError").methods(List.of(HttpMethod.GET))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/getErrorServiceNames").methods(List.of(HttpMethod.GET))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/searchStats").methods(List.of(HttpMethod.POST))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/getStatServiceNames").methods(List.of(HttpMethod.GET))
				.authorizer(apiGatewayAuthorizer).integration(albIntegration).build());
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

	private void lookupSecrets() {
		String invdbsecarnStr = System.getenv("INVDBSECARN");
		if (invdbsecarnStr != null && invdbsecarnStr.trim().length() > 0) {
			invdbSecret = software.amazon.awscdk.services.secretsmanager.Secret.fromSecretCompleteArn(this,
					"INVDBSECARN", invdbsecarnStr);
		}
	}

	/**
	 * Output values from CDK to be used later.
	 */
	private void setupOutputVariables() {
		// no underscores in output name
		CfnOutput.Builder.create(this, "DBSECARN").value(dbSecret.getSecretArn()).build();
		CfnOutput.Builder.create(this, "SSMEPID").value(endpoint.getVpcEndpointId()).build();
		CfnOutput.Builder.create(this, "AMPEP").value(amplifyApp.getAttrAppId()).build();
		CfnOutput.Builder.create(this, "ORDMGMUSERPOOLID").value(userPool.getUserPoolId()).build();
		CfnOutput.Builder.create(this, "ORDMGMCLIENTID").value(userPoolClient.getUserPoolClientId()).build();
		CfnOutput.Builder.create(this, "AMPLIFYROLEARN").value(amplifyRole.getRoleArn()).build();
		CfnOutput.Builder.create(this, "APIEP").value(ecsHTTPApi.getApiEndpoint()).build();
		CfnOutput.Builder.create(this, "APIID").value(ecsHTTPApi.getApiId()).build();
	}
}