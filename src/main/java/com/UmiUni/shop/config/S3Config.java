package com.UmiUni.shop.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

//    @Bean
//    public AmazonS3 s3client() {
//        AWSCredentials awsCredentials = new BasicAWSCredentials(
//                System.getenv("AWS_ACCESS_KEY_ID"),
//                System.getenv("AWS_SECRET_ACCESS_KEY")
//        );
//
//        System.out.println("AWS_ACCESS_KEY_ID: " + System.getenv("AWS_ACCESS_KEY_ID"));
//        System.out.println("AWS_SECRET_ACCESS_KEY: " + System.getenv("AWS_SECRET_ACCESS_KEY"));
//
//        return AmazonS3ClientBuilder
//                .standard()
//                .withRegion(region)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//    }

    @Bean
    public AmazonS3 s3client() {
        String accessKey = "AKIAVPHSJTQMAYMEYTWN";
        String secretKey = "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";

        if (accessKey == null || secretKey == null) {
            throw new IllegalArgumentException("AWS Access Key and Secret Key must be configured");
        }

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

}
