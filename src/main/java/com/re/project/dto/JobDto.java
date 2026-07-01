package com.re.project.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class JobDto {
    @Data
    public static class CreateRequest {
        private String title;
        private String description;
        private String location;
        private Double salary;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private String location;
        private Double salary;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String location;
        private Double salary;
        private String status;
        private Long employerId;
        private String employerName;
        private LocalDateTime createdAt;
    }
}
