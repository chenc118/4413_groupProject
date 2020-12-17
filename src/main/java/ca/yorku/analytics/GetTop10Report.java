package ca.yorku.analytics;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.*;
import java.util.logging.Logger;

public class GetTop10Report implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        if (!auth.isAdmin()) {
            return auth.forbidden();
        }
        List<Item.TopItemInfo> allItems = new Item().topItems();
        PriorityQueue<Item.TopItemInfo> top = new PriorityQueue<>(Comparator.comparingInt(Item.TopItemInfo::getNumSold));
        for (Item.TopItemInfo i : allItems) {
            logger.info("Item scan: " + i.getItemId() + " num " + i.getNumSold());
            top.offer(i);
            if (top.size() > 10) {
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
