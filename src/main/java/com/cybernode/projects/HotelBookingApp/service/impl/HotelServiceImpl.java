package com.cybernode.projects.HotelBookingApp.service.impl;


import com.cybernode.projects.HotelBookingApp.dto.*;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.InventoryRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import com.cybernode.projects.HotelBookingApp.service.ImageUploadService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import com.cybernode.projects.HotelBookingApp.service.PricingUpdateService;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ImageUploadService imageUploadService;
    private final PricingUpdateService pricingUpdateService;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with ID: {}", hotelDto.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }

        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating the hotel with ID: {}", id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }


        for(Room room: hotel.getRooms()) {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        hotel.setActive(true);

        // assuming only do it once
        for(Room room: hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }

        pricingUpdateService.updateHotelPrices(hotel);
    }

    //    public method
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        long nightsCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate());
        LocalDate stayEndDate = hotelInfoRequestDto.getEndDate().minusDays(1);

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(), stayEndDate,
                hotelInfoRequestDto.getRoomsCount(), nightsCount);

        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());

        HotelDto hotelDto = modelMapper.map(hotel, HotelDto.class);
        hotelDto.setPhotos(hotel.getPhotos());
        hotelDto.setAmenities(hotel.getAmenities());

        return new HotelInfoDto(hotelDto, rooms);
    }

    private static final int MAX_PAGE_SIZE = 100;

    private Pageable clampPageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }

    @Override
    public Page<HotelDto> getAllHotels(Pageable pageable) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Getting all hotels for the admin user with ID: {}", user.getId());
        return hotelRepository.findByOwner(user, clampPageSize(pageable))
                .map(hotel -> modelMapper.map(hotel, HotelDto.class));
    }

    @Override
    public String uploadHotelPhoto(Long hotelId, MultipartFile file) {
        log.info("Uploading photo for hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.equals(hotel.getOwner())) {
            throw new UnAuthorisedException("This user does not own this hotel with id: " + hotelId);
        }

        String imageUrl = imageUploadService.uploadImage(file);

        String[] currentPhotos = hotel.getPhotos();
        String[] newPhotos;
        if (currentPhotos == null) {
            newPhotos = new String[]{imageUrl};
        } else {
            newPhotos = new String[currentPhotos.length + 1];
            System.arraycopy(currentPhotos, 0, newPhotos, 0, currentPhotos.length);
            newPhotos[currentPhotos.length] = imageUrl;
        }
        hotel.setPhotos(newPhotos);
        hotelRepository.save(hotel);

        log.info("Successfully uploaded photo to Cloudinary and saved to hotel {}", hotelId);
        return imageUrl;
    }

    @Override
    public Page<HotelDto> getActiveHotels(String city, Pageable pageable) {
        log.info("Getting active hotels. City filter: {}", city);
        Pageable clamped = clampPageSize(pageable);
        Page<Hotel> hotelsPage;
        if (city == null || city.trim().isEmpty()) {
            hotelsPage = hotelRepository.findByActiveTrue(clamped);
        } else {
            hotelsPage = hotelRepository.findByActiveTrueAndCityContainingIgnoreCase(city.trim(), clamped);
        }
        return hotelsPage.map(hotel -> {
            HotelDto dto = modelMapper.map(hotel, HotelDto.class);
            dto.setPhotos(hotel.getPhotos());
            dto.setAmenities(hotel.getAmenities());
            if (hotel.getRooms() != null && !hotel.getRooms().isEmpty()) {
                dto.setPrice(hotel.getRooms().get(0).getBasePrice().doubleValue());
            } else {
                dto.setPrice(3500.0);
            }
            return dto;
        });
    }
}
