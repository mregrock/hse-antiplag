package ru.hse.antiplag.filestorageservice.utils;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Utility class for calculating the hash of a file.
 */
public class FileHashUtil {
  /**
   * Calculates the SHA-256 hash of the given input stream.
   * 
   * @param inputStream the input stream to calculate the hash of
   * @return the SHA-256 hash of the input stream
   */
  public static String calculateSHA256(InputStream inputStream) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] bytesBuffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
        digest.update(bytesBuffer, 0, bytesRead);
    }
    byte[] hashedBytes = digest.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : hashedBytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
}
