package com.fiserv.tos.cloud.initializer.service;

import com.fasterxml.jackson.core.JsonProcessingException;


public interface CloudInitalizrService {
     String getApplications(String workspaceID, String userId) throws JsonProcessingException;
}

