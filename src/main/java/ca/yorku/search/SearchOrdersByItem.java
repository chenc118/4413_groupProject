package ca.yorku.search;

import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.List;
import java.util.Map;

public class SearchOrdersByItem implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        // get the 'pathParameters' from input
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String itemId = pathParameters.get("itemId");

        List<Order> orders = new Order().getByItem(itemId);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(orders)
                .build();
    }
}
