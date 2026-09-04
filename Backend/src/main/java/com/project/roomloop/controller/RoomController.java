package com.project.roomloop.controller;


import com.project.roomloop.dto.RegisterNewRoomRequest;
import com.project.roomloop.dto.RoomDetailsDto;
import com.project.roomloop.dto.RoomOccupancyUpdateDto;
import com.project.roomloop.dto.RoomUpdateDto;
import com.project.roomloop.entity.User;
import com.project.roomloop.repository.RoomRepository;
import com.project.roomloop.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")  // add here @valid latter
    public ResponseEntity<RoomDetailsDto> registerNewRoom(@RequestBody RegisterNewRoomRequest registerNewRoomRequest,@AuthenticationPrincipal Long userId){
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.registerNewRoom(registerNewRoomRequest,userId));
    }

    @GetMapping("/me")
    public ResponseEntity<RoomDetailsDto> getRoomDetails(@AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(roomService.getRoomDetails(userId));
    }

    // no need extra
    @GetMapping("/me/{roomId}")
    public ResponseEntity<RoomDetailsDto> getRoomDetailsById(@PathVariable Long roomId){
        return ResponseEntity.ok(roomService.getRoomDetailsById(roomId));
    }

    @PatchMapping("/edit/{roomId}")
    public RoomDetailsDto editRoom(@PathVariable Long roomId,
                                   @RequestBody RoomUpdateDto roomUpdateDto,
                                   @AuthenticationPrincipal Long userId){
        return roomService.editRoom(roomId,roomUpdateDto,userId);
    }

    @PatchMapping("/edit/occupancy/{roomId}")
    public RoomDetailsDto editRoomOccupancy(@PathVariable Long roomId,
                                            @RequestBody RoomOccupancyUpdateDto roomOccupancyUpdateDto,
                                            @AuthenticationPrincipal Long userId){
        return roomService.editRoomOccupancy(roomId,roomOccupancyUpdateDto,userId);
    }
}
