package ca.yorku.analytics;

import ca.yorku.dal.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.*;
import java.util.logging.Logger;

public class GetTop10Report implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        List<Map<String, AttributeValue>> allItems = new Item().getAll();
        PriorityQueue<Map<String, AttributeValue>> top = new PriorityQueue<>(
                Comparator.comparingInt(o -> Integer.parseInt(o.get("numSold").getN())));
        for(Map<String, AttributeValue> i:allItems){
            logger.info("Item scan: "+i.get("id").getS()+" num "+i.get("numSold").getN());
            top.offer(i);
            if(top.size()>10){
                top.poll();
            }
        }
        logger.info("Iter Done");
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(top)
                .build();
    }
}
