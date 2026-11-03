package com.myshopycart.orderservice.service;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Stores an invoice PDF in MinIO.
     *
     * @param invoiceNumber invoice number used as the PDF filename
     * @param pdfBytes       generated PDF content
     * @return object name stored in MinIO
     */
    public String storeInvoicePdf(String invoiceNumber, byte[] pdfBytes) {

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number must not be blank");
        }

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF content must not be empty");
        }

        String objectName = buildObjectName(invoiceNumber);

        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes)) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    pdfBytes.length,
                                    -1
                            )
                            .contentType("application/pdf")
                            .build()
            );

            return objectName;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to store invoice PDF in MinIO: " + invoiceNumber,
                    exception
            );
        }
    }

    /**
     * Retrieves an invoice PDF from MinIO.
     *
     * @param invoiceNumber invoice number
     * @return PDF input stream
     */
    public InputStream getInvoicePdf(String invoiceNumber) {

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number must not be blank");
        }

        String objectName = buildObjectName(invoiceNumber);

        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to retrieve invoice PDF from MinIO: " + invoiceNumber,
                    exception
            );
        }
    }

    /**
     * Generates a temporary presigned URL for an invoice PDF.
     *
     * The bucket remains private. The generated URL provides temporary
     * access to the specific invoice object.
     *
     * @param invoiceNumber invoice number
     * @return temporary presigned URL
     */
    public String generateDownloadUrl(String invoiceNumber) {

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number must not be blank");
        }

        String objectName = buildObjectName(invoiceNumber);

        try {

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to generate invoice download URL: " + invoiceNumber,
                    exception
            );
        }
    }

    private String buildObjectName(String invoiceNumber) {
        return "invoices/" + invoiceNumber + ".pdf";
    }
}