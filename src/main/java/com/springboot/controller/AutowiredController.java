package com.springboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class AutowiredController {


	List<OfferRequest> allOffers = new ArrayList<>();

	@PostMapping(path = "/api/v1/offer")
	public ApiResponse postOperation(@RequestBody OfferRequest offerRequest) {
		System.out.println(offerRequest);
		allOffers.add(offerRequest);
		return new ApiResponse("success");
	}

	@PostMapping(path = "/api/v1/cart/apply_offer")
	public ApplyOfferResponse applyOffer(@RequestBody ApplyOfferRequest applyOfferRequest) throws Exception {
		System.out.println(applyOfferRequest);
		int cartVal = applyOfferRequest.getCart_value();
		SegmentResponse segmentResponse = getSegmentResponse(applyOfferRequest.getUser_id());
		Optional<OfferRequest> matchRequest = allOffers.stream().filter(x->x.getRestaurant_id()==applyOfferRequest.getRestaurant_id())
				.filter(x->x.getCustomer_segment().contains(segmentResponse.getSegment()))
				.findFirst();

		if(matchRequest.isPresent()){
			System.out.println("got a match");
//			System.out.println(matchRequest.get());
			OfferRequest gotOffer = matchRequest.get();

			if(gotOffer.getOffer_type().equals("FLATX")) {
				cartVal = cartVal - gotOffer.getOffer_value();
			} else {
				cartVal = (int) (cartVal - cartVal * gotOffer.getOffer_value()*(0.01));
			}

		}
		return new ApplyOfferResponse(cartVal);
	}

	private SegmentResponse getSegmentResponse(int userid) throws Exception
	{
		String urlString = "http://localhost:1080/api/v1/user_segment?" + "user_id=" + userid;
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("accept", "application/json");

		// Check the response code
		int responseCode = connection.getResponseCode();
		System.out.println("Segment API Response Code: " + responseCode);
		
		if (responseCode != HttpURLConnection.HTTP_OK) {
			throw new RuntimeException("Segment API returned error code: " + responseCode);
		}

		InputStream responseStream = connection.getInputStream();

		ObjectMapper mapper = new ObjectMapper();
		SegmentResponse segmentResponse = mapper.readValue(responseStream, SegmentResponse.class);
		System.out.println("got segment response" + segmentResponse);
		
		return segmentResponse;
	}


}
