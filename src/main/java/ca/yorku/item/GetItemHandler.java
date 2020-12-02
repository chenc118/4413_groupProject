package ca.yorku.item;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class GetItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        ApiGatewayResponse res =  ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody("GetItem")
                .build();
        return res;

    }

}
