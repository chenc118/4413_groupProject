package ca.yorku.user;

import ca.yorku.dal.Item;
import ca.yorku.dal.User;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;
import com.serverless.dal.DynamoDBAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class GetUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String userId = pathParameters.get("uuid");

        User user = new User().get(userId);

        if(user != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(user)
                    .build();
        }
        else{

            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("User with id: '" + userId + "' not found.")
                    .build();
        }
    }

}
