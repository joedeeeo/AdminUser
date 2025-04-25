package com.example.exam.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.exam.entity.AdminUser;
import com.example.exam.enums.Role;


@Repository
public interface AdminUserRepo extends JpaRepository<AdminUser, Long> {
	
	List<AdminUser> findByNameStartingWith(String query);
	List<AdminUser> findByRole(Role role);
	Optional<AdminUser> findByEmail(String email);
	Page<AdminUser> findByRole(Role role, Pageable pageable);
	
	@Query("SELECT u FROM AdminUser u WHERE u.role = :role AND "
			+ "(LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
	Page<AdminUser> findByRoleAndSearch(@Param("role") Role role, @Param("search") String search, Pageable pageable);

}