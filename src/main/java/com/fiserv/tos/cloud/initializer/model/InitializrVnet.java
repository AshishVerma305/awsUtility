package com.fiserv.tos.cloud.initializer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class InitializrVnet implements Comparable<InitializrVnet>{

    private String region;
    private String vnetName;
    private Map<String, String> tags;

    public int compareTo(InitializrVnet object) {
        return vnetName.toLowerCase().compareTo(object.getVnetName().toLowerCase());
    }

}
