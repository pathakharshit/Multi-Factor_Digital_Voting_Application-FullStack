package com.example.Online.Voting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FaceAuthService {

    private final RestTemplate restTemplate;

    @Value("${python.face.encode.url:http://localhost:5000/encode_face}")
    private String encodeFaceUrl;

    @Value("${python.face.compare.url:http://localhost:5000/compare_faces}")
    private String compareFaceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calls Python /encode_face with the given face photo (MultipartFile).
     * Returns a JSON string (e.g. "[0.123, -0.456, ...]") representing the face encoding.
     */
    public String encodeFace(MultipartFile faceFile) throws Exception {
        if (faceFile == null || faceFile.isEmpty()) {
            throw new Exception("No face file provided.");
        }

        // Prepare multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(faceFile.getBytes()) {
            @Override
            public String getFilename() {
                return faceFile.getOriginalFilename();
            }
        };
        body.add("faceFile", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Call Python /encode_face
        ResponseEntity<Map> response = restTemplate.postForEntity(
                encodeFaceUrl,
                requestEntity,
                Map.class
        );

        List<Double> encodingList = getDoubles(response);
        // Convert it to JSON string (e.g. "[0.123, -0.456, ...]")
        return objectMapper.writeValueAsString(encodingList);
    }

    private static List<Double> getDoubles(ResponseEntity<Map> response) throws Exception {
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error calling /encode_face: " + response.getStatusCode());
        }
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.containsKey("error")) {
            throw new Exception("Python error: " + (responseBody != null ? responseBody.get("error") : "no response"));
        }

        // Extract the "encoding" field from Python response
        List<Double> encodingList = (List<Double>) responseBody.get("encoding");
        return encodingList;
    }

    /**
     * Calls Python /compare_faces with the user's new face photo and the stored face encoding JSON.
     * Returns true if they match, false otherwise.
     */
    public boolean compareFaces(MultipartFile faceFile, String storedEncodingJson) throws Exception {
        if (faceFile == null || faceFile.isEmpty()) {
            throw new Exception("No face file provided.");
        }
        if (storedEncodingJson == null || storedEncodingJson.isEmpty()) {
            throw new Exception("No stored face encoding provided.");
        }

        // Prepare multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(faceFile.getBytes()) {
            @Override
            public String getFilename() {
                return faceFile.getOriginalFilename();
            }
        };
        body.add("faceFile", fileResource);
        body.add("storedEncoding", storedEncodingJson);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Call Python /compare_faces
        ResponseEntity<Map> response = restTemplate.postForEntity(
                compareFaceUrl,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error calling /compare_faces: " + response.getStatusCode());
        }
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new Exception("No response from Python /compare_faces");
        }

        if (responseBody.containsKey("error")) {
            // Possibly "No face detected" or something else
            throw new Exception("Python compare_faces error: " + responseBody.get("error"));
        }

        // "match": true or false
        return (boolean) responseBody.get("match");
    }
}

