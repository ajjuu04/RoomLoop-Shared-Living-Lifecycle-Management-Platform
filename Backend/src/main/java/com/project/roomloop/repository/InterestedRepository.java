package com.project.roomloop.repository;

import com.project.roomloop.entity.Interested;
import com.project.roomloop.entity.Listing;
import com.project.roomloop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestedRepository extends JpaRepository<Interested, Long> {
    boolean existsByUserAndListing(
            User user,
            Listing listing
    );

    Optional<Interested> findByUserAndListing(
            User user,
            Listing listing
    );
}