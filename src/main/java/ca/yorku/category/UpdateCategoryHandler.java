package ca.yorku.category;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Category;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

public class UpdateCategoryHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
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
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String categoryId = pathParameters.get("categoryId");

        Category category = new Category().get(categoryId);
        if (category == null) {
            return StandardResponses.error("Category not found", 404);
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
            if (body.has("name")) {
                category.setName(body.get("name").asText());
                if (category.getName().equalsIgnoreCase("")) {
                    return StandardResponses.error("Name must not be empty");
                }
            }
            category.save();

            ApiGatewayResponse res = ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(category)
                    .build();
            return res;
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving category: " + ex + sw.toString());
            return StandardResponses.error("Invalid Input");
        }
    }
}
