package com.dscjss.taskexecutor.config;

import com.dscjss.taskexecutor.model.Details;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "lang")
public class LangConfigProperties {

    private Map<String, Details> map = new HashMap<>();

    public Map<String, Details> getMap() {
        return map;
    }

    public void setMap(Map<String, Details> map) {
        this.map = map;
    }

}
