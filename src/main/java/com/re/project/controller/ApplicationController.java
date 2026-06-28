package com.re.project.controller;

import com.re.project.dto.ApplicationDto;
import com.re.project.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/candidate/applications")
    public ResponseEntity<ApplicationDto.Response> applyJob(@RequestBody ApplicationDto.ApplyRequest request,
            Authentication authentication) {
        ApplicationDto.Response response = applicationService.applyJob(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/employer/applications/{id}/status")
    public ResponseEntity<ApplicationDto.Response> updateStatus(@PathVariable Long id,
            @RequestBody ApplicationDto.StatusUpdateRequest request, Authentication authentication) {
        ApplicationDto.Response response = applicationService.updateApplicationStatus(id, request.getStatus(),
                authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/candidate/applications")
    public ResponseEntity<List<ApplicationDto.Response>> getCandidateApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplicationsForCandidate(authentication.getName()));
    }

    @GetMapping("/employer/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationDto.Response>> getJobApplications(@PathVariable Long jobId,
            Authentication authentication) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, authentication.getName()));
    }
}
