package com.example.Online.Voting.service;

import com.example.Online.Voting.model.AadhaarDetails;
import com.example.Online.Voting.repository.AadhaarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AadhaarService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OTPService otpService;

    @Autowired
    private AadhaarRepository aadhaarRepository;

    // URL for the Python OCR microservice (adjust host/port as necessary)
    private final String OCR_ENDPOINT = "http://localhost:5000/aadhaar_ocr";

    /**
     * Processes the uploaded Aadhaar photo by sending it to the OCR service,
     * validates the extracted Aadhaar number, sets the manually provided mobile number,
     * and generates an OTP.
     *
     * @param aadhaarPhoto the uploaded Aadhaar card image.
     * @param mobileNumber the mobile number manually provided by the user.
     * @return AadhaarDetails containing extracted data and generated OTP.
     * @throws Exception if the OCR extraction fails or the Aadhaar number is invalid.
     */
    public AadhaarDetails verifyAadhaar(MultipartFile aadhaarPhoto, String mobileNumber) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity(aadhaarPhoto, mobileNumber, headers);

        // 3) POST to "http://localhost:5000/aadhaar_ocr"
        ResponseEntity<AadhaarDetails> response =
                restTemplate.postForEntity(OCR_ENDPOINT, requestEntity, AadhaarDetails.class);


        AadhaarDetails details = getAadhaarDetails(response);
        details.setMobileNumber(mobileNumber);

        if (aadhaarRepository.existsByMobileNumber(details.getMobileNumber())) {
            throw  new RuntimeException("Mobile number already in use: " + details.getMobileNumber());
        }

        //calling otp
        otpService.generateOTP(mobileNumber);
        return details;
    }

    private AadhaarDetails getAadhaarDetails(ResponseEntity<AadhaarDetails> response) throws Exception {
        AadhaarDetails details = response.getBody();


        // 4) Validate Aadhaar number is 12 digits
        if (details == null || !isValidAadhaar(details.getAadhaarNumber())) {
            throw new Exception("Invalid Aadhaar number extracted from the uploaded image.");
        }


        if(aadhaarRepository.existsByAadhaarNumber(details.getAadhaarNumber())) {
            throw new RuntimeException("This aadhar is already registered!!");
        }


        if (details.getAge() == null || details.getAge() < 18) {
            // Throw an exception or return a custom error response
            throw new Exception("User is under 18 and not eligible to create an account.");
        }
        return details;
    }

    private static HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity(MultipartFile aadhaarPhoto, String mobileNumber, HttpHeaders headers) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 1) File must be named "aadhaarFile"
        ByteArrayResource fileResource = new ByteArrayResource(aadhaarPhoto.getBytes()) {
            @Override
            public String getFilename() {
                return aadhaarPhoto.getOriginalFilename();
            }
        };
        body.add("aadhaarFile", fileResource);

        // 2) Also send "mobileNumber"
        body.add("mobileNumber", mobileNumber);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return requestEntity;
    }

    /**
     * Helper method to validate that the Aadhaar number is a 12-digit numeric string.
     *
     * @param aadhaarNumber the Aadhaar number to validate.
     * @return true if valid; false otherwise.
     */
    private boolean isValidAadhaar(String aadhaarNumber) {
        return aadhaarNumber != null && aadhaarNumber.matches("^\\d{12}$");
    }

    public AadhaarDetails saveDetails(AadhaarDetails details) {
        return aadhaarRepository.save(details);
    }
}

