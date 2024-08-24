package com.shopit.project.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.delete;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @Mock
    private MultipartFile multipartFile;

    @Test
    void testUploadFile() throws IOException {
        String path = "test-path";
        String originalFileName = "image.jpg";

        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        String fileName = fileService.uploadFile(path, multipartFile);
        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));

        String randomId = fileName.substring(fileName.lastIndexOf('.'));
        assertNotNull(randomId);

        String filePath = path + File.separator + fileName;
        File file = new File(filePath);
        assertTrue(file.exists());


        // Clean up the test file
        file.delete();
        delete(Path.of(path));
    }
}