package ca.yorku.search;

import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.List;
import java.util.Map;

public class SearchItemsByCategory implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        // get the 'pathParameters' from input
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String categoryId = pathParameters.get("categoryId");

        List<Item> items = new Item().getByCategory(categoryId);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(items)
                .build();
    }
}
