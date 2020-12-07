package ca.yorku.review;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Review;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class DeleteReviewHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
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

        boolean deleted = new Review().delete(reviewId);


        if (deleted) {
            return StandardResponses.message("OK", 404);
        } else {
            return StandardResponses.error("Review not found", 404);
        }
    }
}
