package ca.yorku.category;

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
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String categoryId = pathParameters.get("categoryId");

        Category category = new Category().get(categoryId);
        if(category==null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setRawBody("Category not found")
                    .build();
        }
        try{
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            if(body.has("categoryId")){
                category.setCategoryId(body.get("categoryId").asText());
            }
            if(body.has("name")){
                category.setName(body.get("name").asText());
            }

            category.save();

            ApiGatewayResponse res = ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(category)
                    .build();
            return res;
        } catch (Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving category: "+ex+sw.toString());

            return ApiGatewayResponse.builder()
                    .setStatusCode(400)
                    .setObjectBody("Invalid Data")
                    .build();
        }
    }
}
