// src/test/java/com/eventmanagement/util/ValidationUtilsTest.java
package com.eventmanagement.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilsTest {

    @Test
    void whenIsValidEmail_withValidEmail_thenReturnTrue() {
        assertThat(ValidationUtils.isValidEmail("test@example.com")).isTrue();
        assertThat(ValidationUtils.isValidEmail("user.name@domain.co.uk")).isTrue();
        assertThat(ValidationUtils.isValidEmail("user+tag@example.org")).isTrue();
    }

    @Test
    void whenIsValidEmail_withInvalidEmail_thenReturnFalse() {
        assertThat(ValidationUtils.isValidEmail("invalid-email")).isFalse();
        assertThat(ValidationUtils.isValidEmail("@example.com")).isFalse();
        assertThat(ValidationUtils.isValidEmail("test@")).isFalse();
        assertThat(ValidationUtils.isValidEmail("test.example.com")).isFalse();
        assertThat(ValidationUtils.isValidEmail(null)).isFalse();
    }

    @Test
    void whenIsValidPassword_withValidPassword_thenReturnTrue() {
        assertThat(ValidationUtils.isValidPassword("password123")).isTrue();
        assertThat(ValidationUtils.isValidPassword("123456")).isTrue();
        assertThat(ValidationUtils.isValidPassword("verylongpassword")).isTrue();
    }

    @Test
    void whenIsValidPassword_withInvalidPassword_thenReturnFalse() {
        assertThat(ValidationUtils.isValidPassword("12345")).isFalse(); // Too short
        assertThat(ValidationUtils.isValidPassword("")).isFalse();
        assertThat(ValidationUtils.isValidPassword(null)).isFalse();
    }

    @Test
    void whenIsNotBlank_withValidString_thenReturnTrue() {
        assertThat(ValidationUtils.isNotBlank("hello")).isTrue();
        assertThat(ValidationUtils.isNotBlank("  hello  ")).isTrue();
        assertThat(ValidationUtils.isNotBlank("a")).isTrue();
    }

    @Test
    void whenIsNotBlank_withInvalidString_thenReturnFalse() {
        assertThat(ValidationUtils.isNotBlank("")).isFalse();
        assertThat(ValidationUtils.isNotBlank("   ")).isFalse();
        assertThat(ValidationUtils.isNotBlank(null)).isFalse();
    }
}
