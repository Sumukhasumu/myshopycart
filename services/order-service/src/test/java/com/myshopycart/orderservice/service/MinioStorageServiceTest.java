
package com.myshopycart.orderservice.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
        "minio.endpoint=${MYSHOPYCART_TEST_MINIO_ENDPOINT:http://localhost:9100}",
        "minio.access-key=myshopycart",
        "minio.secret-key=myshopycart",
        "minio.bucket=myshopycart-invoices",
        "minio.initialize-bucket=false",
        "INVENTORY_SERVICE_INTERNAL_TOKEN=test-inventory-token"
})
class MinioStorageServiceTest {

    private static final String BUCKET_NAME = "myshopycart-invoices";

    private static final String INVOICE_NUMBER =
            "INV-TEST-2026-000001";

    private static final byte[] PDF_BYTES =
            "%PDF-1.4 MYSHOPYCART TEST INVOICE"
                    .getBytes(StandardCharsets.UTF_8);

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("myshopycart_order_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @Autowired
    private MinioStorageService minioStorageService;

    @Autowired
    private MinioClient minioClient;

    @BeforeAll
    static void createInvoiceBucket() throws Exception {
        String endpoint = System.getenv().getOrDefault(
                "MYSHOPYCART_TEST_MINIO_ENDPOINT",
                "http://localhost:9100"
        );

        MinioClient testClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials("myshopycart", "myshopycart")
                .build();

        boolean bucketExists = testClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .build()
        );

        if (!bucketExists) {
            testClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(BUCKET_NAME)
                            .build()
            );
        }
    }

    @Test
    void shouldStoreInvoicePdf() throws Exception {
        String objectName =
                minioStorageService.storeInvoicePdf(
                        INVOICE_NUMBER,
                        PDF_BYTES
                );

        assertEquals(
                "invoices/" + INVOICE_NUMBER + ".pdf",
                objectName
        );

        assertDoesNotThrow(() ->
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(BUCKET_NAME)
                                .object(objectName)
                                .build()
                )
        );
    }

    @Test
    void shouldRetrieveInvoicePdf() throws Exception {
        minioStorageService.storeInvoicePdf(
                INVOICE_NUMBER,
                PDF_BYTES
        );

        try (InputStream inputStream =
                     minioStorageService.getInvoicePdf(INVOICE_NUMBER)) {

            byte[] retrievedBytes = inputStream.readAllBytes();

            assertArrayEquals(PDF_BYTES, retrievedBytes);
        }
    }

    @Test
    void shouldGenerateTemporaryDownloadUrl() throws Exception {
        minioStorageService.storeInvoicePdf(
                INVOICE_NUMBER,
                PDF_BYTES
        );

        String downloadUrl =
                minioStorageService.generateDownloadUrl(INVOICE_NUMBER);

        assertNotNull(downloadUrl);
        assertFalse(downloadUrl.isBlank());

        String expectedPrefix = minioEndpoint.endsWith("/")
                ? minioEndpoint
                : minioEndpoint + "/";

        assertTrue(
                downloadUrl.startsWith(expectedPrefix),
                "Download URL should start with configured MinIO endpoint"
        );
    }

    @Test
    void shouldRejectBlankInvoiceNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> minioStorageService.storeInvoicePdf(
                        "",
                        PDF_BYTES
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> minioStorageService.getInvoicePdf("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> minioStorageService.generateDownloadUrl("")
        );
    }

    @Test
    void shouldRejectEmptyPdfContent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> minioStorageService.storeInvoicePdf(
                        INVOICE_NUMBER,
                        new byte[0]
                )
        );
    }
}
