package com.cloudx.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RegionController {

    @GetMapping("/")
    public Map<String, String> getRegionInfo() {
        Map<String, String> info = new HashMap<>();

        try {
            HttpClient client = HttpClient.newHttpClient();

            // First, get the token (IMDSv2)
            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://169.254.169.254/latest/api/token"))
                    .header("X-aws-ec2-metadata-token-ttl-seconds", "21600")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> tokenResponse = client.send(tokenRequest,
                    HttpResponse.BodyHandlers.ofString());
            String token = tokenResponse.body();

            // Get availability zone
            HttpRequest azRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://169.254.169.254/latest/meta-data/placement/availability-zone"))
                    .header("X-aws-ec2-metadata-token", token)
                    .build();

            HttpResponse<String> azResponse = client.send(azRequest,
                    HttpResponse.BodyHandlers.ofString());
            String az = azResponse.body();

            // Region is AZ without the last character (e.g., eu-north-1a -> eu-north-1)
            String region = az.substring(0, az.length() - 1);

            info.put("region", region);
            info.put("availabilityZone", az);

        } catch (Exception e) {
            info.put("error", e.getMessage());
        }

        return info;
    }
}