package ca.yorku.search;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Item;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.List;
import java.util.Map;

public class SearchOrdersByItem implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        if (!auth.isPartner()) {
            return auth.forbidden();
        }
        // get the 'pathParameters' from input
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String itemId = pathParameters.get("itemId");
        Item item = new Item().get(itemId);
        if (item != null && !auth.getUserId().equalsIgnoreCase(item.getSoldBy()) && !auth.isAdmin()) {
            return auth.forbidden();
        }
        if (item == null) {
            return StandardResponses.error("Item not found", 404);
        }
        List<Order> orders = new Order().getByItem(itemId);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(orders)
                .build();
    }
}
