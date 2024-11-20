package com.hexpanda.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "gateway.whitelist")
@Component
public class AuthWhitelistConfig {
    private List<String> allowUrls;
}
