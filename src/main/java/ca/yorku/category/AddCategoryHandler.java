package ca.yorku.category;

import ca.yorku.dal.Category;
import ca.yorku.dal.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

public class AddCategoryHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>  {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		
		try{
			JsonNode body = new ObjectMapper().readTree((String) input.get("body"));
			Category newCategory = new Category();
			
			if(body.has("name")) {
				newCategory.setName(body.get("name").asText());
			}
			if(body.has("categoryId")){
				newCategory.setCategoryId(body.get("categoryId").asText());
			}
			
			newCategory.save();
			return ApiGatewayResponse.builder()
				.setStatusCode(201)
				.setObjectBody(newCategory)
				.build();
		} catch(Exception ex){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			logger.severe("Error in adding category: "+ex+sw.toString());
			
			return ApiGatewayResponse.builder()
				.setStatusCode(400)
				.setObjectBody("Invalid data")
				.build();
		}
		
	}
}
