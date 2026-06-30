package com.cybernode.projects.HotelBookingApp;

import com.cybernode.projects.HotelBookingApp.dto.SignUpRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        // Setup standard Jakarta Bean Validator factory (no Spring context needed)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testSignUpValidationFailsForInvalidFields() {
        SignUpRequestDto invalidRequest = new SignUpRequestDto();
        invalidRequest.setEmail("not-an-email"); // invalid format
        invalidRequest.setPassword("short");      // too short (min 8 characters)
        invalidRequest.setName("");                // blank name is invalid

        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(invalidRequest);
        assertFalse(violations.isEmpty(), "Validation violations should be reported");

        boolean emailFailed = false;
        boolean passwordFailed = false;
        boolean nameFailed = false;

        for (ConstraintViolation<SignUpRequestDto> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            if ("email".equals(propertyPath)) {
                emailFailed = true;
            } else if ("password".equals(propertyPath)) {
                passwordFailed = true;
            } else if ("name".equals(propertyPath)) {
                nameFailed = true;
            }
        }

        assertTrue(emailFailed, "Email field validation should fail");
        assertTrue(passwordFailed, "Password field validation should fail");
        assertTrue(nameFailed, "Name field validation should fail");
    }

    @Test
    public void testSignUpValidationPassesForValidFields() {
        SignUpRequestDto validRequest = new SignUpRequestDto();
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPassword("securepassword123");
        validRequest.setName("John Doe");

        Set<ConstraintViolation<SignUpRequestDto>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "A valid request should have no validation violations");
    }
}
