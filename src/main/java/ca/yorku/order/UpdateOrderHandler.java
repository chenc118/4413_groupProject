package ca.yorku.order;

import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class UpdateOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String orderId = pathParameters.get("orderId");

        Order order = new Order().get(orderId);
        if(order==null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Item not found")
                    .build();
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if(body.has("comment")){
                order.setComment(body.get("comment").asText());
            }
            if(body.has("status")){
                switch(body.get("status").asText().toLowerCase()){
                    case "placed": order.setStatus(Order.Status.Placed);
                        break;
                    case "delivered":
                        order.setStatus(Order.Status.Delivered);
                        //order.setDeliveredDate(new Date());
                        break;
                    case "shipped":
                        order.setStatus(Order.Status.Shipped);
                        //order.setShippedDate(new Date());
                        break;
                }
            }
            if(body.has("items")&&body.get("items").isArray()){
                ArrayList<Order.ItemInfo> items = new ArrayList<>();
                for(JsonNode i : body.get("items")){
                    Order.ItemInfo it = new Order.ItemInfo();
                    it.setItemId(body.get("itemId").asText());
                    it.setQuantity(body.get("quantity").asInt());
                    items.add(it);
                }
                order.setItems(items);
            }
            //test setters to test get monthly orders
            if(body.has("placedDate")) {
                order.setPlacedDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .parse(body.get("placedDate").asText()));
            }
            if(body.has("shippedDate")) {
                order.setPlacedDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .parse(body.get("shippedDate").asText()));
            }
            if(body.has("deliveredDate")) {
                order.setPlacedDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .parse(body.get("delieveredDate").asText()));
            }
            order.save();

            ApiGatewayResponse res =  ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(order)
                    .build();
            return res;
        }catch(Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: "+ex+sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Input")
                    .build();
        }

    }

}
