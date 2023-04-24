package com.fiserv.tos.cloud.initializer.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component("artifactTemplateRenderer")
public class TemplateRenderer {

    private final Compiler mustache;
    private final ConcurrentMap<String, Template> templateCaches = new ConcurrentReferenceHashMap<>();

    public TemplateRenderer() {
        this(mustacheCompiler());
    }

    public TemplateRenderer(Compiler mustache) {
        this.mustache = mustache;
    }

    private static Compiler mustacheCompiler() {
        return Mustache.compiler().withLoader(mustacheTemplateLoader());
    }

    private static TemplateLoader mustacheTemplateLoader() {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String prefix = "classpath:/templates/";
//        String prefix = "file:C:\\cloudaccelerator\\blueprints\\";
//        String prefix = "file:////cloudaccelerator/blueprints/";
//        String prefix = "file:C:\\Users\\jkacha\\devops\\dtt-terraform-blueprints\\blueprints\\";
        return name -> new InputStreamReader(resourceLoader.getResource(prefix + name).getInputStream(), UTF_8);
        
    }

    public String process(String name, Object model) {
        try {
            Template template = getTemplate(name);
            return template.execute(model);
        } catch (MustacheException ex) {
            log.error("Cannot render: " + name, ex);
            throw new IllegalStateException("Cannot render template", ex);
        }
    }

    private Template getTemplate(String name) {
        return this.templateCaches.computeIfAbsent(name, this::loadTemplate);
    }
    
    public void refreshTemplate(String name) {
    	Template template = this.loadTemplate(name);
    	this.templateCaches.put(name, template);
    }
    

    private Template loadTemplate(String name) {
        try {
            Reader template;
            template = this.mustache.loader.getTemplate(name);
            return this.mustache.compile(template);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot load template " + name, ex);
        }
    }
    public String generateFile(String source, Path destination, Object modelObject) throws IOException {
        Map<String, Object> model = new ObjectMapper().convertValue(modelObject,
                new TypeReference<Map<String, Object>>() {
                });
        String generatedFile = process(source, model);

        FileUtils.writeStringToFile(destination.toFile(), generatedFile, Charset.defaultCharset());

        return generatedFile;
    }
}
