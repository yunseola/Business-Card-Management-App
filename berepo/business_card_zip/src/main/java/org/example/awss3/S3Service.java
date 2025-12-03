package org.example.awss3;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String upload(byte[] bytes, String folderName, String contentType);
    String upload(MultipartFile file, String folderName);
}
