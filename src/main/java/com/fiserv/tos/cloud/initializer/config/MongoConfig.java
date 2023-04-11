package com.fiserv.tos.cloud.initializer.config;

import com.fiserv.tos.cloud.initializer.AzureUtilitiesApplication;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class MongoConfig {

    @Value("${spring.data.mongodb.host}")
    String mongoHost;
    @Value("${spring.data.mongodb.port}")
    String mongoPort;


    @Value("${spring.data.mongodb.username}")
    String mongoUser;
    @Value("${spring.data.mongodb.password}")
    String mongoPassword;
    @Value("${spring.data.mongodb.database}")
    String mongoDB;

    @Value("${spring.data.mongodb.params}")
    String mongoParams;

    @Value("${spring.data.mongodb.clientdb}")
    String clientDB;

    @Value("${spring.data.mongodb.collections}")
    String mongoCollection;
    @Bean
    public MongoClient mongoClient(){
        String connectionString="mongodb://"+ mongoUser+":"+mongoPassword+"@"+mongoHost+":"+mongoPort+"/?"+mongoParams;
        return MongoClients.create(connectionString);
    }
}
