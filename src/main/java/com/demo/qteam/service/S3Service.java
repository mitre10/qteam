package com.demo.qteam.service;

import com.demo.qteam.config.S3Properties;
import com.demo.qteam.exception.ResourceNotFoundException;
import com.demo.qteam.model.ListResult;
import com.demo.qteam.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.apache.tomcat.util.http.fileupload.FileUtils.cleanDirectory;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final static Logger LOG = LoggerFactory.getLogger(S3Service.class);

    public S3Service(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    /**
     * Retrieves a resource based on its ID.
     *
     * @param id the ID of the resource (S3 object key)
     * @return the Resource object representing the S3 object
     * @throws ResourceNotFoundException if the S3 object key is not found
     */
    public Resource getResource(String id) {
        LOG.info("Trying to fetch resource with ID: {}", id);

        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(id)
                .build();
        try {
            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

            if (200 == headObjectResponse.sdkHttpResponse().statusCode()) {
                String name = id.substring(id.lastIndexOf('/') + 1);
                int type = id.endsWith("/") ? 1 : 0;

                Resource resource = new Resource(id, name, type);
                LOG.info("Successfully retrieved resource.");
                return resource;
            } else {
                LOG.error("S3 Client response status: {}.", headObjectResponse.sdkHttpResponse().statusCode());
                throw new Exception("S3 Client did not respond with correct HTTP status. See logs for more details.");
            }

        } catch (NoSuchKeyException e) {
            LOG.error("Object key {} not found. Logs: {}", id, e.getMessage());
            throw new ResourceNotFoundException("Resource not found in S3 bucket.");
        } catch (Exception e) {
            LOG.error("Error fetching resource with ID {}. Logs: {}", id, e.getMessage());
            throw new RuntimeException("An error occurred while fetching the resource.");
        }
    }

    /**
     * Retrieves an object from the specified S3 bucket and saves it to a temporary file.
     *
     * @param resource the resource representing the S3 object to be retrieved
     * @return the file containing the retrieved S3 object
     * @throws ResourceNotFoundException if the S3 object key is not found
     * @throws RuntimeException          if an error occurs while creating the file or retrieving the object
     */
    public File getFile(Resource resource) {
        LOG.info("Trying to fetch object for resource: {}", resource);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(resource.getId())
                .build();

        File file;
        try {
            Path tempDir = Paths.get("temp");
            cleanDirectory(tempDir.toFile());

            file = new File(tempDir.toFile(), resource.getName());

            s3Client.getObject(getObjectRequest, ResponseTransformer.toFile(file.toPath()));
            LOG.info("Successfully wrote object to file.");
        } catch (NoSuchKeyException e) {
            LOG.error("The specified key does not exist. Logs: {}", e.getMessage());
            throw new ResourceNotFoundException("The specified key does not exist.");
        } catch (IOException e) {
            LOG.error("Error creating file. Logs: {}", e.getMessage());
            throw new RuntimeException("Error creating file. Please check the logs.");
        } catch (Exception e) {
            LOG.error("Something went wrong. Logs: {}", e.getMessage());
            throw new RuntimeException("Something went wrong. Please check the logs.");
        }

        return file;
    }

    /**
     * Lists the contents of a given resource.
     * Supports pagination.
     *
     * @param resource The resource for which you list the contents (could be null).
     * @param cursor   The pagination cursor.
     * @return ListResult containing the list of resources and the next pagination cursor.
     */
    public ListResult<Resource> listFolder(Resource resource, String cursor) {
        LOG.info("Trying to list contents of resource resource: {}", resource);

        if (resource == null & cursor == null) {
            LOG.error("Neither resource nor cursor present.");
            throw new RuntimeException("At least the cursor must be present when no resource is present.");
        }
        String prefix = (resource != null) ? resource.getId() + "/" : "";

        int MAX_KEYS = 2;
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(s3Properties.getBucketName())
                .prefix(prefix)
                .maxKeys(MAX_KEYS);

        if (cursor != null && !cursor.isEmpty()) {
            requestBuilder.continuationToken(cursor);
        }

        ListObjectsV2Request request = requestBuilder.build();

        ListObjectsV2Response result;
        try {
            result = s3Client.listObjectsV2(request);
        } catch (Exception e) {
            LOG.error("Something went wrong. Logs: {}", e.getMessage());
            throw new RuntimeException("Something went wrong. Please check the logs.");
        }
        List<Resource> resources = new ArrayList<>();

        for (S3Object s3Object : result.contents()) {
            String id = s3Object.key();
            String name = id.substring(id.lastIndexOf('/') + 1);
            int type = id.endsWith("/") ? 1 : 0;

            resources.add(new Resource(id, name, type));
        }

        String nextCursor = result.nextContinuationToken();
        LOG.info("Successfully retrieved contents for resource {}", resource);
        return new ListResult<>(resources, nextCursor);
    }
}
