package com.project.roomloop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class RoomMembersDto {
    private MemberDto admin;
    private List<MemberDto> members;
}