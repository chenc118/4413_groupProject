package ca.yorku.review;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Review;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class DeleteReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>  {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"));
        if(!auth.verified()){
            return ApiGatewayResponse.builder()
                    .setStatusCode(401)
                    .setRawBody("{\"error\":\"Not Authorized\"}")
                    .build();
        }
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String reviewId = pathParameters.get("reviewId");

        boolean deleted = new Review().delete(reviewId);


        if(deleted) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setRawBody("OK")
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Review not found")
                    .build();
        }
    }
}
