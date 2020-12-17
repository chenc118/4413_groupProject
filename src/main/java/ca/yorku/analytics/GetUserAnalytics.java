package ca.yorku.analytics;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class GetUserAnalytics implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        if (!auth.isAdmin()) {
            return auth.forbidden();
        }

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(new Order().userReport())
                .build();
    }
}
