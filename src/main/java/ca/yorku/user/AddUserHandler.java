package ca.yorku.user;

import ca.yorku.dal.User;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

public class AddUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        try {
            // get the 'body' from input
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            User newUser = new User();
            newUser.setUserId(body.get("userId").asInt());
            newUser.setFirstName(body.get("firstName").asText());
            newUser.setLastName(body.get("lastName").asText());
            newUser.setEmail(body.get("email").asText());
            newUser.setPassword(body.get("password").asText());
            if (body.has("phone")) {
                newUser.setPhone(body.get("phone").asText());
            }
            //Likely need another endpoint to update group to higher privledges
            newUser.setGroup(User.UserGroup.Customer);
            if (body.has("identity")) {
                newUser.setCognitoIdentity(body.get("identity").asText());
            }

            newUser.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newUser)
                    .build();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in adding user: " + ex + sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Input")
                    .build();
        }
    }
}
