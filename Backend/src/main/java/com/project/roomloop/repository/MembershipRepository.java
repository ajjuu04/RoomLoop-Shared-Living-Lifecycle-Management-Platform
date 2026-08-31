
package com.project.roomloop.repository;

import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    boolean existsByUserAndMembershipStatus(
            User user,
            MembershipStatus membershipStatus
    );

    Optional<Membership> findByUserAndMembershipStatus(
            User user,
            MembershipStatus membershipStatus
    );

    Optional<Membership> findByUserAndRoomAndMembershipStatus(
            User user,
            Room room,
            MembershipStatus membershipStatus
    );

    long countByRoomAndMembershipStatus(
            Room room,
            MembershipStatus membershipStatus
    );
}