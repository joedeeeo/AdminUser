package com.example.exam.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.exam.entity.ResetPasswordToken;


@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, String> {

}
