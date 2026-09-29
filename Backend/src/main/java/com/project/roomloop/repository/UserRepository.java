package com.project.roomloop.repository;

import com.project.roomloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String Email);

    Optional<User> findByMobileNumber(String mobileNumber);
}
