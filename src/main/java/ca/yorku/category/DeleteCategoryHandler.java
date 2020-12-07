package ca.yorku.category;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Category;
import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;

public class DeleteCategoryHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>  {
	@Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		Map<String, String> headers = (Map<String, String>) input.get("headers");
		BBCAuth auth = new BBCAuth(headers.get("Authorization"));
		if(!auth.verified()){
			return ApiGatewayResponse.builder()
					.setStatusCode(401)
					.setRawBody("{\"error\":\"Not Authorized\"}")
					.build();
		}
		Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
		String categoryId = pathParameters.get("categoryId");

		boolean deleted = new Category().delete(categoryId);
		if(deleted) {
			return ApiGatewayResponse.builder()
					.setStatusCode(200)
					.setRawBody("OK")
					.build();
		}
		else{
			return ApiGatewayResponse.builder()
					.setStatusCode(404)
					.setRawBody("Item not found")
					.build();
		}
	}
	
}