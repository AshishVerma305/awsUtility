package com.fiserv.tos.cloud.initializer.util;

import lombok.extern.slf4j.Slf4j;
import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class JsonDateTimeConverter implements Converter<Long> {


    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT
        .withZone(ZoneId.of("UTC"));

    @Override
    public void convert(Long value, StrictJsonWriter writer) {
        try {
            Instant instant = new Date(value).toInstant();
            String s = DATE_TIME_FORMATTER.format(instant);
            writer.writeString(s);
        } catch (Exception e) {
            log.error("Connecting to database to fetch application details"+e.getMessage());
        }
    }
}