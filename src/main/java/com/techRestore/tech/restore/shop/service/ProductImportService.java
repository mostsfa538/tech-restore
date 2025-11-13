package com.techRestore.tech.restore.shop.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final JobLauncher jobLauncher;
    private final Job runJob;

    public String importProducts(MultipartFile file) {
        try {
            File tempFile = Files.createTempFile("products-", ".csv").toFile();
            file.transferTo(tempFile);
            System.setProperty("uploadFile", tempFile.getAbsolutePath());
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(runJob, jobParameters);
            return "Product import job started successfully.";
        } catch (IOException e) {
            throw new RuntimeException("Failed to store temporary file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to run import job: " + e.getMessage(), e);
        }
    }
}
