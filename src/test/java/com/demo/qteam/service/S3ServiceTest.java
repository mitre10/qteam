package com.demo.qteam.service;

import com.demo.qteam.config.S3Properties;
import com.demo.qteam.exception.ResourceNotFoundException;
import com.demo.qteam.model.ListResult;
import com.demo.qteam.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private final String BUCKET_NAME = "my-bucket";
    private final String RESOURCE_ID = "my-resource";
    private final String RESOURCE_NAME = "my-resource.txt";

    @Mock
    private S3Client s3Client;


    @Mock
    private S3Properties s3Properties;

    private S3Service service;

    private Resource testResource;

    @BeforeEach
    public void init() {
        service = new S3Service(s3Client, s3Properties);
        testResource = new Resource(RESOURCE_ID, RESOURCE_NAME, 0);
        when(s3Properties.getBucketName()).thenReturn(BUCKET_NAME);
    }

    @Test
    void test_getResource() {

        SdkHttpResponse httpResponse = SdkHttpResponse.builder()
                .statusCode(200)
                .build();

        HeadObjectResponse mockedResponse = (HeadObjectResponse) HeadObjectResponse.builder()
                .contentType("text/plain")
                .sdkHttpResponse(httpResponse)
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockedResponse);

        Resource resource = service.getResource(RESOURCE_NAME);

        assertNotNull(resource);
        assertEquals(RESOURCE_NAME, resource.getId());
        assertEquals(RESOURCE_NAME, resource.getName());
        assertEquals(0, resource.getType());
    }

    @Test
    void test_getResource_throwsException() {

        SdkHttpResponse httpResponse = SdkHttpResponse.builder()
                .statusCode(400)
                .build();

        HeadObjectResponse mockedResponse = (HeadObjectResponse) HeadObjectResponse.builder()
                .contentType("text/plain")
                .sdkHttpResponse(httpResponse)
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockedResponse);

        assertThrows(Exception.class, () -> {
            service.getResource(RESOURCE_NAME);
        });
    }

    @Test
    void test_getResource_throwsResourceNotFoundException() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getResource(RESOURCE_NAME);
        });
    }

    @Test
    void test_getFile() {
        File response = service.getFile(testResource);

        assertNotNull(response);
    }

    @Test
    void test_getFile_throwsNotSuchKeyException() {
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class))).thenThrow(NoSuchKeyException.class);

        assertThrows(ResourceNotFoundException.class, () -> {
            service.getFile(testResource);
        });
    }

    @Test
    void test_getFile_throwsException() {
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class))).thenThrow(AwsServiceException.class);

        assertThrows(Exception.class, () -> {
            service.getFile(testResource);
        });
    }

    @Test
    void test_listFolder() {
        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(RESOURCE_ID).build())
                .nextContinuationToken("2")
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(response);

        ListResult<Resource> result = service.listFolder(testResource, "1");

        assertEquals("2", result.getCursor());
        assertEquals(RESOURCE_ID, result.getResources().get(0).getName());
        assertEquals(RESOURCE_ID, result.getResources().get(0).getId());
        assertEquals(0, result.getResources().get(0).getType());
    }
}