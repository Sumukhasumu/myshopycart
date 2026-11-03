
package com.myshopycart.orderservice.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "minio.initialize-bucket",
            havingValue = "true",
            matchIfMissing = true
    )
    public CommandLineRunner initializeMinioBucket(
            MinioClient minioClient) {

        return args -> {
            try {
                boolean bucketExists = minioClient.bucketExists(
                        BucketExistsArgs.builder()
                                .bucket(bucket)
                                .build()
                );

                if (!bucketExists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder()
                                    .bucket(bucket)
                                    .build()
                    );
                }
            } catch (Exception exception) {
                throw new IllegalStateException(
                        "Unable to initialize MinIO bucket: " + bucket,
                        exception
                );
            }
        };
    }
}