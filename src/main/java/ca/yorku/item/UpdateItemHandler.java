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

public class UpdateItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

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
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String itemId = pathParameters.get("itemId");


        Item item = new Item().get(itemId);
        if (item != null && !auth.getUserId().equalsIgnoreCase(item.getSoldBy()) && !auth.isAdmin()) {
            return auth.forbidden();
        }
        if (item == null) {
            return StandardResponses.error("Item not found", 404);
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if (body.has("name")) {
                item.setName(body.get("name").asText());
                if (item.getName().equalsIgnoreCase("")) {
                    return StandardResponses.error("Item name must not be empty");
                }
            }
            if (body.has("categoryId")) {
                item.setCategoryId(body.get("categoryId").asText());
                if (new Category().get(item.getCategoryId()) == null) {
                    return StandardResponses.error("Invalid category Id");
                }
            }
            if (body.has("price")) {
                item.setPrice(body.get("price").asDouble());
                if (item.getPrice() < 0) {
                    return StandardResponses.error("Item price must be >= 0");
                }
            }
            if (body.has("quantityForSale")) {
                item.setQuantityForSale(body.get("quantityForSale").asLong());
                if (item.getQuantityForSale() < 0) {
                    return StandardResponses.error("Item quantity for sale must be >= 0");
                }
            }
            if (body.has("numSold")) {
                item.setNumSold(body.get("numSold").asInt());
                if (item.getNumSold() < 0) {
                    return StandardResponses.error("Item num sold must be >= 0");
                }
            }
            if (body.has("soldBy") && auth.isAdmin()) {
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
            if (body.has("image")) {
                item.setImage(body.get("image").asText());
            }
            if (body.has("description")) {
                item.setDescription(body.get("description").asText());
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
            return StandardResponses.error("Invalid Input");
        }
    }
}
