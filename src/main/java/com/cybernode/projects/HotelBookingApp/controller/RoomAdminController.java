package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.RoomDto;
import com.cybernode.projects.HotelBookingApp.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomAdminController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new room in a hotel", tags = {"Admin Inventory"})
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId,
                                                 @Valid @RequestBody RoomDto roomDto) {
        // RoomDto is validated against constraints (capacity >= 1, base price >= 0, etc.) before creating
        RoomDto room = roomService.createNewRoom(hotelId, roomDto);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all rooms in a hotel", tags = {"Admin Inventory"})
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }


    @GetMapping("/{roomId}")
    @Operation(summary = "Get a room by id", tags = {"Admin Inventory"})
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Delete a room by id", tags = {"Admin Inventory"})
    public ResponseEntity<RoomDto> deleteRoomById(@PathVariable Long hotelId, @PathVariable Long roomId) {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Update a room", tags = {"Admin Inventory"})
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId, @PathVariable Long roomId,
                                                  @Valid @RequestBody RoomDto roomDto) {
        // RoomDto is validated against constraints (capacity >= 1, base price >= 0, etc.) before updating
        return ResponseEntity.ok(roomService.updateRoomById(hotelId, roomId, roomDto));
    }

    @PostMapping("/{roomId}/photos")
    @Operation(summary = "Upload a photo for a room", tags = {"Admin Inventory"})
    public ResponseEntity<String> uploadRoomPhoto(@PathVariable Long hotelId,
                                                   @PathVariable Long roomId,
                                                   @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String url = roomService.uploadRoomPhoto(roomId, file);
        return ResponseEntity.ok(url);
    }

}
