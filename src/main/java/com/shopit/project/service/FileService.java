package com.shopit.project.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    public String uploadFile(String fileName, MultipartFile file) throws IOException;
}
