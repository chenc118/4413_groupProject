package ca.yorku.order;

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
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String orderId = pathParameters.get("orderId");
        Order ord = new Order().get(orderId);
        if(ord!=null) {
            for (Order.ItemInfo i : ord.getItems()) {
                Item item = new Item().get(i.getItemId());
                //this stuff might be screwed up by eventual consistency
                item.setQuantityForSale(item.getQuantityForSale() + i.getQuantity());
                item.setNumSold(item.getNumSold() - i.getQuantity());
                item.save();
            }
        }
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
