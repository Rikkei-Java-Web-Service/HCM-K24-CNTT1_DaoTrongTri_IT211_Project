package com.re.project.repository;

import com.re.project.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findAllByStatus(String status, Pageable pageable);
    List<Job> findByEmployerId(Long employerId);
}
