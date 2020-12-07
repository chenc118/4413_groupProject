package ca.yorku.item;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Category;
import ca.yorku.dal.Item;
import ca.yorku.dal.Review;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AddItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        if (!auth.isPartner()) {
            return auth.forbidden();
        }

        try {
            // get the 'body' from input
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Item newItem = new Item();
            if (body.has("categoryId")) {
                newItem.setCategoryId(body.get("categoryId").asText());
                if (new Category().get(newItem.getCategoryId()) == null) {
                    return StandardResponses.error("Category Id must be a valid ID");
                }
            } else {
                return StandardResponses.error("Item must have a categoryId field");
            }
            if (body.has("name")) {
                newItem.setName(body.get("name").asText());
            } else {
                return StandardResponses.error("Item must have a name field");
            }
            if (body.has("price")) {
                newItem.setPrice(body.get("price").asDouble());
                if (newItem.getPrice() < 0) {
                    return StandardResponses.error("Price must be >=0");
                }
            } else {
                return StandardResponses.error("Item must have a price field");
            }
            if (body.has("quantityForSale")) {
                newItem.setQuantityForSale(body.get("quantityForSale").asLong());
                if (newItem.getQuantityForSale() < 0) {
                    return StandardResponses.error("Quantity for sale must be >= 0");
                }
            } else {
                return StandardResponses.error("Item must have a quantityForSale field");
            }
            if (body.has("numSold")) {
                newItem.setNumSold(body.get("numSold").asInt());
                if (newItem.getNumSold() < 0) {
                    return StandardResponses.error("Num sold must be >= 0");
                }
            }
            newItem.setSoldBy(auth.getUserId());
            if (body.has("reviews") && body.get("reviews").isArray()) {
                List<Review.ReviewId> reviewList = new ArrayList<>();
                for (JsonNode reviewId : body.get("reviews")) {
                    Review.ReviewId r = new Review.ReviewId();
                    r.setReviewId(reviewId.get("reviewId").asText());
                    reviewList.add(r);
                }
                newItem.setReviews(reviewList);
            }
            if (body.has("image")) {
                newItem.setImage(body.get("image").asText());
            }
            if (body.has("description")) {
                newItem.setDescription(body.get("description").asText());
            }
            newItem.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newItem)
                    .build();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: " + ex + sw.toString());
            return StandardResponses.error("Invalid Input");
        }

    }
}
