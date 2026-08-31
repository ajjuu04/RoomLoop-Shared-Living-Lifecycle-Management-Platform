package com.project.roomloop.helperMethod;

import com.project.roomloop.entity.Membership;
import com.project.roomloop.entity.Room;
import com.project.roomloop.entity.User;
import com.project.roomloop.entity.types.MembershipStatus;
import com.project.roomloop.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomAccessGuide {

    private final MembershipRepository membershipRepository;


    public Membership isUserActiveMemberOfRoom(User user, Room room){
        return membershipRepository.findByUserAndRoomAndMembershipStatus(user,room, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("you are not active member"));
    }

    public Boolean isUserAdmin(User user,Room room){
        Membership membership = isUserActiveMemberOfRoom(user,room);
        if (!Boolean.TRUE.equals(membership.getIsAdmin())){
            throw new AccessDeniedException("Member in not admin, Only admin can do this action");
        }
        return true;
    }

    public Boolean isUserSercher(User user){
        boolean isActiveMember = membershipRepository.existsByUserAndMembershipStatus(user,MembershipStatus.ACTIVE);
        if (isActiveMember){
            throw new AccessDeniedException("Member is Already is room");
        }
        return true;
    }

}
