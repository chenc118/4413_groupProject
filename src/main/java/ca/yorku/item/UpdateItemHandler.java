package ca.yorku.item;

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

public class UpdateItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String itemId = pathParameters.get("itemId");

        Item item = new Item().get(itemId);
        if (item == null) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Item not found")
                    .build();
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if (body.has("name")) {
                item.setName(body.get("name").asText());
            }
            if (body.has("category")) {
                item.setCategory(body.get("category").asText());
            }
            if (body.has("price")) {
                item.setPrice(body.get("price").asDouble());
            }
            if (body.has("quantity")) {
                item.setQuantity(body.get("quantity").asLong());
            }
            if (body.has("numSold")) {
                item.setNumSold(body.get("numSold").asInt());
            }
            if (body.has("soldBy")) {
                item.setSoldBy(body.get("soldBy").asText());
            }
            if (body.has("reviews") && body.get("reviews").isArray()) {
                List<Review.ReviewId> reviewList = new ArrayList<>();
                for (JsonNode reviewId : body.get("reviews")) {
                    Review.ReviewId r = new Review.ReviewId();
                    r.setReviewId(reviewId.get("reviewId").asText());
                    reviewList.add(r);
                }
                item.setReviews(reviewList);
            }
            item.save();

            ApiGatewayResponse res = ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item)
                    .build();
            return res;
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: " + ex + sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Input")
                    .build();
        }
    }
}
