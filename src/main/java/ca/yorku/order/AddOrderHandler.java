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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AddOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

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
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Order newOrder = new Order();
            newOrder.setPlacedDate(new Date());
            if(body.has("comment")) {
                newOrder.setComment(body.get("comment").asText());
            }
            if(body.has("items")&&body.get("items").isArray()){
                ArrayList<Order.ItemInfo> items = new ArrayList<>();
                for(JsonNode i : body.get("items")){
                    Order.ItemInfo it = new Order.ItemInfo();
                    it.setItemId(i.get("itemId").asText());
                    it.setQuantity(i.get("quantity").asInt());
                    items.add(it);
                    Item item = new Item().get(it.getItemId());
                    //this stuff might be screwed up by eventual consistency
                    if(item.getQuantityForSale()<it.getQuantity()){
                        return ApiGatewayResponse.builder()
                            .setStatusCode(400)
                            .setObjectBody("Quantity of item "+it.getItemId()+" exceeds available stock")
                            .build();
                    }
                    else{
                        item.setQuantityForSale(item.getQuantityForSale()-it.getQuantity());
                    }
                    item.setNumSold(item.getNumSold()+ it.getQuantity());
                    item.save();
                }
                newOrder.setItems(items);
            }
//            switch(body.get("status").asText().toLowerCase()){
//                case "placed": newOrder.setStatus(Order.Status.Placed);
//                    break;
//                case "delivered": newOrder.setStatus(Order.Status.Delivered);
//                    break;
//                case "shipped": newOrder.setStatus(Order.Status.Shipped);
//                    break;
//            }
            newOrder.setStatus(Order.Status.Placed);
            newOrder.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newOrder)
                    .build();
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
