package com.re.project.service;

import com.re.project.dto.JobDto;
import com.re.project.dto.UserDetailsDto;
import com.re.project.entity.Job;
import com.re.project.entity.JobStatusEnum;
import com.re.project.entity.User;
import com.re.project.exception.ResourceNotFoundException;
import com.re.project.mapper.UserMapper;
import com.re.project.repository.JobRepository;
import com.re.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserMapper userMapper;

    /**
     * Lấy danh sách tất cả User, lọc theo role nếu có
     */
    @Transactional(readOnly = true)
    public List<UserDetailsDto> getAllUsers(String role) {
        List<User> entities = userRepository.findAll();

        return entities.stream()
                .filter(user -> role == null || user.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("ROLE_" + role)))
                .map(userMapper::toUserDetailsDto)
                .collect(Collectors.toList());
    }

    /**
     * Khóa/Mở khóa tài khoản người dùng
     */
    @Transactional
    public UserDetailsDto toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setEnabled(!user.isEnabled());
        User savedUser = userRepository.save(user);
        return userMapper.toUserDetailsDto(savedUser);
    }

    /**
     * Lấy danh sách tất cả tin tuyển dụng
     */
    @Transactional(readOnly = true)
    public List<JobDto.Response> getAllJobsForAdmin() {
        return jobRepository.findAll().stream()
                .map(this::mapJobToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Duyệt tin tuyển dụng (PENDING_APPROVAL -> APPROVED)
     */
    @Transactional
    public JobDto.Response approveJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatusEnum.PENDING_APPROVAL) {
            throw new RuntimeException("Only jobs with status PENDING_APPROVAL can be approved");
        }

        job.setStatus(JobStatusEnum.APPROVED);
        Job savedJob = jobRepository.save(job);
        return mapJobToResponse(savedJob);
    }

    /**
     * Từ chối tin tuyển dụng (PENDING_APPROVAL -> REJECTED)
     */
    @Transactional
    public JobDto.Response rejectJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatusEnum.PENDING_APPROVAL) {
            throw new RuntimeException("Only jobs with status PENDING_APPROVAL can be rejected");
        }

        job.setStatus(JobStatusEnum.REJECTED);
        Job savedJob = jobRepository.save(job);
        return mapJobToResponse(savedJob);
    }

    private JobDto.Response mapJobToResponse(Job job) {
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
