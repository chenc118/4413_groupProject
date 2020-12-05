package ca.yorku.user;

import ca.yorku.dal.User;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UpdateUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String userId = pathParameters.get("userId");

        User user = new User().get(userId);

        if (user == null) {
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Item not found")
                    .build();
        }
        try{
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            if (body.has("userId")) {
                user.setUserId(body.get("userId").asInt());
            }
            if (body.has("firstName")) {
                user.setFirstName(body.get("firstName").asText());
            }
            if (body.has("lastName")) {
                user.setLastName(body.get("lastName").asText());
            }
            if (body.has("email")) {
                user.setEmail(body.get("email").asText());
            }
            if (body.has("password")) {
                user.setPassword(body.get("password").asText());
            }
            if (body.has("phone")) {
                user.setPhone(body.get("phone").asText());
            }

            user.save();

            ApiGatewayResponse res = ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(user)
                    .build();
            return res;
        }catch (Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving user: " + ex + sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Input")
                    .build();
        }
    }

}
