package com.re.project.service;

import com.re.project.dto.JobDto;
import com.re.project.entity.Job;
import com.re.project.entity.JobStatusEnum;
import com.re.project.entity.User;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.repository.JobRepository;
import com.re.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Transactional
    public JobDto.Response createJob(JobDto.CreateRequest request, String employerUsername) {
        User employer = userRepository.findUserByUsername(employerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .salary(request.getSalary())
                .status(JobStatusEnum.PENDING_APPROVAL)
                .employer(employer)
                .build();

        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional
    public JobDto.Response updateJobStatus(Long jobId, String status, String employerUsername) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to update this job");
        }

        job.setStatus(JobStatusEnum.valueOf(status.toUpperCase()));
        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional
    public JobDto.Response updateJob(Long jobId, JobDto.UpdateRequest request, String employerUsername) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to update this job");
        }

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getSalary() != null) job.setSalary(request.getSalary());

        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional
    public void deleteJob(Long jobId, String employerUsername) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getUsername().equals(employerUsername)) {
            throw new RuntimeException("You do not have permission to delete this job");
        }

        jobRepository.delete(job);
    }

    @Transactional(readOnly = true)
    public Page<JobDto.Response> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs = jobRepository.findAllByStatus(JobStatusEnum.APPROVED, pageable);
        return jobs.map(this::mapToResponse);
    }

    private JobDto.Response mapToResponse(Job job) {
        JobDto.Response res = new JobDto.Response();
        res.setId(job.getId());
        res.setTitle(job.getTitle());
        res.setDescription(job.getDescription());
        res.setLocation(job.getLocation());
        res.setSalary(job.getSalary());
        res.setStatus(job.getStatus().name());
        res.setEmployerId(job.getEmployer().getId());
        res.setEmployerName(job.getEmployer().getFullName());
        res.setCreatedAt(job.getCreatedAt());
        return res;
    }
}
