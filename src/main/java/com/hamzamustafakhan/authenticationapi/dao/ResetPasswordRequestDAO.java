package com.hamzamustafakhan.authenticationapi.dao;

import com.hamzamustafakhan.authenticationapi.entity.ResetPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface ResetPasswordRequestDAO extends JpaRepository<ResetPasswordRequest, Integer> {

    @Query("SELECT r FROM ResetPasswordRequest r WHERE r.user.id = ?1 AND r.status = ?2 ORDER BY r.createdAt DESC")
    ResetPasswordRequest findByUser(int id, String status);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM reset_requests r WHERE r.user_id = :userId", nativeQuery = true)
    void deleteRequests(@Param("userId") int userId);

}
