package ca.yorku.item;

import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

public class AddItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

        try {
            // get the 'body' from input
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Item newItem = new Item();
            if(body.has("id")) {
                newItem.setId(body.get("id").asInt());
            }
            newItem.setName(body.get("name").asText());
            if(body.has("price")){
                newItem.setPrice(body.get("price").asDouble());
            }
            if(body.has("quantity")){
                newItem.setQuantity(body.get("quantity").asLong());
            }
            newItem.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(newItem)
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
