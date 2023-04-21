package com.fiserv.tos.cloud.initializer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.tos.cloud.initializer.model.*;
import com.fiserv.tos.cloud.initializer.util.AzureSecurityUtil;
import com.microsoft.azure.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
public class AzureManagementController {

    private AzureSecurityUtil azureSecurityUtil;

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
        ArrayList<InitializrResourceGroup> list = new ArrayList<InitializrResourceGroup>();
        for (ResourceGroup resourceGroup : azure.resourceGroups().list()){
            list.add(new InitializrResourceGroup(resourceGroup.name(), resourceGroup.tags()));
        }
        Collections.sort(list);
        return jsonMessage(list);
    }

    @GetMapping("/resources")
    public String getResources(@RequestParam(name = "subscriptionid") String subscriptionId,@RequestParam(name = "resourceGroupName") String resourceGroupName,@RequestParam(name = "resourceName") String resourceName) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }

        ResourceGroup resourceGroup = azure.resourceGroups().getByName(resourceGroupName);
        if (resourceGroup == null) {
            System.out.printf("Resource group '%s' not found.%n", resourceGroupName);
            return "x";
        }

        ResourceId resourceId = ResourceId.fromString(String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Compute/virtualMachines/%s", subscriptionId, resourceGroupName, resourceName));
        GenericResource resource = azure.genericResources().getById(resourceId.toString());
        if (resource == null) {
            System.out.printf("Resource '%s' not found in resource group '%s'.%n", resourceName, resourceGroupName);
            return "x";
        }

        System.out.printf("Resource '%s' (%s) found in resource group '%s'.%n", resourceName, resourceId.toString(), resourceGroupName);
        return "x";
    }


    @GetMapping("/getAllResources")
    public String getAllResources(@RequestParam(name = "subscriptionid") String subscriptionId,@RequestParam(name = "resourceGroupName") String resourceGroupName) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }

        ResourceGroup resourceGroup = azure.resourceGroups().getByName(resourceGroupName);
        if (resourceGroup == null) {
            System.out.printf("Resource group '%s' not found.%n", resourceGroupName);
            return "x";
        }

        System.out.printf("Resource group '%s' found with ID: %s%n", resourceGroupName, resourceGroup.id());

        List<GenericResource> resources = azure.genericResources().listByResourceGroup(resourceGroupName);
        List resourceList=new ArrayList();
        if (resources == null || resources.isEmpty()) {
            System.out.printf("No resources found in resource group '%s'.%n", resourceGroupName);
        } else {
            System.out.printf("Resources in resource group '%s':%n", resourceGroupName);
            for (GenericResource resource : resources) {
                ResourceId resourceId = ResourceId.fromString(resource.id());
                System.out.println(resourceId);
                Resources resourcesData=new Resources();
                resourcesData.setResourceId(resourceId.toString());
                resourcesData.setResourceName(resourceId.name());
                resourcesData.setResourceType(resourceId.resourceType());
                resourceList.add(resourcesData);
                System.out.printf("- Resource type: %s, Name: %s, ID: %s%n", resourceId.resourceType(), resourceId.name(), resourceId.toString());
            }
        }
        return jsonMessage(resourceList);
    }
    @GetMapping("/getVms")
    public String getAllVMS(@RequestParam(name = "subscriptionid") String subscriptionId, @RequestParam(name = "resourceGroupName") String resourceGroupName, Model model) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }

        ResourceGroup resourceGroup = azure.resourceGroups().getByName(resourceGroupName);
        if (resourceGroup == null) {
            System.out.printf("Resource group '%s' not found.%n", resourceGroupName);
            return "x";
        }

        System.out.printf("Resource group '%s' found with ID: %s%n", resourceGroupName, resourceGroup.id());

        List<GenericResource> resources = azure.genericResources().listByResourceGroup(resourceGroupName);
        List resourceList=new ArrayList();
        if (resources == null || resources.isEmpty()) {
            System.out.printf("No resources found in resource group '%s'.%n", resourceGroupName);
        } else {
            System.out.printf("Resources in resource group '%s':%n", resourceGroupName);
            for (GenericResource resource : resources) {
                ResourceId resourceId = ResourceId.fromString(resource.id());
                System.out.println(resourceId);
                if(resourceId.resourceType().equalsIgnoreCase("virtualMachines"))
                {
                    Resources resourcesData=new Resources();
                    resourcesData.setResourceId(resourceId.toString());
                    resourcesData.setResourceName(resourceId.name());
                    resourcesData.setResourceType(resourceId.resourceType());
                    resourceList.add(resourcesData);
                    model.addAttribute("subscriptionId",subscriptionId);
                    model.addAttribute("resourceGroupName",resourceGroupName);
                    model.addAttribute("resource",resourceId.name());
                    break;
                }
                System.out.printf("- Resource type: %s, Name: %s, ID: %s%n", resourceId.resourceType(), resourceId.name(), resourceId.toString());
            }
        }
        return "chaosAzureVmYaml";
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
