package ca.yorku.review;

import ca.yorku.dal.Order;
import ca.yorku.dal.Review;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class GetReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String reviewId = pathParameters.get("reviewId");

        Review review = new Review().get(reviewId);
        //TODO checks to make sure that the category is valid/other resonse codes
        if(review != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(review)
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("Item with id: '" + reviewId + "' not found.")
                    .build();
        }

    }
}
