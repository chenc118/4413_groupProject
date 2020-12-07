package ca.yorku.order;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Item;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class DeleteOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String orderId = pathParameters.get("orderId");
        Order ord = new Order().get(orderId);
        if (ord != null) {
            if (!auth.getUserId().equalsIgnoreCase(ord.getUserId()) && !auth.isAdmin()) {
                return auth.forbidden();
            }
            for (Order.ItemInfo i : ord.getItems()) {
                Item item = new Item().get(i.getItemId());
                //this stuff might be screwed up by eventual consistency
                item.setQuantityForSale(item.getQuantityForSale() + i.getQuantity());
                item.setNumSold(item.getNumSold() - i.getQuantity());
                item.save();
            }
        }
        boolean deleted = new Order().delete(orderId);
        if (deleted) {
            return StandardResponses.message("OK", 200);
        } else {
            return StandardResponses.error("Order not found", 404);
        }

    }
}
