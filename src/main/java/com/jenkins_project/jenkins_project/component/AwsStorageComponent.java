package com.jenkins_project.jenkins_project.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("amazon.s3")
@Getter
@Setter
public class AwsStorageComponent {

    private String accessKey;
    private String secretKey;
    private String serviceEndpoint;
    private String signingRegion;
    private String bucketName;
    private String queueUrl;

}
