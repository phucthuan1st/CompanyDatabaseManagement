package com.csdlcongty;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptographyUtilities {

    /**
     * Hashes the input string using SHA-1 algorithm with salt.
     *
     * @param input The input string to hash.
     * @param salt The salt value.
     * @return The hashed string.
     * @throws NoSuchAlgorithmException if the SHA-1 algorithm is not available.
     */
    public static String hashSHA1(String input, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(salt.getBytes());
        byte[] hashedBytes = md.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * Hashes the input string using MD5 algorithm.
     *
     * @param input The input string to hash.
     * @return The hashed string.
     * @throws NoSuchAlgorithmException if the MD5 algorithm is not available.
     */
    public static String hashMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashedBytes = md.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * Applies PKCS7 padding to the input byte array.
     *
     * @param input The input byte array to pad.
     * @param blockSize The block size to use for padding.
     * @return The padded byte array.
     */
    public static byte[] paddingPKCS7(byte[] input, int blockSize) {
        int paddingLength = blockSize - (input.length % blockSize);
        byte[] paddedInput = new byte[input.length + paddingLength];
        System.arraycopy(input, 0, paddedInput, 0, input.length);
        for (int i = input.length; i < paddedInput.length; i++) {
            paddedInput[i] = (byte) paddingLength;
        }
        return paddedInput;
    }

    /**
     * Removes PKCS7 padding from the input byte array.
     *
     * @param paddedBytes The byte array with PKCS7 padding.
     * @return The byte array without padding.
     */
    private static byte[] removePKCS7Padding(byte[] paddedBytes) {
        int paddingLength = paddedBytes[paddedBytes.length - 1];
        if (paddingLength < 1 || paddingLength > 16) {
            throw new IllegalArgumentException("Invalid PKCS7 padding length");
        }

        // Check if the padding is valid
        for (int i = paddedBytes.length - paddingLength; i < paddedBytes.length; i++) {
            if (paddedBytes[i] != paddingLength) {
                throw new IllegalArgumentException("Invalid PKCS7 padding");
            }
        }

        // Remove the padding bytes
        return Arrays.copyOf(paddedBytes, paddedBytes.length - paddingLength);
    }

    /**
     * Encrypts the input string using AES-128 with CBC mode and PKCS7 padding.
     *
     * @param input The input string to encrypt.
     * @param key The encryption key.
     * @return The encrypted string.
     * @throws Exception if an error occurs during encryption.
     */
    public static String encryptAES(String input, String key) throws Exception {
        byte[] keyBytes = key.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generate a random IV (Initialization Vector)
        byte[] ivBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        // Initialize the cipher in encryption mode with the secret key and IV
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        // Perform the encryption
        byte[] paddedInput = paddingPKCS7(input.getBytes(), cipher.getBlockSize());
        byte[] encryptedBytes = cipher.doFinal(paddedInput);

        // Combine the IV and encrypted bytes into a single byte array
        byte[] combinedBytes = new byte[ivBytes.length + encryptedBytes.length];
        System.arraycopy(ivBytes, 0, combinedBytes, 0, ivBytes.length);
        System.arraycopy(encryptedBytes, 0, combinedBytes, ivBytes.length, encryptedBytes.length);

        // Encode the combined bytes using Base64
        return Base64.getEncoder().encodeToString(combinedBytes);
    }

    /**
     * Decrypts the input encrypted Base64 string using AES-128 with CBC mode
     * and PKCS7 padding.
     *
     * @param encryptedBase64 The encrypted Base64 string to decrypt.
     * @param key The decryption key.
     * @return The decrypted string.
     * @throws Exception if an error occurs during decryption.
     */
    public static String decryptAES(String encryptedBase64, String key) throws Exception {
        byte[] keyBytes = key.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Decode the encrypted Base64 string to get the combined bytes
        byte[] combinedBytes = Base64.getDecoder().decode(encryptedBase64);

        // Extract the IV bytes from the combined bytes
        byte[] ivBytes = new byte[16];
        System.arraycopy(combinedBytes, 0, ivBytes, 0, ivBytes.length);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

        // Initialize the cipher in decryption mode with the secret key and IV
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        // Decrypt the remaining bytes after the IV
        byte[] encryptedBytes = new byte[combinedBytes.length - ivBytes.length];
        System.arraycopy(combinedBytes, ivBytes.length, encryptedBytes, 0, encryptedBytes.length);

        // Perform the decryption
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // Remove PKCS7 padding
        byte[] unpaddedBytes = removePKCS7Padding(decryptedBytes);

        // Convert the decrypted bytes to a string
        return new String(unpaddedBytes);
    }
    
    public static String generateSalt(int length) {
        // Create a secure random object
        SecureRandom secureRandom = new SecureRandom();

        // Generate random bytes
        byte[] saltBytes = new byte[length];
        secureRandom.nextBytes(saltBytes);

        // Encode the random bytes as a Base64 string
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        return salt;
    }
}
