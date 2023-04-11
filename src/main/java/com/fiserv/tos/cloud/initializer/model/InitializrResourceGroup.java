package com.fiserv.tos.cloud.initializer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class InitializrResourceGroup implements Comparable<InitializrResourceGroup>{
    private String name;

    private Map<String, String> tags;

    public int compareTo(InitializrResourceGroup object) {
        return name.toLowerCase().compareTo(object.getName().toLowerCase());
    }

}
