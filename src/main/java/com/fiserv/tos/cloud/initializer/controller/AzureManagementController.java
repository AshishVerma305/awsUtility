package com.fiserv.tos.cloud.initializer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.tos.cloud.initializer.model.ErrorMessage;
import com.fiserv.tos.cloud.initializer.model.InitializrResourceGroup;
import com.fiserv.tos.cloud.initializer.model.InitializrSubscription;
import com.fiserv.tos.cloud.initializer.model.InitializrVnet;
import com.fiserv.tos.cloud.initializer.service.CloudInitalizrService;
import com.fiserv.tos.cloud.initializer.util.AzureSecurityUtil;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fiserv.tos.cloud.initializer.AzureUtilitiesApplication;

import java.util.ArrayList;
import java.util.Collections;

@RestController
@AllArgsConstructor
public class AzureManagementController {

    private AzureSecurityUtil azureSecurityUtil;

    @Autowired
    private CloudInitalizrService cloudInitalizrService;

    @GetMapping("/subscriptions")
    public String getSubscriptions() {
        Azure.Authenticated azure = null;
        try {
            azure = this.azureSecurityUtil.getAuthenticated();
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error authenticating to Azure "+e.getMessage()));
        }
        // Get the list of subscriptions
        ArrayList<InitializrSubscription> list = new ArrayList<InitializrSubscription>();
        for (Subscription subscription : azure.subscriptions().list()) {
            list.add(new InitializrSubscription(subscription.subscriptionId(), subscription.displayName()));
        }
        Collections.sort(list);
        return jsonMessage(list);
    }

    @GetMapping("/vnets")
    public String getVnets(@RequestParam(name = "subscriptionid") String subscriptionId) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);

        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }

        // Get the list of subscriptions
        ArrayList<InitializrVnet> list = new ArrayList<InitializrVnet>();
        for (Network network : azure.networks().list()) {
            list.add(new InitializrVnet(network.regionName(),network.name(), network.tags()));
        }
        Collections.sort(list);
        return jsonMessage(list);
    }


    @GetMapping("/resourcegroups")
    public String getResourceGroups(@RequestParam(name = "subscriptionid") String subscriptionId) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }
        // Get the list of subscriptions
        ArrayList<InitializrResourceGroup> list = new ArrayList<InitializrResourceGroup>();
        for (ResourceGroup resourceGroup : azure.resourceGroups().list()){
            list.add(new InitializrResourceGroup(resourceGroup.name(), resourceGroup.tags()));
        }
        Collections.sort(list);
        return jsonMessage(list);
    }

    @GetMapping("/getApplications")
    public String getApplications(@RequestParam(name = "workspaceid") String workspaceid, @RequestParam(name = "userid") String userid) {
        try {
            return  cloudInitalizrService.getApplications(workspaceid, userid);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }
    }


    private static String jsonMessage(Object error){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
