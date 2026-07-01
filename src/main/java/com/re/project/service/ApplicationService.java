package com.re.project.service;

import com.re.project.dto.ApplicationDto;
import com.re.project.entity.Application;
import com.re.project.entity.ApplicationStatusEnum;
import com.re.project.entity.Job;
import com.re.project.entity.JobStatusEnum;
import com.re.project.entity.User;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.repository.ApplicationRepository;
import com.re.project.repository.JobRepository;
import com.re.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

        // Kiểm tra tin tuyển dụng vẫn đang mở (đã được duyệt)
        if (job.getStatus() != JobStatusEnum.APPROVED) {
            throw new RuntimeException("Job is no longer accepting applications");
        }

        // Chống nộp trùng hồ sơ
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new RuntimeException("You have already applied for this job");
        }

        // Cập nhật cvUrl cho Candidate nếu có gửi lên
        if (request.getCvUrl() != null && !request.getCvUrl().isEmpty()) {
            candidate.setCvUrl(request.getCvUrl());
            userRepository.save(candidate);
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatusEnum.PENDING)
                .build();

        Application savedApp = applicationRepository.save(application);
        return mapToResponse(savedApp);
    }

    // Ràng buộc luồng vòng đời chuyển trạng thái hồ sơ
    private static final Map<ApplicationStatusEnum, Set<ApplicationStatusEnum>> VALID_TRANSITIONS = Map.of(
            ApplicationStatusEnum.PENDING, Set.of(ApplicationStatusEnum.REVIEWING),
            ApplicationStatusEnum.REVIEWING, Set.of(ApplicationStatusEnum.INTERVIEWING, ApplicationStatusEnum.REJECTED),
            ApplicationStatusEnum.INTERVIEWING, Set.of(ApplicationStatusEnum.ACCEPTED, ApplicationStatusEnum.REJECTED)
    );

    @Transactional
    public ApplicationDto.Response updateApplicationStatus(Long applicationId, String newStatus, String employerUsername) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getJob().getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to update this application");
        }

        ApplicationStatusEnum currentStatus = application.getStatus();
        ApplicationStatusEnum targetStatus;
        try {
            targetStatus = ApplicationStatusEnum.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        Set<ApplicationStatusEnum> allowedNextStatuses = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedNextStatuses.contains(targetStatus)) {
            throw new RuntimeException(
                    String.format("Invalid status transition: %s → %s. Allowed: %s",
                            currentStatus, targetStatus, allowedNextStatuses));
        }

        application.setStatus(targetStatus);
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

    @Transactional
    public void deleteApplication(Long applicationId, String candidateUsername) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getCandidate().getUsername().equals(candidateUsername)) {
            throw new RuntimeException("You do not have permission to delete this application");
        }

        applicationRepository.delete(application);
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
        res.setStatus(app.getStatus().name());
        res.setCreatedAt(app.getCreatedAt());
        return res;
    }
}
