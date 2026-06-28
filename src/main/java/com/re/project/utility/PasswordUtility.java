package com.re.project.utility;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtility {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The raw password from user input
     * @return The hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    /**
     * Verify if a raw password matches a hashed password
     * @param plainPassword The raw password
     * @param hashedPassword The hashed password from DB
     * @return true if matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }

    public static void main(String[] args) {
        String rawPass = "123456";
        String hashedPass = hashPassword(rawPass);

        System.out.println("Raw Password: " + rawPass);
        System.out.println("B-Crypt Hash: " + hashedPass);
    }
}
