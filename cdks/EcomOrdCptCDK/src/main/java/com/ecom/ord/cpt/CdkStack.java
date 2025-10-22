package com.ecom.ord.cpt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.Aws;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpAlbIntegration;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.AwsIntegration;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizer;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.EndpointConfiguration;
import software.amazon.awscdk.services.apigateway.EndpointType;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.IntegrationOptions;
import software.amazon.awscdk.services.apigateway.IntegrationResponse;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.MethodResponse;
import software.amazon.awscdk.services.apigateway.Model;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod;
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.IVpcLink;
import software.amazon.awscdk.services.apigatewayv2.VpcLink;
import software.amazon.awscdk.services.apigatewayv2.VpcLinkAttributes;
import software.amazon.awscdk.services.appsync.AppsyncFunction;
import software.amazon.awscdk.services.appsync.AppsyncFunctionProps;
import software.amazon.awscdk.services.appsync.AuthorizationConfig;
import software.amazon.awscdk.services.appsync.AuthorizationMode;
import software.amazon.awscdk.services.appsync.Code;
import software.amazon.awscdk.services.appsync.Definition;
import software.amazon.awscdk.services.appsync.DynamoDbDataSource;
import software.amazon.awscdk.services.appsync.FunctionRuntime;
import software.amazon.awscdk.services.appsync.GraphqlApi;
import software.amazon.awscdk.services.appsync.NoneDataSource;
import software.amazon.awscdk.services.appsync.Resolver;
import software.amazon.awscdk.services.appsync.UserPoolConfig;
import software.amazon.awscdk.services.cloudfront.CachePolicy;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.DistributionProps;
import software.amazon.awscdk.services.cloudfront.IOrigin;
import software.amazon.awscdk.services.cloudfront.OriginAccessIdentity;
import software.amazon.awscdk.services.cloudfront.ResponseHeadersPolicy;
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOriginWithOAIProps;
import software.amazon.awscdk.services.cognito.AccountRecovery;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs;
import software.amazon.awscdk.services.cognito.CfnUserPoolClient;
import software.amazon.awscdk.services.cognito.CognitoDomainOptions;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.OAuthFlows;
import software.amazon.awscdk.services.cognito.OAuthScope;
import software.amazon.awscdk.services.cognito.OAuthSettings;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolDomain;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.IInterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpointAttributes;
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
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.Ec2Service;
import software.amazon.awscdk.services.ecs.Ec2TaskDefinition;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.NetworkMode;
import software.amazon.awscdk.services.ecs.PortMapping;
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
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;
import software.amazon.awscdk.services.servicediscovery.IPrivateDnsNamespace;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespaceAttributes;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.LambdaSubscription;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.IQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class CdkStack extends Stack {

	private Stack stack;
	private String stackName;
	private Properties additionalProperties;

	private IVpc vpc;
	private IInterfaceVpcEndpoint endpoint;

	private ISecurityGroup smepsg;
	private ISecurityGroup asgsg;

	private UserPoolDomain userPoolDomain;
	private UserPoolClient userPoolClient;
	private IUserPool userPool;
	private CognitoUserPoolsAuthorizer authorizer;

	private Bucket websiteBucket;
	private IBucket bucket;
	private Distribution distribution;
	private GraphqlApi userInfoApi;
	private RestApi itemInfoApi;
	private RestApi qapi;

	private IQueue createOrderQ;
	private Topic createOrderTopic;

	private Table userInfoTable;

	private Ec2Service getSkuListService;
	private HttpApi ecsHTTPApi;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
		stack = this;
		this.stackName = id;
		this.additionalProperties = additionalProperties;

		String baseDir = "/home/ec2-user/deploymentWorkspace3/modules/OrderCaptureModule/";

		lookupNetwork();
		lookupEndpoints();
		lookupS3();
		lookupSQS();

		setupCognito();
		createUserInfoService(baseDir);
		createItemInfoService(baseDir);
		setupOrderConsole(baseDir);
		createSNSBackedAPI();
		setupECSJobs(baseDir);
		exposeECSWithHttpApi();
		setupLambda("OrderHistoryOp", baseDir);

		// record output variables
		setupOutputVariables();
	}

	private void setupLambda(String lambdaName, String baseDir) {

		String relLambdaName = stackName + lambdaName;

		SecurityGroup sg = new SecurityGroup(stack, relLambdaName + "SecurityGroup",
				SecurityGroupProps.builder().allowAllOutbound(true).vpc(vpc).build());

		Function function = Function.Builder.create(this, relLambdaName).runtime(Runtime.PYTHON_3_11)
				.functionName(relLambdaName)
				.code(software.amazon.awscdk.services.lambda.Code.fromAsset(baseDir + lambdaName + ".zip"))
				.handler("lambda_function.lambda_handler").securityGroups(Arrays.asList(sg)).vpc(vpc)
				.environment(Map.of("TABLE_NAME", userInfoTable.getTableName())).build();

		// need to update lambda function to read
		userInfoTable.grantReadWriteData(function);

		// add lambda as subscriber to sns
		createOrderTopic.grantSubscribe(function);
		createOrderTopic.addSubscription(new LambdaSubscription(function));
	}

	private void createSNSBackedAPI() {
		// create sns topic with Q as subscriber
		createOrderTopic = Topic.Builder.create(this, "CreateOrderTopic").topicName("CreateOrderTopic").build();
		createOrderTopic.addSubscription(new SqsSubscription(createOrderQ));

		// similar to
		// https://docs.aws.amazon.com/prescriptive-guidance/latest/patterns/integrate-amazon-api-gateway-with-amazon-sqs-to-handle-asynchronous-rest-apis.html
		Role apiRole = Role.Builder.create(this, "APIRole").assumedBy(new ServicePrincipal("apigateway.amazonaws.com"))
				.managedPolicies(List.of(ManagedPolicy.fromAwsManagedPolicyName("AmazonSNSFullAccess"))).build();
		createOrderTopic.grantPublish(apiRole);

		Integration snsIntegration = AwsIntegration.Builder.create().service("sns").region("us-east-1")
				.path("sns:action/Publish").integrationHttpMethod("POST")
				.options(IntegrationOptions.builder().credentialsRole(apiRole)
						.requestParameters(Map.of("integration.request.header.Content-Type",
								"'application/x-www-form-urlencoded'"))
						.requestTemplates(Map.of("application/json",
								"Action=Publish&TopicArn="
										+ String.format("arn:aws:sns:us-east-1:%s:CreateOrderTopic", Aws.ACCOUNT_ID)
										+ "&Message=$util.urlEncode($input.body)"))
						.integrationResponses(List.of(IntegrationResponse.builder().statusCode("200")
								.responseParameters(Map.of("method.response.header.Access-Control-Allow-Origin", "'*'"))
								.responseTemplates(Map.of("application/json", "{\"success\": true}")).build()))
						.build())
				.build();

		qapi = RestApi.Builder.create(this, "QApi").restApiName("CreateOrderQApi")
				.endpointConfiguration(EndpointConfiguration.builder().types(List.of(EndpointType.EDGE)).build())
				.defaultCorsPreflightOptions(CorsOptions.builder().allowCredentials(false).allowOrigins(List.of("*"))
						.allowMethods(List.of(CorsHttpMethod.POST.toString()))
						.allowHeaders(List.of("Content-Type", "origin", "accept", "Authorization"))
						.maxAge(Duration.days(10)).build())
				.build();

		qapi.getRoot()
				.addMethod("POST", snsIntegration,
						MethodOptions.builder()
								.methodResponses(List.of(
										MethodResponse.builder().statusCode("200")
												.responseParameters(Map
														.of("method.response.header.Access-Control-Allow-Origin", true))
												.responseModels(Map.of("application/json", Model.EMPTY_MODEL)).build(),
										MethodResponse.builder().statusCode("400")
												.responseModels(Map.of("application/json", Model.EMPTY_MODEL)).build()))
								.authorizationType(AuthorizationType.COGNITO).authorizer(authorizer)
								.authorizationScopes(List.of("email", "openid")).build());
	}

	private void setupOrderConsole(String baseDir) {
		baseDir = baseDir + "/Console/";

		// cloudfront for app sync & website
		websiteBucket = new Bucket(this, "OrdCapHostBucket",
				BucketProps.builder().websiteIndexDocument("index.html").websiteErrorDocument("error.html")
						.publicReadAccess(false).blockPublicAccess(BlockPublicAccess.BLOCK_ALL).autoDeleteObjects(true)
						.removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY).build());

		OriginAccessIdentity accessIdentity = OriginAccessIdentity.Builder.create(this, "OrdCapHostAccess").build();
		IOrigin s3BucketOrigin = S3BucketOrigin.withOriginAccessIdentity(websiteBucket,
				S3BucketOriginWithOAIProps.builder().originPath("/ordcpt/ui").build());
		websiteBucket.grantRead(accessIdentity);

		// create cloudfront distribution
		distribution = new Distribution(this, "OrderCapDistribution",
				DistributionProps.builder().defaultRootObject("index.html")
						.defaultBehavior(software.amazon.awscdk.services.cloudfront.BehaviorOptions.builder()
								.origin(s3BucketOrigin).cachePolicy(CachePolicy.CACHING_DISABLED) // change to
																									// enabled
																									// later on
																									// ***
								.viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS).build())
						.build());
	}

	private void setupCognito() {

		// user pool - start
		userPool = UserPool.Builder.create(this, "OrderCaptureUserPool").selfSignUpEnabled(true)
				.signInAliases(SignInAliases.builder().email(true).build())
				.autoVerify(AutoVerifiedAttrs.builder().email(true).build()).accountRecovery(AccountRecovery.EMAIL_ONLY)
				.standardAttributes(StandardAttributes.builder()
						.fullname(StandardAttribute.builder().required(true).mutable(true).build())
						.email(StandardAttribute.builder().required(true).mutable(true).build()).build())
				.build();

		// create cognito domain
		userPoolDomain = UserPoolDomain.Builder.create(this, "OrderCapturePoolDomain").userPool(userPool)
				.cognitoDomain(CognitoDomainOptions.builder().domainPrefix("ordercaptureaccess").build()).build();

		// create cognito client - without client secret
		userPoolClient = UserPoolClient.Builder.create(this, "OrderCaptureClient").userPool(userPool)
				.generateSecret(false).authFlows(AuthFlow.builder().userPassword(true).build())
				.accessTokenValidity(Duration.hours(1))
				.oAuth(OAuthSettings.builder().flows(OAuthFlows.builder().authorizationCodeGrant(true).build())
						.scopes(List.of(OAuthScope.EMAIL, OAuthScope.OPENID)).callbackUrls(List.of("https://")).build())
				.build();

		CfnUserPoolClient cfnClient = (CfnUserPoolClient) userPoolClient.getNode().getDefaultChild();
		cfnClient.addPropertyOverride("SupportedIdentityProviders", List.of("COGNITO"));

		authorizer = CognitoUserPoolsAuthorizer.Builder.create(this, "ConsoleUserAuthorizer")
				.cognitoUserPools(List.of(userPool)).authorizerName("ConsoleUserAuthorizer").build();
	}

	/**
	 * DB to hold item info including availability
	 * 
	 * @param baseDir
	 */
	private void createItemInfoService(String baseDir) {
		// get DB - start
		ITable itemInfoTable = Table.fromTableName(this, "ItemInfo", "ItemInfo");

		// createAppSyncForItemInfoService(baseDir, itemInfoTable);
		createAPIGWForItemInfoService(baseDir, itemInfoTable);
	}

	private void createAPIGWForItemInfoService(String baseDir, ITable itemInfoTable) {
		itemInfoApi = RestApi.Builder.create(this, "ItemInfoAPI").restApiName("ItemInfoAPI")
				.endpointConfiguration(EndpointConfiguration.builder().types(List.of(EndpointType.EDGE)).build())
				.build();
		Resource infoResource = itemInfoApi.getRoot().addResource("info");

		Role itemInfoRole = Role.Builder.create(this, "ItemInfoAPIGWRole")
				.assumedBy(new ServicePrincipal("apigateway.amazonaws.com")).build();
		itemInfoTable.grantReadData(itemInfoRole);

		AwsIntegration apigwIntegration = AwsIntegration.Builder.create().service("dynamodb")
				.integrationHttpMethod("POST").action("Query")
				.options(IntegrationOptions.builder().credentialsRole(itemInfoRole)
						.requestTemplates(Map.of("application/json", "{\n" + "  \"TableName\": \"ItemInfo\",\n"
								+ "  \"KeyConditionExpression\": \"itemID = :itemId and infoType = :infoType\",\n"
								+ "  \"ExpressionAttributeValues\": {\n"
								+ "    \":itemId\": { \"S\": \"$input.params('itemID')\" },\n"
								+ "    \":infoType\": { \"S\": \"$input.params('infoType')\" }\n" + "  }\n" + "}"))
						.integrationResponses(List.of(IntegrationResponse.builder().statusCode("200")
								.responseParameters(Map.of("method.response.header.Access-Control-Allow-Origin", "'*'"))
								.responseTemplates(Map.of("application/json", "#set($allParams = $input.params())\n"
										+ "{\n" + "	\"body-json\" : $input.json('$'),\n" + "	\"params\" : {\n"
										+ "		#foreach($type in $allParams.keySet())\n"
										+ "			#set($params = $allParams.get($type))\n" + "		\"$type\" : {\n"
										+ "			#foreach($paramName in $params.keySet())\n"
										+ "			\"$paramName\" : \"$util.escapeJavaScript($params.get($paramName))\"\n"
										+ "				#if($foreach.hasNext),#end\n" + "			#end\n" + "		}\n"
										+ "			#if($foreach.hasNext),#end\n" + "		#end\n" + "	}\n" + "}"))
								.build()))
						.build())
				.build();

		infoResource.addMethod("GET", apigwIntegration,
				MethodOptions.builder()
						.methodResponses(List.of(MethodResponse.builder().statusCode("200")
								.responseParameters(Map.of("method.response.header.Access-Control-Allow-Origin", true))
								.build()))
						.build());
	}

	@SuppressWarnings("unused")
	private void createAppSyncForItemInfoService(String baseDir, ITable itemInfoTable) {
		// app sync for dynamodb
		GraphqlApi itemInfoApi = GraphqlApi.Builder.create(this, "ItemInfoApi").name("ItemInfoApi")
				.definition(Definition.fromFile(baseDir + "ItemInfo/appsync/schema/schema.graphql")).xrayEnabled(false)
				.build();

		Role appSyncRole = Role.Builder.create(this, "ItemInfoAppSyncRole")
				.assumedBy(new ServicePrincipal("appsync.amazonaws.com")).build();
		itemInfoTable.grantReadWriteData(appSyncRole);

		// Create data sources for app sync
		DynamoDbDataSource ddbDs = itemInfoApi.addDynamoDbDataSource("ItemInfoDataSource", itemInfoTable);

		// Query.getInfo
		Resolver.Builder.create(this, "GetItemInfoResolver").api(itemInfoApi).dataSource(ddbDs).typeName("Query")
				.fieldName("getItemInfo").runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "ItemInfo/appsync/resolvers/getItemInfo.js")).build();
	}

	/**
	 * DB to hold user info. A 2nd DB will be created using s3 import to hold
	 * availability info.
	 * 
	 * @param baseDir
	 */
	private void createUserInfoService(String baseDir) {
		// create Dynamodb table
		userInfoTable = Table.Builder.create(this, "UserInfo").tableName("UserInfo")
				.partitionKey(Attribute.builder().name("userSub").type(AttributeType.STRING).build())
				.sortKey(Attribute.builder().name("infoType").type(AttributeType.STRING).build())
				.billingMode(BillingMode.PAY_PER_REQUEST).removalPolicy(RemovalPolicy.DESTROY).build();

		createAppSyncForUserInfoService(baseDir, userInfoTable);
	}

	private void createAppSyncForUserInfoService(String baseDir, Table userInfoTable) {
		// app sync for dynamodb
		userInfoApi = GraphqlApi.Builder.create(this, "UserInfoApi").name("UserInfoApi")
				.definition(Definition.fromFile(baseDir + "UserInfo/schema/schema.graphql"))
				.authorizationConfig(AuthorizationConfig.builder()
						.defaultAuthorization(AuthorizationMode.builder()
								.authorizationType(software.amazon.awscdk.services.appsync.AuthorizationType.USER_POOL)
								.userPoolConfig(UserPoolConfig.builder().userPool(userPool).build()).build())
						.build())
				.xrayEnabled(false).build();

		Role appSyncRole = Role.Builder.create(this, "UserInfoAppSyncRole")
				.assumedBy(new ServicePrincipal("appsync.amazonaws.com")).build();
		userInfoTable.grantReadWriteData(appSyncRole);

		// Create data sources for app sync
		DynamoDbDataSource ddbDs = userInfoApi.addDynamoDbDataSource("UserInfoDataSource", userInfoTable);
		NoneDataSource noneDs = userInfoApi.addNoneDataSource("NoneDS");

		AppsyncFunction authCheckFn = new AppsyncFunction(this, "authCheckFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("AuthCheck").dataSource(noneDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/authCheck.js")).build());

		AppsyncFunction getInfoFn = new AppsyncFunction(this, "getUserInfoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("GetUserInfo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/getUserInfo.js")).build());

		AppsyncFunction putInfoFn = new AppsyncFunction(this, "putUserInfoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("PutUserInfo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/createUserInfo.js")).build());

		AppsyncFunction updateInfoFn = new AppsyncFunction(this, "updateUserInfoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("UpdateUserInfo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/updateUserInfo.js")).build());

		AppsyncFunction deleteInfoFn = new AppsyncFunction(this, "deleteUserInfoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("DeleteUserInfo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/deleteUserInfo.js")).build());

		AppsyncFunction getOrderNoFn = new AppsyncFunction(this, "getOrderNoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("GetOrderNo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/getOrderNo.js")).build());

		AppsyncFunction updateOrderNoFn = new AppsyncFunction(this, "updateOrderNoFn",
				AppsyncFunctionProps.builder().api(userInfoApi).name("UpdateOrderNo").dataSource(ddbDs)
						.runtime(FunctionRuntime.JS_1_0_0)
						.code(Code.fromAsset(baseDir + "UserInfo/functions/updateOrderNo.js")).build());

		// Query.getInfo
		Resolver.Builder.create(this, "GetUserInfoResolver").api(userInfoApi).typeName("Query").fieldName("getUserInfo")
				.runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "UserInfo/resolvers/genericResolver.js"))
				.pipelineConfig(List.of(authCheckFn, getInfoFn)).build();

		// Query.getInfo
		Resolver.Builder.create(this, "GenerateOrderNoResolver").api(userInfoApi).typeName("Query")
				.fieldName("generateOrderNo").runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "UserInfo/resolvers/genericResolver.js"))
				.pipelineConfig(List.of(authCheckFn, getOrderNoFn, updateOrderNoFn)).build();

		// Mutation.putInfo
		Resolver.Builder.create(this, "CreateUserInfoResolver").api(userInfoApi).typeName("Mutation")
				.fieldName("createUserInfo").runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "UserInfo/resolvers/genericResolver.js"))
				.pipelineConfig(List.of(authCheckFn, putInfoFn)).build();

		Resolver.Builder.create(this, "UpdateUserInfoResolver").api(userInfoApi).typeName("Mutation")
				.fieldName("updateUserInfo").runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "UserInfo/resolvers/genericResolver.js"))
				.pipelineConfig(List.of(authCheckFn, updateInfoFn)).build();

		// Mutation.deleteInfo
		Resolver.Builder.create(this, "DeleteUserInfoResolver").api(userInfoApi).typeName("Mutation")
				.fieldName("deleteUserInfo").runtime(FunctionRuntime.JS_1_0_0)
				.code(Code.fromAsset(baseDir + "UserInfo/resolvers/genericResolver.js"))
				.pipelineConfig(List.of(authCheckFn, deleteInfoFn)).build();

		distribution.addBehavior("/graphql",
				HttpOrigin.Builder.create(Fn.select(2, Fn.split("/", userInfoApi.getGraphqlUrl()))).build(),
				software.amazon.awscdk.services.cloudfront.BehaviorOptions.builder()
						.cachePolicy(CachePolicy.CACHING_DISABLED)
						.viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS).build());
	}

	private void setupECSJobs(String baseDir) {
		// lookup needed resources
		IRole asgRole = Role.fromRoleArn(this, "ECSASGROLE", System.getenv("ECSASGROLE"));
		asgsg = SecurityGroup.fromLookupById(this, "ECSASGSG", System.getenv("ECSASGSG"));
		ICluster cluster = Cluster.fromClusterAttributes(this, "ECOMECSCluster",
				ClusterAttributes.builder().clusterName(System.getenv("ECSARN")).vpc(vpc).build());
		IPrivateDnsNamespace pdn = PrivateDnsNamespace.fromPrivateDnsNamespaceAttributes(this, "EcomNamespace",
				PrivateDnsNamespaceAttributes.builder().namespaceArn(System.getenv("ECSNMSPARN"))
						.namespaceId(System.getenv("ECSNMSPID")).namespaceName("ecom.internal").build());

		getSkuListService = setupECSJob(baseDir, "SkuList", asgRole, asgsg, cluster, pdn);
	}

	private Ec2Service setupECSJob(String baseDir, String jobName, IRole asgRole, ISecurityGroup asgsg,
			ICluster cluster, IPrivateDnsNamespace pdn) {
		// create roles for ecs job
		Role executionRole = Role.Builder.create(this, jobName + "TskExecRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
				.managedPolicies(
						List.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonECSTaskExecutionRolePolicy"),
								ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
								ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess")))
				.build();

		List<IManagedPolicy> taskPolicies = List.of(ManagedPolicy.fromAwsManagedPolicyName("SecretsManagerReadWrite"),
				ManagedPolicy.fromAwsManagedPolicyName("AmazonS3ReadOnlyAccess"));
		Role taskRole = Role.Builder.create(this, jobName + "TskRole")
				.assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com")).managedPolicies(taskPolicies).build();
		websiteBucket.grantRead(taskRole);

		// create task definition along with log group
		Ec2TaskDefinition taskDefinition = Ec2TaskDefinition.Builder.create(this, jobName + "TskDef")
				.networkMode(NetworkMode.AWS_VPC).executionRole(executionRole).taskRole(taskRole).build();

		// create log group
		LogGroup logGroup = LogGroup.Builder.create(this, jobName + "LogGroup").logGroupName("/ecs/" + jobName)
				.retention(RetentionDays.ONE_DAY).build();
		logGroup.grantWrite(asgRole);

		int mem = 512;
		int cpu = 512;

		try {
			mem = Integer.parseInt(additionalProperties.getProperty(jobName + "MEM"));
			cpu = Integer.parseInt(additionalProperties.getProperty(jobName + "CPU"));
		} catch (Exception e) {

		}

		// define container along with rds secret
		String imageURI = System.getenv("ECRREPO") + ":" + jobName.toLowerCase();
		taskDefinition.addContainer(jobName + "Container", ContainerDefinitionOptions.builder()
				.image(ContainerImage.fromRegistry(imageURI)).memoryLimitMiB(mem).cpu(cpu)
				.portMappings(List.of(PortMapping.builder().containerPort(8080).build()))
				.logging(LogDriver.awsLogs(AwsLogDriverProps.builder().logGroup(logGroup).streamPrefix("ecom").build()))
				.environment(Map.of("bucketName", bucket.getBucketName())).build());

		// create service for task - use same sg as asg
		// expose service with cloud map and dns A records
		// set count as 0 and start service after initDB
		Ec2Service service = Ec2Service.Builder.create(this, jobName + "Service").cluster(cluster)
				.serviceName(jobName + "Service").securityGroups(List.of(asgsg))
				.cloudMapOptions(CloudMapOptions.builder().cloudMapNamespace(pdn).name(jobName.toLowerCase())
						.dnsRecordType(DnsRecordType.A).build())
				.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
				.taskDefinition(taskDefinition).desiredCount(0).build();

		return service;
	}

	private void exposeECSWithHttpApi() {
		// fetch alb created in common stack
		String albARNStr = System.getenv("ALBARN");
		String albSGStr = System.getenv("ALBSG");
		String vpclinkidStr = System.getenv("VPCLINKID");

		// fetch vpc link and ALB
		IVpcLink vpcLink = VpcLink.fromVpcLinkAttributes(this, "VpcLink",
				VpcLinkAttributes.builder().vpc(vpc).vpcLinkId(vpclinkidStr).build());

		IApplicationLoadBalancer alb = ApplicationLoadBalancer.fromApplicationLoadBalancerAttributes(this, stackName,
				ApplicationLoadBalancerAttributes.builder().loadBalancerArn(albARNStr).vpc(vpc)
						.securityGroupId(albSGStr).build());

		// create target group
		ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "SkuTargetGroup").vpc(vpc)
				.healthCheck(null).port(8080).protocol(ApplicationProtocol.HTTP).targetType(TargetType.IP)
				.targets(List.of(getSkuListService)).build();

		// create listener & point to target group
		ApplicationListener listener = alb.addListener("Listener", BaseApplicationListenerProps.builder()
				.protocol(ApplicationProtocol.HTTP).defaultTargetGroups(List.of(targetGroup)).port(81).build());

		// create new integration using vpclink between http api and ALB
		HttpAlbIntegration albIntegration = HttpAlbIntegration.Builder.create("ALBLinkIntegration", listener)
				.method(HttpMethod.ANY).vpcLink(vpcLink).build();

		// Create HTTP API using albIntegration - allow cors
		ecsHTTPApi = HttpApi.Builder.create(this, "GetSkuListHttpApi")
				.corsPreflight(CorsPreflightOptions.builder().allowCredentials(false).allowOrigins(List.of("*"))
						.allowMethods(List.of(CorsHttpMethod.GET))
						.allowHeaders(List.of("Content-Type", "origin", "accept")).maxAge(Duration.days(10)).build())
				.build();

		ecsHTTPApi.addRoutes(AddRoutesOptions.builder().path("/getSkuList").methods(List.of(HttpMethod.GET))
				.integration(albIntegration).build());

		// Using cloudfront distribution as restapi doesnt use apigateway v2, which
		// allows alb. For restapi need to create nlb ...
		distribution.addBehavior("/getSkuList",
				HttpOrigin.Builder.create(Fn.select(2, Fn.split("/", ecsHTTPApi.getApiEndpoint()))).build(),
				software.amazon.awscdk.services.cloudfront.BehaviorOptions.builder()
						.responseHeadersPolicy(
								ResponseHeadersPolicy.CORS_ALLOW_ALL_ORIGINS_WITH_PREFLIGHT_AND_SECURITY_HEADERS)
						.cachePolicy(CachePolicy.CACHING_OPTIMIZED)
						.viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS).build());
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

	private void lookupS3() {
		String bucketArn = System.getenv("ECOMBKTARN");
		bucket = Bucket.fromBucketArn(this, bucketArn, bucketArn);
	}

	private void lookupSQS() {
		String createOrderQARN = System.getenv("CREATEORDERQARN");
		createOrderQ = Queue.fromQueueArn(this, "CreateOrderQ", createOrderQARN);
	}

	/**
	 * Output values from CDK to be used later.
	 */
	private void setupOutputVariables() {
		// no underscores in output name
		CfnOutput.Builder.create(this, "SNSAPIURL").value(qapi.getUrl()).build();
		CfnOutput.Builder.create(this, "SNSAPIID").value(qapi.getRestApiId()).build();
		CfnOutput.Builder.create(this, "ORDCAPUSERPOOLID").value(userPool.getUserPoolId()).build();
		CfnOutput.Builder.create(this, "ORDCAPCLIENTID").value(userPoolClient.getUserPoolClientId()).build();
		CfnOutput.Builder.create(this, "CLOUDFRONTID").value(distribution.getDistributionId()).build();
		CfnOutput.Builder.create(this, "CLOUDFRONTDOMAIN").value(distribution.getDistributionDomainName()).build();
		CfnOutput.Builder.create(this, "USERINFOAPIID").value(userInfoApi.getApiId()).build();
		CfnOutput.Builder.create(this, "USERINFOGRPHURL").value(userInfoApi.getGraphqlUrl()).build();
		CfnOutput.Builder.create(this, "ITEMINFOURL").value(itemInfoApi.getUrl()).build();
		CfnOutput.Builder.create(this, "WEBBUCKET").value(websiteBucket.getBucketArn()).build();
		if (ecsHTTPApi != null) {
			CfnOutput.Builder.create(this, "SKULISTEP").value(ecsHTTPApi.getApiEndpoint()).build();
			CfnOutput.Builder.create(this, "SKULISTID").value(ecsHTTPApi.getApiId()).build();
		}
	}
}