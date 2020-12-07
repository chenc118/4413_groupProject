package ca.yorku.order;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Collections;
import java.util.Map;

public class GetOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String orderId = pathParameters.get("orderId");

        Order order = new Order().get(orderId);
        if (order != null) {
            if (!auth.getUserId().equalsIgnoreCase(order.getUserId()) && !auth.isAdmin()) {
                return auth.forbidden();
            }
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(order)
                    .build();
        } else {
            return StandardResponses.error("Item with id: '" + orderId + "' not found.", 404);
        }

    }
}
