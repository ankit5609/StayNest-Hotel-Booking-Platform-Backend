package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.repository.BookingRepository;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private User user;
    private Booking booking;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");

        com.cybernode.projects.HotelBookingApp.entity.Room room = new com.cybernode.projects.HotelBookingApp.entity.Room();
        room.setType("Deluxe");

        booking = new Booking();
        booking.setId(100L);
        booking.setAmount(BigDecimal.valueOf(250.00));
        booking.setHotel(hotel);
        booking.setRoom(room);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User principal) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetCheckoutSession_Success() {
        mockSecurityContext(user);

        try (MockedStatic<Customer> customerMocked = mockStatic(Customer.class);
             MockedStatic<Session> sessionMocked = mockStatic(Session.class)) {

            Customer customer = mock(Customer.class);
            when(customer.getId()).thenReturn("cust_123");
            customerMocked.when(() -> Customer.create(any(CustomerCreateParams.class))).thenReturn(customer);

            Session session = mock(Session.class);
            when(session.getId()).thenReturn("sess_999");
            when(session.getUrl()).thenReturn("http://stripe-session-url");
            sessionMocked.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            String url = checkoutService.getCheckoutSession(booking, "http://success", "http://failure");

            assertEquals("http://stripe-session-url", url);
            verify(bookingRepository, times(1)).save(booking);
            assertEquals("sess_999", booking.getPaymentSessionId());
        }
    }
}
