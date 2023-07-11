package com.fiserv.tos.cloud.initializer.model;

import lombok.Data;

@Data
public class AwsResource {
    private String region;
    private String resourceArn;
    private String resourceType;
    private String accountName;
}
