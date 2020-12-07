package ca.yorku.category;

import ca.yorku.dal.Category;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;
import java.util.logging.Logger;

public class GetAllCategoriesHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(new Category().listCategories())
                .build();
    }
}
