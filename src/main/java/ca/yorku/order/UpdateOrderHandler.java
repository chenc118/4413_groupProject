package ca.yorku.order;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Item;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UpdateOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

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
            HashMap<String, Order.ItemInfo> old = new HashMap<>();
            HashMap<String,Order.ItemInfo> upd = new HashMap<>();
            for(Order.ItemInfo i : order.getItems()){
                old.put(i.getItemId(), i);
            }
            if(body.has("items")&&body.get("items").isArray()){
                ArrayList<Order.ItemInfo> items = new ArrayList<>();
                for(JsonNode i : body.get("items")){
                    Order.ItemInfo it = new Order.ItemInfo();
                    it.setItemId(i.get("itemId").asText());
                    it.setQuantity(i.get("quantity").asInt());
                    upd.put(it.getItemId(), it);
                    items.add(it);
                }
                order.setItems(items);
            }
            for(String k:old.keySet()){
                Item item = new Item().get(k);
                if(upd.containsKey(k)){
                    Order.ItemInfo o = old.get(k);
                    Order.ItemInfo n = upd.get(k);
                    item.setQuantityForSale(item.getQuantityForSale()+ o.getQuantity()-n.getQuantity());
                    item.setNumSold(item.getNumSold()-o.getQuantity()+n.getQuantity());
                    upd.remove(k);
                }
                else{
                    Order.ItemInfo o = old.get(k);
                    item.setQuantityForSale(item.getQuantityForSale()+o.getQuantity());
                    item.setNumSold(item.getNumSold()-o.getQuantity());
                }
                item.save();
            }
            for(String k:upd.keySet()){
                Item item = new Item().get(k);
                Order.ItemInfo n = upd.get(k);
                item.setQuantityForSale(item.getQuantityForSale()+-n.getQuantity());
                item.setNumSold(item.getNumSold()-+n.getQuantity());
                item.save();
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
