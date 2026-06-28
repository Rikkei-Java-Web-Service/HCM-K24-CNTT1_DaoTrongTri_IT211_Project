package com.re.project.service;

import com.re.project.dto.ApplicationDto;
import com.re.project.entity.Application;
import com.re.project.entity.Job;
import com.re.project.entity.User;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.repository.ApplicationRepository;
import com.re.project.repository.JobRepository;
import com.re.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationDto.Response applyJob(ApplicationDto.ApplyRequest request, String candidateUsername) {
        User candidate = userRepository.findUserByUsername(candidateUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!"OPEN".equals(job.getStatus())) {
            throw new RuntimeException("Job is no longer accepting applications");
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new RuntimeException("You have already applied for this job");
        }

        // Cập nhật cvUrl cho Candidate nếu có gửi lên (hỗ trợ UC-05)
        if (request.getCvUrl() != null && !request.getCvUrl().isEmpty()) {
            candidate.setCvUrl(request.getCvUrl());
            userRepository.save(candidate);
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .coverLetter(request.getCoverLetter())
                .status("PENDING")
                .build();

        Application savedApp = applicationRepository.save(application);
        return mapToResponse(savedApp);
    }

    // Định nghĩa luồng vòng đời trạng thái hồ sơ hợp lệ
    private static final java.util.Map<String, java.util.Set<String>> VALID_TRANSITIONS = java.util.Map.of(
            "PENDING", java.util.Set.of("REVIEWING"),
            "REVIEWING", java.util.Set.of("INTERVIEWING", "REJECTED"),
            "INTERVIEWING", java.util.Set.of("ACCEPTED", "REJECTED")
    );

    @Transactional
    public ApplicationDto.Response updateApplicationStatus(Long applicationId, String newStatus, String employerUsername) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to update this application");
        }

        String currentStatus = application.getStatus();
        java.util.Set<String> allowedNextStatuses = VALID_TRANSITIONS.getOrDefault(currentStatus, java.util.Set.of());

        if (!allowedNextStatuses.contains(newStatus.toUpperCase())) {
            throw new RuntimeException(
                    String.format("Invalid status transition: %s → %s. Allowed: %s",
                            currentStatus, newStatus, allowedNextStatuses));
        }

        application.setStatus(newStatus.toUpperCase());
        Application savedApp = applicationRepository.save(application);
        return mapToResponse(savedApp);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto.Response> getApplicationsForCandidate(String candidateUsername) {
        User candidate = userRepository.findUserByUsername(candidateUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        return applicationRepository.findByCandidateId(candidate.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto.Response> getApplicationsForJob(Long jobId, String employerUsername) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to view these applications");
        }

        return applicationRepository.findByJobId(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ApplicationDto.Response mapToResponse(Application app) {
        ApplicationDto.Response res = new ApplicationDto.Response();
        res.setId(app.getId());
        res.setJobId(app.getJob().getId());
        res.setJobTitle(app.getJob().getTitle());
        res.setCandidateId(app.getCandidate().getId());
        res.setCandidateName(app.getCandidate().getFullName());
        res.setCvUrl(app.getCandidate().getCvUrl());
        res.setCoverLetter(app.getCoverLetter());
        res.setStatus(app.getStatus());
        res.setCreatedAt(app.getCreatedAt());
        return res;
    }
}
