package com.re.project.controller;

import com.re.project.dto.JobDto;
import com.re.project.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/employer/jobs")
    public ResponseEntity<JobDto.Response> createJob(@RequestBody JobDto.CreateRequest request,
            Authentication authentication) {
        JobDto.Response response = jobService.createJob(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/employer/jobs/{id}/status")
    public ResponseEntity<JobDto.Response> updateJobStatus(@PathVariable Long id, @RequestParam String status,
            Authentication authentication) {
        JobDto.Response response = jobService.updateJobStatus(id, status, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/employer/jobs/{id}")
    public ResponseEntity<JobDto.Response> updateJob(@PathVariable Long id, @RequestBody JobDto.UpdateRequest request,
            Authentication authentication) {
        JobDto.Response response = jobService.updateJob(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/employer/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        jobService.deleteJob(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobDto.Response>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAllJobs(page, size));
    }
}
