package com.fiserv.tos.cloud.initializer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiserv.tos.cloud.initializer.config.MongoConfig;
import com.fiserv.tos.cloud.initializer.util.JsonDateTimeConverter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class CloudInitalizrServiceImpl implements CloudInitalizrService {
    @Autowired
    MongoConfig mongoConfig;

    @Autowired
    MongoClient mongoClient;


    public String getApplications(String workspaceId, String userId) throws JsonProcessingException {
        log.info("Connecting to database to fetch application details:" + workspaceId+"; clientdb="+mongoConfig.getClientDB() + "; collection="+mongoConfig.getMongoCollection());
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getClientDB());
        MongoCollection<Document> collection = database.getCollection(mongoConfig.getMongoCollection());
        Document query = new Document("artifactInformation.workspace", workspaceId).append("artifactInformation.userId",userId);
        FindIterable<Document> documents = collection.find(query).projection(Projections.fields(Projections.exclude("_id", "_class"))).limit(100);
        StringBuffer responseBuffer = new StringBuffer("[");
        for (Document document: documents){
            String json = document.toJson(JsonWriterSettings.builder().dateTimeConverter(new JsonDateTimeConverter()).build());
            if (responseBuffer.length() == 1) {
                responseBuffer.append(json);
            } else {
                responseBuffer.append(",").append(json);
            }
            log.info("Connecting to database to fetch application details");
        }
        return responseBuffer.append("]").toString();
    }
}
