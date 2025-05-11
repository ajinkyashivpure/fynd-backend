package com.assess.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        System.setProperty("TOGETHER_AI_API_KEY", dotenv.get("TOGETHER_AI_API_KEY"));
        System.setProperty("MONGODB_ATLAS_URI", dotenv.get("MONGODB_ATLAS_URI"));
        System.setProperty("TOGETHER_AI_EMBEDDING_MODEL", dotenv.get("TOGETHER_AI_EMBEDDING_MODEL"));
        System.setProperty("TOGETHER_AI_BASE_URL", dotenv.get("TOGETHER_AI_BASE_URL"));

        SpringApplication.run(BackendApplication.class, args);
    }

}
