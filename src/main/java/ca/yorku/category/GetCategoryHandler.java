package ca.yorku.category;

import ca.yorku.dal.Category;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;
import java.util.logging.Logger;

public class GetCategoryHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String categoryID = pathParameters.get("Id");

        Category category = new Category().get(categoryID);

        if(category != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(category)
                    .build();
        }
        else{
            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("Category with id: '" + categoryID + "' not found.")
                    .build();
        }
    }
}
