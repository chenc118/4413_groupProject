package ca.yorku.review;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Item;
import ca.yorku.dal.Order;
import ca.yorku.dal.Review;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AddReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Review newReview = new Review();

            if (body.has("itemId")) {
                newReview.setItemId(body.get("itemId").asText());
                if (new Item().get(newReview.getItemId()) == null) {
                    return StandardResponses.error("Invalid item Id");
                }
            } else {
                return StandardResponses.error("A review must have an itemId field");
            }
            if (body.has("rating")) {
                newReview.setRating(body.get("rating").asInt());
                if (newReview.getRating() <= 0 || newReview.getRating() > 5) {
                    return StandardResponses.error("Rating must be between 1-5 inclusive");
                }
            } else {
                return StandardResponses.error("A review must contain a rating field");
            }
            if (body.has("title")) {
                newReview.setTitle(escapeHTML(body.get("title").asText()));
                if (newReview.getTitle().equalsIgnoreCase("")) {
                    return StandardResponses.error("Review title must not be empty");
                }
            } else {
                return StandardResponses.error("A review must have a title field");
            }
            if (body.has("content")) {
                newReview.setContent(escapeHTML(body.get("content").asText()));
                if (newReview.getTitle().equalsIgnoreCase("")) {
                    return StandardResponses.error("Review content must not be empty");
                }
            } else {
                return StandardResponses.error("A review must have a content field");
            }
            if (auth.verified()) {
                newReview.setUserId(auth.getUserId());
            }
            newReview.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newReview)
                    .build();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: " + ex + sw.toString());

            return StandardResponses.error("Invalid Input");
        }

    }

    //https://stackoverflow.com/questions/1265282/recommended-method-for-escaping-html-in-java
    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
