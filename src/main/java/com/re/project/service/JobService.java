package com.re.project.service;

import com.re.project.dto.JobDto;
import com.re.project.entity.Job;
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

import java.util.List;
import java.util.stream.Collectors;

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
                .status("PENDING") // Yêu cầu duyệt tin
                .employer(employer)
                .build();

        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional
    public JobDto.Response approveJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        job.setStatus("OPEN");
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

        job.setStatus(status);
        Job savedJob = jobRepository.save(job);
        return mapToResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public Page<JobDto.Response> getAllJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> jobs = jobRepository.findAllByStatus("OPEN", pageable);
        return jobs.map(this::mapToResponse);
    }

    private JobDto.Response mapToResponse(Job job) {
        JobDto.Response res = new JobDto.Response();
        res.setId(job.getId());
        res.setTitle(job.getTitle());
        res.setDescription(job.getDescription());
        res.setLocation(job.getLocation());
        res.setSalary(job.getSalary());
        res.setStatus(job.getStatus());
        res.setEmployerId(job.getEmployer().getId());
        res.setEmployerName(job.getEmployer().getFullName());
        res.setCreatedAt(job.getCreatedAt());
        return res;
    }
}
