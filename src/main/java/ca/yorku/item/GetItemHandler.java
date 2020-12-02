package ca.yorku.item;

import ca.yorku.dal.Item;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.serverless.dal.DynamoDBAdapter;

import java.util.Collections;
import java.util.Map;

public class GetItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        int itemId = Integer.parseInt(pathParameters.get("id"));

        Item item = new Item().get(itemId);


        if(item != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item)
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("Item with id: '" + itemId + "' not found.")
                    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
                    .build();
        }
    }

}
