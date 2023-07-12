package com.fiserv.tos.cloud.initializer.model;

import lombok.Data;

@Data
public class AwsResource {
    private String accountName;
    private String resourceId;
    private String region;
    private String resourceType;
    private int volumeId;
    private String instanceType;
    private String hypothesisEndpoint;

}
