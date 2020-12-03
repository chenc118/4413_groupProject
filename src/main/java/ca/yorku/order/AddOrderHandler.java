package ca.yorku.order;

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
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AddOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Order newOrder = new Order();
            newOrder.setPlacedDate(new Date());
//            newOrder.setPlacedDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
//                    .parse(body.get("placedDate").asText()));
            if(body.has("comment")) {
                newOrder.setComment(body.get("comment").asText());
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
