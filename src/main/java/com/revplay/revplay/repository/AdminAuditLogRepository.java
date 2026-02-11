package com.revplay.revplay.repository;

import com.revplay.revplay.entity.AdminAuditLog;
import com.revplay.revplay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {


    List<AdminAuditLog> findByAdminUser(User adminUser);


    List<AdminAuditLog> findByTargetEntity(String targetEntity);


    List<AdminAuditLog> findAllByOrderByCreatedAtDesc();
}
