package com.myapp.mcqprep.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class DedupService {

    public String hash(String questionText, String codeSnippet) {
        String normalized = (questionText.trim().toLowerCase() + "|" +
                (codeSnippet == null ? "" : codeSnippet.trim())).replaceAll("\\s+", " ");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // should never happen on a real JVM
        }
    }
}