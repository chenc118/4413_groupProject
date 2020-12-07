package ca.yorku.order;

import ca.yorku.BBCAuth;
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
        BBCAuth auth = new BBCAuth(headers.get("Authorization"));
        if(!auth.verified()){
            return ApiGatewayResponse.builder()
                    .setStatusCode(401)
                    .setRawBody("{\"error\":\"Not Authorized\"}")
                    .build();
        }
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String orderId = pathParameters.get("orderId");

        Order order = new Order().get(orderId);

        if(order != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(order)
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("Item with id: '" + orderId + "' not found.")
                    .build();
        }

    }
}
