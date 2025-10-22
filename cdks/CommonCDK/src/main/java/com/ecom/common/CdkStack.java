package com.ecom.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigatewayv2.VpcLink;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.CfnVPCPeeringConnection;
import software.amazon.awscdk.services.ec2.FlowLog;
import software.amazon.awscdk.services.ec2.FlowLogDestination;
import software.amazon.awscdk.services.ec2.FlowLogResourceType;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpoint;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.IpAddresses;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroupProps;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.AsgCapacityProvider;
import software.amazon.awscdk.services.ecs.CapacityProviderStrategy;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.EcsOptimizedImage;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketPolicy;
import software.amazon.awscdk.services.s3.BucketPolicyProps;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

/**
 * 
 */
public class CdkStack extends Stack {

	// Assuming USERVPC CIDR block is 10.1.0.0/16 from buildbox stack
	private static final String ecomVPCCIDR = "10.2.0.0/16";
	private static final String userVPCCIDR = "10.1.0.0/16";

	private Stack stack;

	private Vpc vpc;
	private InterfaceVpcEndpoint endpoint;
	private ISecurityGroup smepsg;
	private ISecurityGroup ecsepsg;
	private ISecurityGroup asgsg;

	private Bucket ecomBucket;

	private Queue createOrderQ;
	private Topic orderStatusUpdatesTopic;

	private Function runDDLFunc;
	private ISecurityGroup runDDLFuncSG;

	private Cluster cluster;
	private Role ecsInstanceRole;
	private Repository ecrrepo;
	private AutoScalingGroup asg;
	private PrivateDnsNamespace namespace;

	private SecurityGroup albSecurityGroup;
	private ApplicationLoadBalancer alb;
	private VpcLink vpcLink;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
		stack = this;

		setupNetworking();
		// setupVPCPeering();
		setupCommonVPCEndpoints();
		setupS3();
		setupRunDDLLambda("/home/cloudshell-user/ECOM/modules/CommonModule/");
		setupECSCluster();
		setupVPCALB();
		setupSQS();
		setupSNS();

		setupOutputVariables();
	}

	private void setupSNS() {
		orderStatusUpdatesTopic = Topic.Builder.create(this, "OrderStatusUpdates").topicName("OrderStatusUpdates")
				.fifo(true)
				.messageRetentionPeriodInDays(1)
				.build();
	}

	private void setupSQS() {
		createOrderQ = Queue.Builder.create(this, "CreateOrderQ").queueName("CreateOrderQ")
				.retentionPeriod(Duration.days(1)).build();

		// add policy to sqs to allow posting from sns
		createOrderQ.addToResourcePolicy(PolicyStatement.Builder.create().actions(List.of("sqs:SendMessage"))
				.principals(List.of(new ServicePrincipal("sns.amazonaws.com")))
				.resources(List.of(createOrderQ.getQueueArn()))
				.conditions(Map.of("ArnLike", Map.of("aws:SourceArn", "arn:aws:sns:*:*:*"))).build());
	}

	/**
	 * Setup ECS cluster with all the needed vpc endpoints
	 */
	private void setupECSCluster() {
		// create ECS role and add permissions to it
		ecsInstanceRole = Role.Builder.create(this, "EcsInstanceRole")
				.assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
				.managedPolicies(List
						.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEC2ContainerServiceforEC2Role")))
				.build();
		ecsInstanceRole.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AmazonEC2ContainerRegistryReadOnly"));

		// select first 2 subnets for setting up ecs
		List<ISubnet> privateSubnets = vpc.getIsolatedSubnets();
		List<ISubnet> subPrivateSubnets = List.of(privateSubnets.get(0), privateSubnets.get(1));

		// create ECS cluster
		cluster = Cluster.Builder.create(this, "ECOMECSCluster").clusterName("ECOMECSCluster").containerInsights(false)
				.vpc(vpc).build();

		// create ASG for ECS & add to cluster AutoScalingGroup asg - set outbound true,
		// otherwise containers are not able to communicate back to ecs
		asgsg = new SecurityGroup(this, "ECSASGSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(true).build());
		asg = AutoScalingGroup.Builder.create(this, "ECOMECSASG").vpc(vpc)
				.instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
				.machineImage(EcsOptimizedImage.amazonLinux2()).minCapacity(2).maxCapacity(4).securityGroup(asgsg)
				.vpcSubnets(SubnetSelection.builder().subnets(subPrivateSubnets).build())
				.associatePublicIpAddress(false).build();

		// register asg as capacity provider for ECS
		AsgCapacityProvider capacityProvider = AsgCapacityProvider.Builder.create(this, "ECOMECSASGCapProv")
				.capacityProviderName("ECOMECSASGCapProv").autoScalingGroup(asg).enableManagedScaling(true)
				.enableManagedTerminationProtection(true).build();
		cluster.addAsgCapacityProvider(capacityProvider);

		// set default capacity provider
		cluster.addDefaultCapacityProviderStrategy(List.of(CapacityProviderStrategy.builder()
				.capacityProvider(capacityProvider.getCapacityProviderName()).weight(1).build()));

		// setup cluster namespace
		namespace = PrivateDnsNamespace.Builder.create(this, "EcomNamespace").name("ecom.internal").vpc(vpc).build();

		// create ECR to hold docker images & grant access to it
		ecrrepo = Repository.Builder.create(this, "ecomrepo").repositoryName("ecomrepo").build();
		ecrrepo.grantRead(asg);
		ecrrepo.grantRead(ecsInstanceRole);

		// create endpoints to communicate with ecs & cloudwatch logs - use same sg for
		// all endpoints
		ecsepsg = new SecurityGroup(this, "ECSEPSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());
		ecsepsg.addIngressRule(asgsg, Port.HTTPS);
		ecsepsg.addEgressRule(asgsg, Port.HTTPS);
		asgsg.addEgressRule(ecsepsg, Port.HTTPS);
		asgsg.addIngressRule(ecsepsg, Port.HTTPS);

		InterfaceVpcEndpoint ecsEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSInterfaceEndpoint").vpc(vpc)
				.service(InterfaceVpcEndpointAwsService.ECS).privateDnsEnabled(true).securityGroups(List.of(ecsepsg))
				.open(true).subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint ecsAgentEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSAgtInterfaceEndpoint")
				.vpc(vpc).service(InterfaceVpcEndpointAwsService.ECS_AGENT).privateDnsEnabled(true)
				.securityGroups(List.of(ecsepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint ecsTelEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSTelInterfaceEndpoint")
				.vpc(vpc).service(InterfaceVpcEndpointAwsService.ECS_TELEMETRY).privateDnsEnabled(true)
				.securityGroups(List.of(ecsepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint ecrEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECRInterfaceEndpoint").vpc(vpc)
				.service(InterfaceVpcEndpointAwsService.ECR).privateDnsEnabled(true).securityGroups(List.of(ecsepsg))
				.open(true).subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint ecrDckrEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECRDckrInterfaceEndpoint")
				.vpc(vpc).service(InterfaceVpcEndpointAwsService.ECR_DOCKER).privateDnsEnabled(true)
				.securityGroups(List.of(ecsepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint sqsEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSSqsInterfaceEndpoint").vpc(vpc)
				.service(InterfaceVpcEndpointAwsService.SQS).privateDnsEnabled(true).securityGroups(List.of(ecsepsg))
				.open(true).subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		// need s3 endpoint to pull image from ecr
		GatewayVpcEndpoint s3Endpoint = GatewayVpcEndpoint.Builder.create(this, "S3GatewayEndpoint").vpc(vpc)
				.subnets(List.of(SubnetSelection.builder().subnets(subPrivateSubnets).build()))
				.service(GatewayVpcEndpointAwsService.S3).build();

		GatewayVpcEndpoint dynDBEndpoint = GatewayVpcEndpoint.Builder.create(this, "DynDBGatewayEndpoint").vpc(vpc)
				.subnets(List.of(SubnetSelection.builder().subnets(subPrivateSubnets).build()))
				.service(GatewayVpcEndpointAwsService.DYNAMODB).build();

		// need log endpoint to write logs to log group
		InterfaceVpcEndpoint logEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSLogInterfaceEndpoint").vpc(vpc)
				.service(InterfaceVpcEndpointAwsService.CLOUDWATCH_LOGS).privateDnsEnabled(true)
				.securityGroups(List.of(ecsepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();

		InterfaceVpcEndpoint lambdaEndpoint = InterfaceVpcEndpoint.Builder.create(this, "ECSLambdaInterfaceEndpoint")
				.vpc(vpc).service(InterfaceVpcEndpointAwsService.LAMBDA).privateDnsEnabled(true)
				.securityGroups(List.of(ecsepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();
	}

	/**
	 * Lambda service to execute ddl queries against any rds db
	 * 
	 * @param baseDir
	 */
	private void setupRunDDLLambda(String baseDir) {

		String lambdaName = "RunDDLQuery";

		runDDLFuncSG = new SecurityGroup(stack, lambdaName + "SecurityGroup", SecurityGroupProps.builder().vpc(vpc)
				.allowAllOutbound(false).securityGroupName(lambdaName + "SG").build());
		smepsg.addIngressRule(runDDLFuncSG, Port.HTTPS);
		runDDLFuncSG.addEgressRule(smepsg, Port.HTTPS);

		runDDLFunc = Function.Builder.create(this, lambdaName).runtime(Runtime.PYTHON_3_11).functionName(lambdaName)
				.code(Code.fromAsset(baseDir + lambdaName + ".zip")).handler("lambda_function.lambda_handler")
				.securityGroups(Arrays.asList(runDDLFuncSG)).vpc(vpc).build();
	}

	/**
	 * General purpose s3 bucket for ECOM activities and website hosting
	 */
	private void setupS3() {
		ecomBucket = new Bucket(this, "ECOMBucket", BucketProps.builder().versioned(false).build());

		// add required policy statements to bucket policy
		PolicyStatement listBucketStatement = PolicyStatement.Builder.create().effect(Effect.ALLOW)
				.principals(List.of(new ServicePrincipal("amplify.amazonaws.com"))).actions(List.of("s3:ListBucket"))
				.resources(List.of(ecomBucket.getBucketArn())).build();

		PolicyStatement getObjectStatement = PolicyStatement.Builder.create().effect(Effect.ALLOW)
				.principals(List.of(new ServicePrincipal("amplify.amazonaws.com"))).actions(List.of("s3:GetObject"))
				.resources(List.of(ecomBucket.getBucketArn() + "/ordmgm/ui/*")).build();

		BucketPolicy bucketPolicy = new BucketPolicy(this, "ECOMBucketPolicy",
				BucketPolicyProps.builder().bucket(ecomBucket).build());

		bucketPolicy.getDocument().addStatements(listBucketStatement, getObjectStatement);
	}

	/**
	 * Common VPC endpoints
	 */
	private void setupCommonVPCEndpoints() {
		// setup secrets endpoint for vpc - choose 2 subnets to reduce eni's
		List<ISubnet> privateSubnets = vpc.getIsolatedSubnets();
		List<ISubnet> subPrivateSubnets = List.of(privateSubnets.get(0), privateSubnets.get(1));
		smepsg = new SecurityGroup(stack, "SMEPSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());
		endpoint = InterfaceVpcEndpoint.Builder.create(this, "SMInterfaceEndpoint").vpc(vpc)
				.service(InterfaceVpcEndpointAwsService.SECRETS_MANAGER).privateDnsEnabled(true)
				.securityGroups(List.of(smepsg)).open(true)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build()).build();
	}

	/**
	 * Setup isolated vpc with vpc flow logs enabled to s3 bucket
	 */
	private void setupNetworking() {

		// fetch ECOM vpc if already existing, otherwise create it
		vpc = Vpc.Builder.create(this, "ECOMVPC").vpcName("ECOMVPC")
				.subnetConfiguration(Arrays.asList(SubnetConfiguration.builder().cidrMask(24).name("PrivateSubnet")
						.subnetType(SubnetType.PRIVATE_ISOLATED).build()))
				.ipAddresses(IpAddresses.cidr(ecomVPCCIDR)).maxAzs(99) // Use all AZ's
				.enableDnsHostnames(true).enableDnsSupport(true).natGateways(0).build();

		Bucket vpcflowLogsBucket = Bucket.Builder.create(this, "ECOMVPCFlowLogsBucket").build();
		FlowLog.Builder.create(this, "ECOMVPCFlowLogs").flowLogName("ECOMVPCFlowLogs")
				.resourceType(FlowLogResourceType.fromVpc(vpc)).destination(FlowLogDestination.toS3(vpcflowLogsBucket))
				.build();

		/*
		 * NetworkAcl ecomVPCNACL = NetworkAcl.Builder.create(this,
		 * "ECOMVPCNACL").vpc(vpc)
		 * .subnetSelection(SubnetSelection.builder().subnetType(SubnetType.
		 * PRIVATE_ISOLATED).build()).build();
		 * 
		 * // allow 8080 port traffic from/to userVPC w.r.t ecomVPC
		 * ecomVPCNACL.addEntry("Allow from User VPC",
		 * CommonNetworkAclEntryOptions.builder().cidr(AclCidr.ipv4(userVPCCIDR)).
		 * ruleNumber(200)
		 * .traffic(AclTraffic.tcpPort(8080)).direction(TrafficDirection.INGRESS).
		 * ruleAction(Action.ALLOW) .build());
		 * 
		 * ecomVPCNACL.addEntry("Allow to User VPC",
		 * CommonNetworkAclEntryOptions.builder().cidr(AclCidr.ipv4(userVPCCIDR)).
		 * ruleNumber(300) .traffic(AclTraffic.tcpPortRange(1024,
		 * 65535)).direction(TrafficDirection.EGRESS)
		 * .ruleAction(Action.ALLOW).build());
		 */
	}

	private void setupVPCPeering() {
		// fetch user vpc attributes
		String userVPCIDStr = System.getenv("USERVPCID");
		IVpc uservpc = Vpc.fromLookup(this, userVPCIDStr, VpcLookupOptions.builder().vpcId(userVPCIDStr).build());

		CfnVPCPeeringConnection peeringConnection = CfnVPCPeeringConnection.Builder
				.create(this, "ECOMAndUSERVPCPeering").vpcId(vpc.getVpcId()).peerVpcId(uservpc.getVpcId()).build();

		// add routes between both vpc's
		for (ISubnet subnet : vpc.getPrivateSubnets()) {
			CfnRoute.Builder.create(this, "RouteFromECOMtoUSERVPC" + subnet.getNode().getId())
					.routeTableId(subnet.getRouteTable().getRouteTableId()).destinationCidrBlock(userVPCCIDR)
					.vpcPeeringConnectionId(peeringConnection.getRef()).build();
		}

		for (ISubnet subnet : uservpc.getPrivateSubnets()) {
			CfnRoute.Builder.create(this, "RouteFromUSERtoECOMVPC" + subnet.getNode().getId())
					.routeTableId(subnet.getRouteTable().getRouteTableId()).destinationCidrBlock(ecomVPCCIDR)
					.vpcPeeringConnectionId(peeringConnection.getRef()).build();
		}
	}

	/**
	 * Create an ALB in private VPC and a VPC Link to expose it
	 */
	private void setupVPCALB() {
		// create alb with sg
		albSecurityGroup = new SecurityGroup(this, "ECSALBSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());

		// select first 2 subnets for setting up alb, vpc link
		List<ISubnet> privateSubnets = vpc.getIsolatedSubnets();
		List<ISubnet> subPrivateSubnets = List.of(privateSubnets.get(0), privateSubnets.get(1));

		// alb
		alb = ApplicationLoadBalancer.Builder.create(this, "ECSALB").vpc(vpc)
				.vpcSubnets(SubnetSelection.builder().subnets(subPrivateSubnets).build())
				.securityGroup(albSecurityGroup).internetFacing(false).build();

		// vpc link
		SecurityGroup vpcLinkSecurityGroup = new SecurityGroup(this, "LinkSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());
		vpcLink = VpcLink.Builder.create(this, "VpcLink").vpc(vpc)
				.subnets(SubnetSelection.builder().subnets(subPrivateSubnets).build())
				.securityGroups(List.of(vpcLinkSecurityGroup)).build();

		// add permission between alb and vpc link
		albSecurityGroup.addIngressRule(vpcLinkSecurityGroup, Port.allTraffic());
		albSecurityGroup.addEgressRule(vpcLinkSecurityGroup, Port.allTraffic());
		vpcLinkSecurityGroup.addEgressRule(albSecurityGroup, Port.allTraffic());
		vpcLinkSecurityGroup.addIngressRule(albSecurityGroup, Port.allTraffic());
		albSecurityGroup.addEgressRule(asgsg, Port.allTraffic());
		asgsg.addIngressRule(albSecurityGroup, Port.allTraffic());
	}

	/**
	 * Output values from CDK to be used later.
	 */
	private void setupOutputVariables() {
		// no underscores in output name
		CfnOutput.Builder.create(this, "SMEPID").value(endpoint.getVpcEndpointId()).build();
		CfnOutput.Builder.create(this, "SMSGID").value(smepsg.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "VPCID").value(vpc.getVpcId()).build();
		CfnOutput.Builder.create(this, "ECOMBKTARN").value(ecomBucket.getBucketArn()).build();
		CfnOutput.Builder.create(this, "ECOMBKTNAME").value(ecomBucket.getBucketName()).build();
		CfnOutput.Builder.create(this, "RunDDLFUNCID").value(runDDLFunc.getFunctionArn()).build();
		CfnOutput.Builder.create(this, "RunDDLSGID").value(runDDLFuncSG.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "RunDDLFUNCRLID").value(runDDLFunc.getRole().getRoleArn()).build();
		CfnOutput.Builder.create(this, "ECSARN").value(cluster.getClusterArn()).build();
		CfnOutput.Builder.create(this, "ECSROLE").value(ecsInstanceRole.getRoleArn()).build();
		CfnOutput.Builder.create(this, "ECRREPO").value(ecrrepo.getRepositoryUri()).build();
		CfnOutput.Builder.create(this, "ECREPSGID").value(ecsepsg.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "ECSASGROLE").value(asg.getRole().getRoleArn()).build();
		CfnOutput.Builder.create(this, "ECSASGSG").value(asgsg.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "ECSNMSPARN").value(namespace.getNamespaceArn()).build();
		CfnOutput.Builder.create(this, "ECSNMSPID").value(namespace.getNamespaceId()).build();
		CfnOutput.Builder.create(this, "ALBARN").value(alb.getLoadBalancerArn()).build();
		CfnOutput.Builder.create(this, "ALBSG").value(albSecurityGroup.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "CREATEORDERQARN").value(createOrderQ.getQueueArn()).build();
		CfnOutput.Builder.create(this, "VPCLINKID").value(vpcLink.getVpcLinkId()).build();
		CfnOutput.Builder.create(this, "OSUTOPICARN").value(orderStatusUpdatesTopic.getTopicArn()).build();
	}
}