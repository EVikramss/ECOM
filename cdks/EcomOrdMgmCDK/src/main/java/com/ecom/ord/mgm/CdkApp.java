package com.ecom.ord.mgm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class CdkApp {

	private static final String STACK_NAME = "ECOMORDMGM";
	private static String accountID = null;
	private static String defaultVPCID = null;
	private static String deploymentRegion = null;
	private static Properties additionalProperties = null;

	public static void main(final String[] args) throws Exception {
		init();
		createAndSynthStack();
	}

	private static void init() throws Exception {
		additionalProperties = new Properties();
		accountID = readAccountID();
		defaultVPCID = readDefaultVPCID();
		validate();
	}

	private static void createAndSynthStack() {

		App app = new App();

		// initialize stack
		StackProps properties = StackProps.builder()
				.env(Environment.builder().account(accountID).region(deploymentRegion).build()).build();
		new CdkStack(app, STACK_NAME, properties, additionalProperties);

		app.synth();
	}

	/**
	 * Method to read account id from shell
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String readAccountID() throws Exception {

		String account_id = null;
		String[] command = new String[] { "aws", "sts", "get-caller-identity", "--query", "\"Account\"", "--output",
				"text" };
		Process process = Runtime.getRuntime().exec(command);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			if ((line = reader.readLine()) != null) {
				account_id = line.trim();
			}
		}
		process.waitFor();
		additionalProperties.put("accountID", account_id);

		return account_id;
	}

	/**
	 * Get default vpc for current zone from shell
	 * 
	 * @return
	 * @throws Exception
	 */
	private static String readDefaultVPCID() throws Exception {

		String vpc_id = null;
		String[] command = new String[] { "aws", "ec2", "describe-vpcs", "--query",
				"\"Vpcs[?IsDefault==`true`].VpcId\"", "--output", "text" };
		Process process = Runtime.getRuntime().exec(command);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			if ((line = reader.readLine()) != null) {
				vpc_id = line.trim();
			}
		}
		process.waitFor();

		return vpc_id;
	}

	private static void validate() throws Exception {
		if (accountID == null || accountID.trim().length() == 0)
			throw new Exception(
					"No accountID. Set manually in code. Run : for /f \"delims=\" %i in ('aws sts get-caller-identity --query \"Account\" --output text') do set ACCOUNT_ID=%i");

		if (defaultVPCID == null || defaultVPCID.trim().length() == 0)
			throw new Exception(
					"No VPCID. Set manually in code. Run : for /f \"delims=\" %i in ('aws ec2 describe-vpcs --query \"Vpcs[?IsDefault==`true`].VpcId\" --output text') do set VPC_ID=%i");

		// get region from args, if not present default to us-east-1
		deploymentRegion = System.getenv("DEPLOYMENT_REGION");
		if (deploymentRegion == null || deploymentRegion.trim().length() == 0) {
			deploymentRegion = "us-east-1";
		}

		checkEnvVariableWithDefaults("CreateOrderMEM", 512);
		checkEnvVariableWithDefaults("CreateOrderCPU", 512);
		checkEnvVariableWithDefaults("ScheduleOrderMEM", 512);
		checkEnvVariableWithDefaults("ScheduleOrderCPU", 512);
		checkEnvVariableWithDefaults("ShipOrderMEM", 512);
		checkEnvVariableWithDefaults("ShipOrderCPU", 512);
		checkEnvVariableWithDefaults("GetDataMEM", 512);
		checkEnvVariableWithDefaults("GetDataCPU", 512);
		
		checkEnvVariableNotEmpty("VPCID");
		checkEnvVariableNotEmpty("SMEPID");
		checkEnvVariableNotEmpty("SMSGID");
		checkEnvVariableNotEmpty("ECOMBKTARN");
		checkEnvVariableNotEmpty("RunDDLFUNCID");
		checkEnvVariableNotEmpty("RunDDLSGID");
		checkEnvVariableNotEmpty("RunDDLFUNCRLID");
		checkEnvVariableNotEmpty("ECSARN");
		checkEnvVariableNotEmpty("ECSROLE");
		checkEnvVariableNotEmpty("ECRREPO");
		checkEnvVariableNotEmpty("ECSASGROLE");
		checkEnvVariableNotEmpty("ECSASGSG");
		checkEnvVariableNotEmpty("ECSNMSPARN");
		checkEnvVariableNotEmpty("ECSNMSPID");
		checkEnvVariableNotEmpty("ALBARN");
		checkEnvVariableNotEmpty("ALBSG");
	}

	private static void checkEnvVariableWithDefaults(String variableName, Integer defaultVariableValue) {
		Integer variableValue = defaultVariableValue;

		try {
			variableValue = Integer.parseInt(System.getenv(variableName));
		} catch (Exception e) {
		}

		additionalProperties.put(variableName, variableValue);
	}

	private static void checkEnvVariableNotEmpty(String variableName) throws Exception {
		String variableValue = System.getenv(variableName);
		if (variableValue == null || variableValue.trim().length() == 0)
			throw new Exception(variableName + " environment variable not found.");
	}
}
