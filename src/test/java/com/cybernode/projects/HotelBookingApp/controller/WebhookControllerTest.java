package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;
import com.cybernode.projects.HotelBookingApp.security.WebSecurityConfig;
import com.cybernode.projects.HotelBookingApp.security.JWTAuthFilter;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    public void testCapturePayments_Success() throws Exception {
        try (MockedStatic<Webhook> webhookMocked = mockStatic(Webhook.class)) {
            Event mockEvent = mock(Event.class);
            webhookMocked.when(() -> Webhook.constructEvent(anyString(), anyString(), any()))
                    .thenReturn(mockEvent);

            doNothing().when(bookingService).capturePayment(mockEvent);

            mockMvc.perform(post("/webhook/payment")
                            .with(csrf())
                            .header("Stripe-Signature", "t=123,v1=sig")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("event-payload"))
                    .andExpect(status().isNoContent());

            verify(bookingService, times(1)).capturePayment(mockEvent);
        }
    }
}
