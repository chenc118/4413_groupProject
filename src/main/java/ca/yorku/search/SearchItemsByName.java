package ca.yorku.search;

import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.List;
import java.util.Map;

public class SearchItemsByName implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        // get the 'pathParameters' from input
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String itemName = pathParameters.get("itemName");

        List<Item.ItemId> items = new Item().itemByName(itemName);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(items)
                .build();
    }
}
