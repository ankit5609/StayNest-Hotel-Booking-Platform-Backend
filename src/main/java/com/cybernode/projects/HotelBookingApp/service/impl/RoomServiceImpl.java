package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.RoomDto;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import com.cybernode.projects.HotelBookingApp.service.ImageUploadService;
import com.cybernode.projects.HotelBookingApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;
    private final ImageUploadService imageUploadService;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorisedException("This user does not own this room with id: "+roomId);
        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating the room with ID: {}", roomId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));

        BigDecimal oldBasePrice = room.getBasePrice();
        Integer oldTotalCount = room.getTotalCount();

        modelMapper.map(roomDto, room);
        room.setId(roomId);
        room = roomRepository.save(room);

        boolean priceChanged = roomDto.getBasePrice() != null && oldBasePrice.compareTo(roomDto.getBasePrice()) != 0;
        boolean countChanged = roomDto.getTotalCount() != null && !oldTotalCount.equals(roomDto.getTotalCount());

        if (priceChanged || countChanged) {
            inventoryService.updateInventoryForRoomChange(room, priceChanged, countChanged);
        }

        return modelMapper.map(room, RoomDto.class);
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public String uploadRoomPhoto(Long roomId, MultipartFile file) {
        log.info("Uploading photo for room with ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + room.getHotel().getId());
        }

        String imageUrl = imageUploadService.uploadImage(file);

        String[] currentPhotos = room.getPhotos();
        String[] newPhotos;
        if (currentPhotos == null) {
            newPhotos = new String[]{imageUrl};
        } else {
            newPhotos = new String[currentPhotos.length + 1];
            System.arraycopy(currentPhotos, 0, newPhotos, 0, currentPhotos.length);
            newPhotos[currentPhotos.length] = imageUrl;
        }
        room.setPhotos(newPhotos);
        roomRepository.save(room);

        log.info("Successfully uploaded photo to Cloudinary and saved to room {}", roomId);
        return imageUrl;
    }
}
