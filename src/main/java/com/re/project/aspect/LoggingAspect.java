package com.re.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Ghi log sau khi nộp CV thành công (phương thức applyForJob trong ApplicationService)
     */
    @AfterReturning(pointcut = "execution(* com.re.project.service.ApplicationService.applyJob(..))", returning = "result")
    public void logSuccessfulApplication(JoinPoint joinPoint, Object result) {
        String username = "Unknown";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Lấy argument jobId
        Object[] args = joinPoint.getArgs();
        Long jobId = null;
        if (args != null && args.length > 0 && args[0] instanceof com.re.project.dto.ApplicationDto.ApplyRequest) {
            com.re.project.dto.ApplicationDto.ApplyRequest request = (com.re.project.dto.ApplicationDto.ApplyRequest) args[0];
            jobId = request.getJobId();
        }

        log.info("🎯 [AUDIT LOG] Ứng viên '{}' vừa nộp CV thành công cho công việc ID: {}", username, jobId);
    }
}
