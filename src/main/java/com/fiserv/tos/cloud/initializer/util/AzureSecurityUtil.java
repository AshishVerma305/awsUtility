package com.fiserv.tos.cloud.initializer.util;

import com.fiserv.tos.cloud.initializer.AzureUtilitiesApplication;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;


@AllArgsConstructor
@Getter
@Setter
@Component
public class AzureSecurityUtil {
    public  Azure getAzure(String subscriptionId) {
        ApplicationTokenCredentials credentials = getApplicationTokenCredentials();
        Azure azure = Azure.configure()
                .withLogLevel(LogLevel.NONE)
                .authenticate(credentials).withSubscription(subscriptionId);
        return azure;
    }
    public   Azure.Authenticated getAuthenticated() {
        ApplicationTokenCredentials credentials = getApplicationTokenCredentials();

        Azure.Authenticated azure = Azure.configure()
                .withLogLevel(LogLevel.NONE)
                .authenticate(credentials);
        return azure;
    }
    @NotNull
    public  ApplicationTokenCredentials getApplicationTokenCredentials() {
        ApplicationTokenCredentials credentials =
                new ApplicationTokenCredentials(
                        AzureUtilitiesApplication.CLIENT_ID,
                        AzureUtilitiesApplication.TENANT_ID,
                        AzureUtilitiesApplication.CLIENT_SECRET,
                        AzureEnvironment.AZURE);
        return credentials;
    }
}
