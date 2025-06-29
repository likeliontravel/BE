package org.example.be.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GCSConfig {

    @Value("${spring.cloud.gcp.credentials.location}")
    private String credentialsLocation;

    @Bean
    public Storage storage() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ClassPathResource("toleave-b9a7b3a17267.json").getInputStream());
            return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (IOException e) {
            throw new RuntimeException("GCS 자격 증명 파일 로드 실패", e);
        }

    }
}
