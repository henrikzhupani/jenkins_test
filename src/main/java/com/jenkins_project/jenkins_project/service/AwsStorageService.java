package com.jenkins_project.jenkins_project.service;


import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jenkins_project.jenkins_project.component.AwsStorageComponent;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class AwsStorageService {

    private final AmazonS3 space;
    private final SqsClient sqsClient;
    private final AwsStorageComponent awsStorageComponent;

    public AwsStorageService(AwsStorageComponent awsStorageComponent) {
        this.awsStorageComponent = awsStorageComponent;

        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(this.awsStorageComponent.getAccessKey(), this.awsStorageComponent.getSecretKey())
        );

        space = AmazonS3ClientBuilder
                .standard()
                .withRegion(awsStorageComponent.getSigningRegion())
                .withCredentials(awsCredentialsProvider)
                .build();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(this.awsStorageComponent.getAccessKey(), this.awsStorageComponent.getSecretKey());

        sqsClient = SqsClient.builder()
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
    /**
     * Method to get the name of the files inside the Amazon S3 storage
     *
     * @return
     */

    public List<String> getFileNames() {

        ListObjectsV2Result result = space.listObjectsV2(awsStorageComponent.getBucketName());
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        return objects.stream()
                .map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    /**
     * Method to upload a new file inside of Amazon S3 storage
     *
     * @param file
     * @param key
     * @return
     * @throws IOException
     */
    public String uploadFiles(MultipartFile file, String key) throws IOException {

        //setting the type of the content that we are going to save
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());


        //adding to the storage the object with PUBLIC = true
        space.putObject(
                new PutObjectRequest(awsStorageComponent.getBucketName(), key, file.getInputStream(), objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        //returning the URL of the file so we can access the file from the url in database
        S3Object s3Object = space.getObject(awsStorageComponent.getBucketName(), key);
        return s3Object.getObjectContent().getHttpRequest().getURI().toString();

    }

    public void sendMessage(MultipartFile file) {
        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl(this.awsStorageComponent.getQueueUrl())
                .messageBody(file.getName())
                .build();
        sqsClient.sendMessage(messageRequest);
    }

}
