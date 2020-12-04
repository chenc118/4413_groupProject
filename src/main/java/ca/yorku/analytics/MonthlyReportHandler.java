package ca.yorku.analytics;

import ca.yorku.dal.Item;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;
import java.util.logging.Logger;

public class MonthlyReportHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
//        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
//        String itemId = pathParameters.get("itemId");
//
//        Item item = new Item().get(itemId);
        new Order().monthlyReport(12,2020);
//        if(item != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
//                    .setObjectBody("itemId: " + item.getId() + ", \n" + "numSold: " + item.getNumSold())
                    .build();
//        }
//        else{
//            return ApiGatewayResponse.builder()
//                    .setStatusCode(404)
//                    .setObjectBody("item with id: '" + itemId + "' not found.")
//                    .build();
//        }
    }
}
