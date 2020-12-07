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
import java.util.Map;
import java.util.logging.Logger;

public class UpdateReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String reviewId = pathParameters.get("reviewId");

        Review review = new Review().get(reviewId);
        if (review != null && !auth.getUserId().equalsIgnoreCase(review.getUserId()) && !auth.isAdmin()) {
            return auth.forbidden();
        }
        if (review == null) {
            return StandardResponses.error("Review not found", 404);
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if (body.has("itemId")) {
                review.setItemId(body.get("itemId").asText());
                if (new Item().get(review.getItemId()) == null) {
                    return StandardResponses.error("Invalid item Id");
                }
            }
            if (body.has("rating")) {
                review.setRating(body.get("rating").asInt());
                if (review.getRating() <= 0 || review.getRating() > 5) {
                    return StandardResponses.error("Rating must be between 1-5 inclusive");
                }
            }
            if (body.has("title")) {
                review.setTitle(escapeHTML(body.get("title").asText()));
                if (review.getTitle().equalsIgnoreCase("")) {
                    return StandardResponses.error("Review title must not be empty");
                }
            }
            if (body.has("content")) {
                review.setContent(escapeHTML(body.get("content").asText()));
                if (review.getContent().equalsIgnoreCase("")) {
                    return StandardResponses.error("Review content must not be empty");
                }
            }
            review.save();

            ApiGatewayResponse res = ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(review)
                    .build();
            return res;
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
