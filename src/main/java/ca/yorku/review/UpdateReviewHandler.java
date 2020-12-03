package ca.yorku.review;

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
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String reviewId = pathParameters.get("reviewId");

        Review review = new Review().get(reviewId);
        if(review==null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Item not found")
                    .build();
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if(body.has("orderId")){
                review.setOrderId(body.get("orderId").asText());
            }
            if(body.has("itemId")){
                review.setItemId(body.get("itemId").asText());
            }
            if(body.has("rating")){
                review.setRating(body.get("rating").asInt());
            }
            if(body.has("title")){
                review.setTitle(body.get("title").asText());
            }
            if(body.has("content")){
                review.setContent(body.get("content").asText());
            }
            review.save();

            ApiGatewayResponse res =  ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(review)
                    .build();
            return res;
        }catch(Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: "+ex+sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Input")
                    .build();
        }

    }
}
