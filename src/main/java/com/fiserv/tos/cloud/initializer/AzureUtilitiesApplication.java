package com.fiserv.tos.cloud.initializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzureUtilitiesApplication {

	public static String TENANT_ID;
	public static String CLIENT_ID;
	public static String CLIENT_SECRET;

	public static void main(String[] args) {
		CLIENT_ID = System.getenv("PARAM_CL_ID");
		TENANT_ID = System.getenv("PARAM_TE_ID");
		CLIENT_SECRET = System.getenv("PARAM_CL_SE");
		SpringApplication.run(AzureUtilitiesApplication.class, args);
	}

}
