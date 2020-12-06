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
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AddReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>  {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Review newReview = new Review();

            if(body.has("itemId")){
                newReview.setItemId(body.get("itemId").asText());
            }
            if(body.has("rating")){
                newReview.setRating(body.get("rating").asInt());
            }
            if(body.has("title")){
                newReview.setTitle(body.get("title").asText());
            }
            if(body.has("content")){
                newReview.setContent(body.get("content").asText());
            }

            newReview.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newReview)
                    .build();
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
