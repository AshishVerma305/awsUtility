package com.fiserv.tos.cloud.initializer.controller;

import com.amazonaws.arn.Arn;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.connect.model.DescribeInstanceRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.tos.cloud.initializer.model.*;
import com.fiserv.tos.cloud.initializer.util.AzureSecurityUtil;
import com.fiserv.tos.cloud.initializer.util.TemplateRenderer;
import com.microsoft.azure.Resource;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.network.TopologyParameters;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import com.amazonaws.services.resourcegroupstaggingapi.AWSResourceGroupsTaggingAPI;
import com.amazonaws.services.resourcegroupstaggingapi.AWSResourceGroupsTaggingAPIClientBuilder;
import com.amazonaws.services.resourcegroupstaggingapi.model.GetResourcesRequest;
import com.amazonaws.services.resourcegroupstaggingapi.model.GetResourcesResult;
import com.amazonaws.services.resourcegroupstaggingapi.model.ResourceTagMapping;

@RestController
@AllArgsConstructor
public class AzureManagementController {

    private AzureSecurityUtil azureSecurityUtil;

    @Autowired
    @Qualifier("artifactTemplateRenderer")
    private TemplateRenderer templateRenderer;

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
        NetworkWatcher networkWatcher=azure.networkWatchers().getByResourceGroup(resourceGroupName,"NetworkWatcher_centralus");
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
//                resourcesData.setResourceId(resourceId.toString());
                resourcesData.setResource(resourceId.name());
//                resourcesData.setResourceType(resourceId.resourceType());
                resourceList.add(resourcesData);
                System.out.printf("- Resource type: %s, Name: %s, ID: %s%n", resourceId.resourceType(), resourceId.name(), resourceId.toString());
            }
        }
        return jsonMessage(resourceList);
    }
    @GetMapping("/getVms")
    public String getAllVMS(@RequestParam(name = "subscriptionid") String subscriptionId, @RequestParam(name = "resourceGroupName") String resourceGroupName,Map<String, Object> model) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }

        ResourceGroup resourceGroup = azure.resourceGroups().getByName(resourceGroupName);
        if (resourceGroup == null) {
            System.out.printf("Resource group '%s' not found.%n", resourceGroupName);
            return jsonMessage(new ErrorMessage("Resource group not found"));
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
                    try {
                        Resources resourcesData=new Resources();
                        resourcesData.setSubscriptionId(subscriptionId);
                        resourcesData.setResource(resourceId.name());
                        resourcesData.setResourceGroupName(resourceGroupName);
                        templateRenderer.generateFile("chaosAzureVmYaml.yaml", Paths.get("src/main/resources/templates/"+resourceId.name()+resourceId.resourceType()+".yaml"),resourcesData);
                        resourceList.add(resourcesData);
                        model.put("subscriptionId",subscriptionId);
                        model.put("resourceGroupName",resourceGroupName);
                        model.put("resource",resourceId.name());
                    }catch (Exception e)
                    {
                        return jsonMessage(new ErrorMessage("Mustache Exception"));
                    }
                }
                System.out.printf("- Resource type: %s, Name: %s, ID: %s%n", resourceId.resourceType(), resourceId.name(), resourceId.toString());
            }
        }
        return jsonMessage(resourceList);
    }

    @GetMapping("/networkWatcher")
    public String getnetworkWatcher(@RequestParam(name = "subscriptionid") String subscriptionId,@RequestParam(name = "resourceGroupName") String resourceGroupName,@RequestParam(name = "networkWatcherName") String networkWatcherName) {
        Azure azure = null;
        try {
            azure = this.azureSecurityUtil.getAzure(subscriptionId);
        } catch (Exception e) {
            return jsonMessage(new ErrorMessage("Error accessing subscription:"+e.getMessage()));
        }
        List<NetworkWatcher> networkWatcherList =azure.networkWatchers().list();
        System.out.println(networkWatcherList);
        NetworkWatcher networkWatcher=azure.networkWatchers().getByResourceGroup(resourceGroupName,networkWatcherName);
        System.out.println(networkWatcher);
        Topology topology=networkWatcher.topology().withTargetResourceGroup(resourceGroupName).execute();
        System.out.println(topology);
        return "x";
    }


    @RequestMapping(value = "/listResources", method = RequestMethod.POST)
    @ResponseBody
    public void listResources(Map<String, Object> model)
    {
        for (Regions region : Regions.values()) {
            try {
                AWSResourceGroupsTaggingAPI taggingAPI = AWSResourceGroupsTaggingAPIClientBuilder.standard()
                        .withRegion(region)
                        .build();

                GetResourcesRequest request = new GetResourcesRequest();
                GetResourcesResult result = taggingAPI.getResources(request);

                System.out.println("Region: " + region.getName());
                for (ResourceTagMapping resource : result.getResourceTagMappingList()) {
                    System.out.println("resources: "+resource);
                    System.out.println("ResourceARN: " + resource.getResourceARN());
                    System.out.println("ResourceType: " + getResourceTypeFromARN(resource.getResourceARN()));
                    System.out.println("Tags: " + resource.getTags());
                    AwsResource awsResource=new AwsResource();
                    awsResource.setResourceId(getResourceIdFromARN(resource.getResourceARN()));
                    awsResource.setRegion(region.getName());
                    awsResource.setResourceType(getResourceTypeFromARN(resource.getResourceARN()));
                    awsResource.setAccountName(getAccountFromARN(resource.getResourceARN()));
                    awsResource.setVolumeId(1);
                    awsResource.setInstanceType("t2-micro");
                    awsResource.setHypothesisEndpoint("https://google.com");
                    if(awsResource.getResourceType().equalsIgnoreCase("ec2")&&getResourceFromARN(resource.getResourceARN()).equalsIgnoreCase("instance"))
                    {
                        AmazonEC2 amazonEC2Client= AmazonEC2ClientBuilder.standard().withRegion(region).build();
                        DescribeInstancesRequest describeInstance= new DescribeInstancesRequest();
                        List<String> idCollection=new ArrayList<>();
                        idCollection.add(getResourceIdFromARN(resource.getResourceARN()));
                        describeInstance.setInstanceIds(idCollection);
                        DescribeInstancesResult describeInstancesResult=amazonEC2Client.describeInstances(describeInstance);
                        System.out.println("describeInstancesResult------>"+describeInstancesResult);
                        templateRenderer.generateFile("chaosAwsTest.yaml", Paths.get("src/main/resources/templates/"+region.getName()+getResourceIdFromARN(resource.getResourceARN())+getResourceFromARN(resource.getResourceARN())+getAccountFromARN(resource.getResourceARN())+".yaml"),awsResource);
                        System.out.println("------------------->"+"rendered test yaml");
                        model.put("accountName",awsResource.getAccountName());
                        System.out.println("accountName"+awsResource.getAccountName());

                        model.put("resourceId",awsResource.getResourceId());
                        System.out.println("resourceId"+getResourceIdFromARN(awsResource.getResourceId()));

                        model.put("region",region.getName());
                        System.out.println("region"+region.getName());

                        model.put("resourceType",awsResource.getResourceType());
                        System.out.println("resourceType"+awsResource.getResourceType());

                        model.put("volumeId",awsResource.getVolumeId());
                        System.out.println("volumeId"+1);

                        model.put("instanceType",awsResource.getInstanceType());
                        System.out.println("instanceType"+"t2.micro");

                        model.put("hypothesisEndpoint",awsResource.getHypothesisEndpoint());
                        System.out.println("hypothesisEndpoint"+"https://google.com");
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to retrieve resources in region " + region.getName() + ": " + e.getMessage());
            }
        }
    }

    @GetMapping("/help")
    public void getResources(Map<String, Object> model)
    {
        AwsResource awsResource=new AwsResource();
        awsResource.setAccountName("249374488161");
        awsResource.setResourceId("i-0ee26ee1ec5e30801");
        awsResource.setResourceType("ec2");
        awsResource.setRegion("us-east-2");
        awsResource.setVolumeId(1);
        awsResource.setInstanceType("t2-micro");
        awsResource.setHypothesisEndpoint("https://google.com");
        try {
            templateRenderer.generateFile("chaosAwsTest.yaml", Paths.get("src/main/resources/templates/"+"us-east-2"+"i-0ee26ee1ec5e30801"+".yaml"),awsResource);
            model.put("accountName",awsResource.getAccountName());
            System.out.println("accountName"+awsResource.getAccountName());

            model.put("resourceId",awsResource.getResourceId());
            System.out.println("resourceId"+awsResource.getResourceId());

            model.put("region","us-east-2");
            System.out.println("region"+"us-east-2");

            model.put("resourceType",awsResource.getResourceType());
            System.out.println("resourceType"+awsResource.getResourceType());

            model.put("volumeId",awsResource.getVolumeId());
            System.out.println("volumeId"+1);

            model.put("instanceType",awsResource.getInstanceType());
            System.out.println("instanceType"+"t2.micro");

            model.put("hypothesisEndpoint",awsResource.getHypothesisEndpoint());
            System.out.println("hypothesisEndpoint"+awsResource.getHypothesisEndpoint());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getResourceTypeFromARN(String arn) {
        // Assuming the resource type is the 3rd component in the ARN
        String[] arnComponents = arn.split(":");
        if (arnComponents.length >= 3) {
            return arnComponents[2];
        }
        return "";
    }

    private static String getResourceIdFromARN(String arn) {
        // Assuming the resource type is the 3rd component in the ARN
        String[] arnComponents = arn.split(":");
        if (arnComponents.length >= 3) {
            return arnComponents[5].split("/")[1];
        }
        return "";
    }
    private static String getResourceFromARN(String arn) {
        // Assuming the resource type is the 3rd component in the ARN
        String[] arnComponents = arn.split(":");
        if (arnComponents.length >= 3) {
            return arnComponents[5].split("/")[0];
        }
        return "";
    }

    private static String getAccountFromARN(String arn) {
        // Assuming the resource type is the 3rd component in the ARN
        String[] arnComponents = arn.split(":");
        if (arnComponents.length >= 3) {
            return arnComponents[4];
        }
        return "";
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
