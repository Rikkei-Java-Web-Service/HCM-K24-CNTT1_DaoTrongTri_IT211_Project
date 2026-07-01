package com.re.project.controller.admin;

import com.re.project.dto.JobDto;
import com.re.project.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
public class AdminJobController {
    private final AdminService adminService;

    /**
     * Lấy danh sách tất cả Job (Admin xem để duyệt)
     */
    @GetMapping
    public ResponseEntity<List<JobDto.Response>> getAllJobs() {
        return ResponseEntity.ok(adminService.getAllJobsForAdmin());
    }

    /**
     * Duyệt tin tuyển dụng
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<JobDto.Response> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveJob(id));
    }

    /**
     * Từ chối tin tuyển dụng
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<JobDto.Response> rejectJob(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectJob(id));
    }
}
