package com.ecom.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class CdkApp {

	private static final String STACK_NAME = "COMMON";
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
		
		checkEnvVariableNotEmpty("USERVPCID");
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
