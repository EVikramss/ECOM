package com.ecom.common;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SecurityGroupProps;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.AddCapacityOptions;
import software.amazon.awscdk.services.ecs.AsgCapacityProvider;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.EcsOptimizedImage;
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
import software.constructs.Construct;

public class CdkStack extends Stack {

	private Stack stack;

	private IVpc vpc;
	private InterfaceVpcEndpoint endpoint;
	private ISecurityGroup smepsg;
	private Bucket ecomBucket;
	private Function runDDLFunc;
	private ISecurityGroup runDDLFuncSG;
	private Cluster cluster;
	private Role ecsInstanceRole;
	private Repository ecrrepo;

	public CdkStack(final Construct scope, final String id, final StackProps props, Properties additionalProperties) {
		super(scope, id, props);
		stack = this;

		setupNetworking();
		setupCommonVPCEndpoints();
		setupS3();
		setupRunDDLLambda("/home/cloudshell-user/ECOM/modules/CommonModule/");
		setupECSCluster();

		setupOutputVariables();
	}

	private void setupECSCluster() {
		// create ECS role and add permissions to it
		ecsInstanceRole = Role.Builder.create(this, "EcsInstanceRole")
				.assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
				.managedPolicies(List
						.of(ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEC2ContainerServiceforEC2Role")))
				.build();

		// create ECS cluster
		cluster = Cluster.Builder.create(this, "ECOMECSCluster").vpc(vpc).clusterName("ECOMECSCluster").build();

		/*
		 * // create ASG for ECS & add to cluster AutoScalingGroup asg =
		 * AutoScalingGroup.Builder.create(this, "ECOMECSASG").vpc(vpc)
		 * .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
		 * .machineImage(EcsOptimizedImage.amazonLinux2()).minCapacity(2).maxCapacity(4)
		 * .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED)
		 * .build()) .associatePublicIpAddress(false).build();
		 * cluster.addAsgCapacityProvider(AsgCapacityProvider.Builder.create(this,
		 * "ECOMECSASGCapProv")
		 * .autoScalingGroup(asg).enableManagedTerminationProtection(true).build());
		 */

		cluster.addCapacity("DefaultASGCapacity",
				AddCapacityOptions.builder()
						.instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
						.autoScalingGroupName("ECOMECSASG").desiredCapacity(2).maxCapacity(4)
						.machineImage(EcsOptimizedImage.amazonLinux2())
						.vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
						.associatePublicIpAddress(false).canContainersAccessInstanceRole(true).spotPrice(null).build());

		// create ECR to hold docker images
		ecrrepo = Repository.Builder.create(this, "ecomrepo").repositoryName("ecomrepo").build();
		//ecrrepo.grantRead(cluster.getAutoscalingGroup());
		ecrrepo.grantRead(ecsInstanceRole);
	}

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

	private void setupNetworking() {
		// fetch ECOM vpc if already existing, otherwise create it
		vpc = Vpc.Builder.create(this, "ECOMVPC").vpcName("ECOMVPC")
				.subnetConfiguration(Arrays.asList(SubnetConfiguration.builder().cidrMask(24).name("PrivateSubnet")
						.subnetType(SubnetType.PRIVATE_ISOLATED).build()))
				.maxAzs(99) // Use all AZ's
				.enableDnsHostnames(true).enableDnsSupport(true).natGateways(0).build();
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
		CfnOutput.Builder.create(this, "RunDDLFUNCID").value(runDDLFunc.getFunctionArn()).build();
		CfnOutput.Builder.create(this, "RunDDLSGID").value(runDDLFuncSG.getSecurityGroupId()).build();
		CfnOutput.Builder.create(this, "RunDDLFUNCRLID").value(runDDLFunc.getRole().getRoleArn()).build();
		CfnOutput.Builder.create(this, "ECSARN").value(cluster.getClusterArn()).build();
		CfnOutput.Builder.create(this, "ECSROLE").value(ecsInstanceRole.getRoleArn()).build();
		CfnOutput.Builder.create(this, "ECRREPO").value(ecrrepo.getRepositoryUri()).build();
	}
}
