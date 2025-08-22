package com.buildbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
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
import software.amazon.awscdk.services.ec2.BlockDevice;
import software.amazon.awscdk.services.ec2.BlockDeviceVolume;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnNatGateway;
import software.amazon.awscdk.services.ec2.CfnRoute;
import software.amazon.awscdk.services.ec2.EbsDeviceOptions;
import software.amazon.awscdk.services.ec2.EbsDeviceVolumeType;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceProps;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroupProps;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationTargetGroup;
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseApplicationListenerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerAction;
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerCertificate;
import software.amazon.awscdk.services.elasticloadbalancingv2.actions.AuthenticateCognitoAction;
import software.amazon.awscdk.services.elasticloadbalancingv2.targets.InstanceTarget;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

public class BuildBoxCdkStack extends Stack {

	private Stack stack;
	private String stackName;

	private Vpc vpc;
	private SecurityGroup ec2SecurityGroup;
	private SecurityGroup albSecurityGroup;
	private Role ec2Role;
	private String natGWAZ;

	private UserData userData;
	private Instance buildInstance;

	private ApplicationLoadBalancer alb;
	private UserPoolDomain userPoolDomain;
	private UserPoolClient userPoolClient;
	private UserPool userPool;

	public BuildBoxCdkStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);
		stack = this;
		this.stackName = id;

		setupNetworking();
		setupSecurityGroupsAndPermissions();
		setupEC2Instance();
		setupCognitoAuthenticatedALB();
		setupOutputVariables();
	}

	private void setupCognitoAuthenticatedALB() {
		createALB();
		setupCognito();
		configureALB();
	}

	/**
	 * Create target group for ALB, use existing certificate for https listener
	 * Setup listener to use cognito user pools for authentication
	 */
	private void configureALB() {
		ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "MyTargetGroup").vpc(vpc)
				.port(8080).protocol(ApplicationProtocol.HTTP).targets(List.of(new InstanceTarget(buildInstance)))
				.build();

		ICertificate existingCertificate = Certificate.fromCertificateArn(this, "CERT_ARN", System.getenv("CERT_ARN"));

		alb.addListener("HttpsListener",
				BaseApplicationListenerProps.builder().port(443).protocol(ApplicationProtocol.HTTPS)
						.certificates(List.of(ListenerCertificate.fromCertificateManager(existingCertificate)))
						.defaultAction(AuthenticateCognitoAction.Builder.create().userPool(userPool)
								.userPoolClient(userPoolClient).userPoolDomain(userPoolDomain)
								.next(ListenerAction.forward(List.of(targetGroup))).build())
						.build());
	}

	private void setupCognito() {
		// create cognito user pool
		userPool = UserPool.Builder.create(this, "BuildBoxUserPool").selfSignUpEnabled(false)
				.signInAliases(SignInAliases.builder().username(true).build()).build();

		// create cognito domain
		userPoolDomain = UserPoolDomain.Builder.create(this, "BuildBoxPoolDomain").userPool(userPool)
				.cognitoDomain(CognitoDomainOptions.builder().domainPrefix("buildboxaccess").build()).build();

		// create cognito client
		userPoolClient = UserPoolClient.Builder.create(this, "BuildBoxClient").userPool(userPool).generateSecret(true)
				.authFlows(AuthFlow.builder().userPassword(true).build())
				.accessTokenValidity(Duration.hours(1))
				.oAuth(OAuthSettings.builder().flows(OAuthFlows.builder().authorizationCodeGrant(true).build())
						.scopes(List.of(OAuthScope.EMAIL))
						.callbackUrls(
								List.of(String.format("https://%s/oauth2/idpresponse", alb.getLoadBalancerDnsName())))
						.build())
				.build();

		CfnUserPoolClient cfnClient = (CfnUserPoolClient) userPoolClient.getNode().getDefaultChild();
		cfnClient.addPropertyOverride("RefreshTokenValidity", 1);
		cfnClient.addPropertyOverride("SupportedIdentityProviders", List.of("COGNITO"));
	}

	private void createALB() {
		// create application load balancer
		alb = ApplicationLoadBalancer.Builder.create(this, "MyALB").vpc(vpc).securityGroup(albSecurityGroup)
				.internetFacing(true).build();
	}

	private void setupEC2Instance() {

		// setup ec2 roles and commands to run on EC2 instance creation
		setupEC2Role();
		configureUserData();

		// ec2 subnets
		SubnetSelection ec2Subnets = SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build();

		// ec2 ebs root volume
		BlockDevice rootDevice = BlockDevice.builder().deviceName("/dev/xvda")
				.volume(BlockDeviceVolume.ebs(30,
						EbsDeviceOptions.builder().encrypted(true).volumeType(EbsDeviceVolumeType.GP3).build()))
				.build();

		// ec2 instance - provision in same AZ as NAT GW to reduce cross AZ traffic
		InstanceProps instanceProps = InstanceProps.builder().instanceName("BuildBox").availabilityZone(natGWAZ)
				.instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM)).userData(userData)
				.vpcSubnets(ec2Subnets).machineImage(MachineImage.latestAmazonLinux2023()).vpc(vpc).role(ec2Role)
				.blockDevices(List.of(rootDevice)).securityGroup(ec2SecurityGroup).build();
		buildInstance = new Instance(stack, stackName, instanceProps);

		/*
		 * Volume rootVolume = Volume.Builder.create(this, "BuildBoxRootVolume")
		 * .availabilityZone(buildInstance.getInstanceAvailabilityZone()).size(Size.
		 * gibibytes(30)).encrypted(false) .build();
		 * 
		 * CfnVolumeAttachment.Builder.create(this,
		 * "RootVolumeAttachment").instanceId(buildInstance.getInstanceId())
		 * .volumeId(rootVolume.getVolumeId()).device("/dev/xvda").build();
		 */

	}

	private void configureUserData() {
		// ec2 commands
		List<String> commandList = new ArrayList<String>();
		userData = UserData.forLinux();
		setEC2CommandList(commandList);
		userData.addCommands(commandList.toArray(new String[0]));
	}

	private void setupEC2Role() {
		// Create role for ec2 box
		ec2Role = Role.Builder.create(this, "ec2BuildBoxRole").roleName("ec2BuildBoxRole")
				.assumedBy(new ServicePrincipal("ec2.amazonaws.com")).build();
		ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AdministratorAccess"));
		/*
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonRDSFullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonVPCFullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AWSLambda_FullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonAPIGatewayAdministrator"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonECS_FullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "SecretsManagerReadWrite"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonS3FullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonEventBridgeFullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "CloudWatchFullAccessV2"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AWSCloudFormationFullAccess"));
		 * ec2Role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName(
		 * "AmazonSSMFullAccess"));
		 */

		ec2Role.addToPolicy(PolicyStatement.Builder.create()
				.actions(Arrays.asList("iam:CreateServiceLinkedRole", "iam:GetRole", "iam:PassRole"))
				.resources(Arrays.asList("*")).build());
	}

	/**
	 * Create a security group each for ec2 & alb. And allow communication between
	 * them.
	 */
	private void setupSecurityGroupsAndPermissions() {
		// ec2 security group
		ec2SecurityGroup = new SecurityGroup(this, "BuildBoxSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(true).build());

		// alb security group
		albSecurityGroup = new SecurityGroup(this, "ALBSecurityGroup",
				SecurityGroupProps.builder().vpc(vpc).allowAllOutbound(false).build());

		// allow ec2-alb connectivity
		ec2SecurityGroup.addIngressRule(albSecurityGroup, Port.tcp(8080), "Allow ALB access to ec2 instance");
		albSecurityGroup.addEgressRule(ec2SecurityGroup, Port.tcp(8080));
	}

	/**
	 * Create Public and private subnets with an internet gateway and NAT gateway in
	 * public subnets. All private subnets route to the single NAT.
	 */
	private void setupNetworking() {

		// public subnet auto creates internet gateway in VPC
		// mark private subnet as isolated to prevent NAT creation, add routes for
		// isolated subnets later on
		vpc = Vpc.Builder.create(this, "BuildBoxVPC").subnetConfiguration(Arrays.asList(
				SubnetConfiguration.builder().cidrMask(24).name("PublicSubnet").subnetType(SubnetType.PUBLIC).build(),
				SubnetConfiguration.builder().cidrMask(24).name("PrivateSubnet").subnetType(SubnetType.PRIVATE_ISOLATED)
						.build()))
				.maxAzs(99) // Use all AZ's
				.natGateways(0).build();

		// get list of private and public subnets
		List<ISubnet> publicSubnets = vpc.getPublicSubnets();
		List<ISubnet> privateSubnets = vpc.getIsolatedSubnets();

		// Track already processed private subnet route table id's
		Map<String, String> processedPrivateSubnetRoutingTables = new HashMap<String, String>();

		// create an elastic IP for the NAT gateway
		CfnEIP eip = CfnEIP.Builder.create(this, "BuildBoxNatEIP").build();

		// Create a NAT instance in the first public subnet with elastic ip
		ISubnet natSubnet = publicSubnets.get(0);
		natGWAZ = natSubnet.getAvailabilityZone();
		CfnNatGateway natgateway = CfnNatGateway.Builder.create(this, "BuildBoxNatGateway")
				.allocationId(eip.getAttrAllocationId()).subnetId(natSubnet.getSubnetId()).build();

		// in each private subnet route table, add route to internet via NAT
		// similarly define route to each private subnet in the public subnet route
		// tables
		// Note: routes within VPC between subnets/AZ's already added by default
		for (int i = 0; i < privateSubnets.size(); i++) {
			String privateSubnetRouteTableID = privateSubnets.get(i).getRouteTable().getRouteTableId();

			// check if route table not already processed - in case all private subnets have
			// a single route table
			if (!processedPrivateSubnetRoutingTables.containsKey(privateSubnetRouteTableID)) {
				CfnRoute.Builder.create(this, "PrivateRouteToNAT" + i).routeTableId(privateSubnetRouteTableID)
						.destinationCidrBlock("0.0.0.0/0").natGatewayId(natgateway.getAttrNatGatewayId()).build();

				processedPrivateSubnetRoutingTables.put(privateSubnetRouteTableID, privateSubnetRouteTableID);
			}
		}
	}

	/**
	 * Output values from CDK to be used later.
	 */
	private void setupOutputVariables() {
		CfnOutput.Builder.create(this, "ALBURL").value("https://" + alb.getLoadBalancerDnsName()).build();
		CfnOutput.Builder.create(this, "JENKINS_USER_POOL_ID").value(userPool.getUserPoolId()).build();
		CfnOutput.Builder.create(this, "JENKINS_CLIENT_ID").value(userPoolClient.getUserPoolClientId()).build();
	}

	private void setEC2CommandList(List<String> commandList) {

		// download artifacts & setup folders
      commandList.add("sudo yum install -y git");
		commandList.add("cd /home/ec2-user");
		commandList.add("git clone https://github.com/EVikramss/ECOM.git");
		commandList.add("chmod -R 777 ECOM");

		// install needed softwares
		commandList.add("cd ./ECOM/setup/buildbox/scripts");
		commandList.add("./installSoftwares.sh");
		commandList.add("./setupBuildFolders.sh");
	}
}