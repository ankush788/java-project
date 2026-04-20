package com.authservice.utility;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password encoding and verification using BCrypt
 */
public class PasswordEncoderUtil {

    /**
     * Encode a plain text password using BCrypt
     * @param plainPassword the plain text password
     * @return the encoded password
     */
    public static String encodePassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify a plain text password against an encoded password
     * @param plainPassword the plain text password to verify
     * @param encodedPassword the encoded password to compare against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String encodedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
