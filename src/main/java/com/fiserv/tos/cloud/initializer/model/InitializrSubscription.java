package com.fiserv.tos.cloud.initializer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class InitializrSubscription implements Comparable<InitializrSubscription>{
    private String subscriptionId;
    private String subscriptionName;

    public int compareTo(InitializrSubscription object) {
        return subscriptionName.toLowerCase().compareTo(object.getSubscriptionName().toLowerCase());
    }

}
