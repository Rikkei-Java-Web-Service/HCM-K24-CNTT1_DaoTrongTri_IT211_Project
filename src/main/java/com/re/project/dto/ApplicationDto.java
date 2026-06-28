package com.re.project.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ApplicationDto {
    @Data
    public static class ApplyRequest {
        private Long jobId;
        private String coverLetter;
        private String cvUrl;
    }

    @Data
    public static class StatusUpdateRequest {
        private String status; // PENDING, REVIEWING, INTERVIEWING, ACCEPTED, REJECTED
    }

    @Data
    public static class Response {
        private Long id;
        private Long jobId;
        private String jobTitle;
        private Long candidateId;
        private String candidateName;
        private String cvUrl;
        private String coverLetter;
        private String status;
        private LocalDateTime createdAt;
    }
}
