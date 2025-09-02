package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartOfferApplicationTests {

    private static final String OFFER_API_URL = "http://localhost:9001/api/v1/offer";
    private static final String APPLY_OFFER_API_URL = "http://localhost:9001/api/v1/cart/apply_offer";

    @Test
    void checkFlatXForOneSegment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Offer should be added successfully");
    }

    //Positive Test Cases------>
    @Test
    void testFlatXOfferForP1Segment() throws Exception {
        // Add FLATX offer for p1 segment
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 20, segments);
        boolean result = addOffer(offerRequest);
		System.out.println("Add Offer Response Code :: " + result);
        assertTrue(result, "FLATX offer should be added successfully");
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(180, response.getCart_value(), "Cart value should be reduced by 20");
    }

    @Test
    void testFlatXOfferForP2Segment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p2");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 15, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLATX offer should be added successfully");
        System.out.println("Add Offer Response Code :: " + result);
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(100, 1, 2);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(100, response.getCart_value(), "Cart value should remain unchanged when below minimum threshold of 400");
    }

    @Test
    void testFlatXOfferForP3Segment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p3");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 25, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLATX offer should be added successfully");
		System.out.println("Add Offer Response Code :: " + result);
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(150, 1, 3);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(125, response.getCart_value(), "Cart value should be reduced by 25");
    }

    @Test
    void testFlatPercentageOfferForP1Segment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 10, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(180, response.getCart_value(), "Cart value should be reduced by 10% (20)");
    }

    @Test
    void testFlatPercentageOfferForP2Segment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p2");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 20, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(100, 1, 2);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(100, response.getCart_value(), "Cart value should remain unchanged when below minimum threshold of 400");
    }

    @Test
    void testFlatPercentageOfferForP3Segment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p3");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 15, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 3);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(170, response.getCart_value(), "Cart value should be reduced by 15% (30)");
    }

    @Test
    void testHighDiscountAmount() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 50, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "High FLATX offer should be added successfully");

=        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(150, response.getCart_value(), "Cart value should be reduced by 50");
    }

    @Test
    void testHighDiscountPercentage() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 50, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "High FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(100, response.getCart_value(), "Cart value should be reduced by 50%");
    }
   
		//negative test cases------------>
    @Test
    void testNoOfferForUserSegment() throws Exception {
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(200, response.getCart_value(), "Cart value should remain unchanged when no offer exists");
    }

    @Test
    void testNoOfferForRestaurant() throws Exception {
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 4, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(200, response.getCart_value(), "Cart value should remain unchanged when no offers exist for restaurant");
    }

	//bug in code------->
    @Test
    void testCartValueLessThanDiscount() throws Exception {
        // Logic incorrect -> Not real world scenario
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 150, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "FLATX offer should be added successfully");
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(100, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(-50, response.getCart_value(), "Cart value can be negative when discount exceeds cart value");
    }

    @Test
    void testZeroDiscountValue() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 0, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Zero FLATX offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(200, response.getCart_value(), "Cart value should remain unchanged with zero discount");
    }

    @Test
    void testZeroPercentageDiscount() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 0, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Zero FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(200, response.getCart_value(), "Cart value should remain unchanged with zero percentage discount");
    }

    @Test
    void testDifferentRestaurantOffers() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest restaurant1Offer = new OfferRequest(1, "FLATX", 20, segments);
        addOffer(restaurant1Offer);

        OfferRequest restaurant2Offer = new OfferRequest(2, "FLATX", 30, segments);
        addOffer(restaurant2Offer);

        ApplyOfferRequest request1 = new ApplyOfferRequest(100, 1, 1);
        ApplyOfferResponse response1 = applyOffer(request1);
        assertEquals(100, response1.getCart_value(), "Cart value should remain unchanged when below minimum threshold of 400");

        ApplyOfferRequest request2 = new ApplyOfferRequest(100, 2, 1);
        ApplyOfferResponse response2 = applyOffer(request2);
        assertEquals(100, response2.getCart_value(), "Cart value should remain unchanged when below minimum threshold of 400");
    }

    @Test
    void testUserSegmentApiError404() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 20, segments);
        addOffer(offerRequest);
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 404);
        	        assertThrows(RuntimeException.class, () -> {
            applyOffer(applyRequest);
        }, "Should throw RuntimeException when segment API returns 404");
    }

    @Test
    void testUserSegmentApiError500() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", 20, segments);
        addOffer(offerRequest);
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 500);
                assertThrows(RuntimeException.class, () -> {
            applyOffer(applyRequest);
        }, "Should throw RuntimeException when segment API returns 500");
    }

    @Test
    void testInvalidOfferType() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "INVALID", 20, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Invalid offer type should still be added");
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(200, response.getCart_value(), "Cart value should remain unchanged with invalid offer type");
    }

	//bug in devcode------->
    @Test
    void testNegativeDiscountValue() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLATX", -10, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Negative FLATX offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(210, response.getCart_value(), "Cart value should increased by 10 with negative discount--Failed Test Case");
    }

	//bug in dev code------->
    @Test
    void testNegativePercentageDiscount() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", -10, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "Negative FLAT% offer should be added successfully");

        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(220, response.getCart_value(), "Cart value increased by 10% with negative percentage--Failed Test Case");
    }

    @Test
    void testVeryHighPercentageDiscount() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest = new OfferRequest(1, "FLAT%", 100, segments);
        boolean result = addOffer(offerRequest);
        assertTrue(result, "100% FLAT% offer should be added successfully");

       
        ApplyOfferRequest applyRequest = new ApplyOfferRequest(200, 1, 1);
        ApplyOfferResponse response = applyOffer(applyRequest);
        assertEquals(0, response.getCart_value(), "Cart value should be 0 with 100% discount");
    }


    private boolean addOffer(OfferRequest offerRequest) throws Exception {
        URL url = new URL(OFFER_API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("POST");

        ObjectMapper mapper = new ObjectMapper();
        String POST_PARAMS = mapper.writeValueAsString(offerRequest);

        try (OutputStream os = con.getOutputStream()) {
            os.write(POST_PARAMS.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("Add Offer Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Add Offer Response: " + response);
            }
            return true;
        } else {
            System.out.println("Add Offer request failed.");
            return false;
        }
    }

    private ApplyOfferResponse applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
        URL url = new URL(APPLY_OFFER_API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("POST");

        ObjectMapper mapper = new ObjectMapper();
        String POST_PARAMS = mapper.writeValueAsString(applyOfferRequest);

        try (OutputStream os = con.getOutputStream()) {
            os.write(POST_PARAMS.getBytes());
            os.flush();
        }

        int responseCode = con.getResponseCode();
        System.out.println("Apply Offer Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Apply Offer Response: " + response);
                return mapper.readValue(response.toString(), ApplyOfferResponse.class);
            }
        } else {
            System.out.println("Apply Offer request failed.");
            throw new RuntimeException("Apply Offer request failed with code: " + responseCode);
        }
    }
}
