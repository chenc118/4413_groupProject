package ca.yorku.order;

import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class DeleteOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String orderId = pathParameters.get("orderId");

        boolean deleted = new Order().delete(orderId);
        if(deleted) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setRawBody("OK")
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Order not found")
                    .build();
        }

    }
}
