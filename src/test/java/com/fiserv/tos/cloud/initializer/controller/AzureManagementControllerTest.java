package com.fiserv.tos.cloud.initializer.controller;
import com.fiserv.tos.cloud.initializer.util.AzureSecurityUtil;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
@Slf4j
@ExtendWith({MockitoExtension.class})
public class AzureManagementControllerTest {

    @Mock
    AzureSecurityUtil azureSecurityUtil;

    @Mock
    Azure.Authenticated azureAuthenticated;


    @Mock
    Subscriptions subscriptions ;

    @Mock
    PagedList<Subscription> subscriptionList;


    @Mock
    Subscription subscription1;
    @Mock
    Subscription subscription2;

    @InjectMocks
    AzureManagementController controller;

    @Test
    public void testAuthenticationFailure() throws Exception {
        when(this.azureSecurityUtil.getAuthenticated()).thenThrow(new RuntimeException("Authentication error"));
        String response = this.controller.getSubscriptions();
        System.out.println(response);
        assertTrue(response.contains("{\"error\":\"Error authenticating to Azure Authentication error\"}"));
    }

    @Test
    public void testGetSubscriptionEmptyResponse() throws Exception {
        when(this.azureSecurityUtil.getAuthenticated()).thenReturn(this.azureAuthenticated);
        when(this.azureAuthenticated.subscriptions()).thenReturn(subscriptions);
        when(this.subscriptions.list()).thenReturn(subscriptionList);
        when(this.subscriptionList.iterator()).thenReturn(new ArrayList<Subscription>().iterator());
        String response = this.controller.getSubscriptions();
        System.out.println(response);
        assertTrue(response.contains("[]"));
    }

    @Test
    public void testGetSubscriptions() throws Exception {
        when(this.subscription1.subscriptionId()).thenReturn("ID1");
        when(this.subscription1.displayName()).thenReturn("SUBSCRIPTION1");
        when(this.subscription2.subscriptionId()).thenReturn("ID2");
        when(this.subscription2.displayName()).thenReturn("SUBSCRIPTION2");

        ArrayList<Subscription> list = new ArrayList<Subscription>();
        list.add(this.subscription1);
        list.add(this.subscription2);
        when(this.azureSecurityUtil.getAuthenticated()).thenReturn(this.azureAuthenticated);
        when(this.azureAuthenticated.subscriptions()).thenReturn(subscriptions);
        when(this.subscriptions.list()).thenReturn(subscriptionList);
        when(this.subscriptionList.iterator()).thenReturn(list.iterator());
        String response = this.controller.getSubscriptions();
        assertTrue(response.contains("[{\"subscriptionId\":\"ID1\",\"subscriptionName\":\"SUBSCRIPTION1\"},{\"subscriptionId\":\"ID2\",\"subscriptionName\":\"SUBSCRIPTION2\"}]"));
    }
}
