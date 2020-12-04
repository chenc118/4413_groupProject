package ca.yorku.analytics;

import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class GetTop10Report implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    //private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        List<Item> allItems = new Item().getAll();
        PriorityQueue<Item> top = new PriorityQueue<>(Comparator.comparingInt(Item::getNumSold));
        for(Item i:allItems){
            //logger.info("Item scan: "+i.getId());
            System.out.println("Item scan: "+i.getId());
            top.offer(i);
            if(top.size()>10){
                top.poll();
            }
        }
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(top)
                .build();
    }
}
