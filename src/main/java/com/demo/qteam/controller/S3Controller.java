package com.demo.qteam.controller;

import com.demo.qteam.exception.ResourceNotFoundException;
import com.demo.qteam.model.ListResult;
import com.demo.qteam.model.Resource;
import com.demo.qteam.service.S3Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/health")
    public String ping() {
        return "pong";
    }

    @PostMapping("/file")
    public ResponseEntity<FileSystemResource> getAsFile(@RequestBody @NonNull Resource resource) {
        File response = s3Service.getFile(resource);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(response));
    }

    @GetMapping("/resource")
    public ResponseEntity<Resource> getResource(@RequestParam @NonNull String id) {
        Resource resource = s3Service.getResource(id);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/listFolder")
    public ResponseEntity<ListResult<Resource>> listFolder(@RequestBody(required = false) Resource resource, @RequestParam String cursor) {
        ListResult<Resource> result = s3Service.listFolder(resource, cursor);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
}
