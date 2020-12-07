package ca.yorku.analytics;

import ca.yorku.BBCAuth;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.ApiGatewayResponse;

import java.util.Map;
import java.util.logging.Logger;

public class MonthlyReportHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

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
        Map<String, String> pathParameters = (Map<String, String>) input.get("pathParameters");
        String yearMonth = pathParameters.get("year-month");

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(new Order().monthlyReport(yearMonth))
                .build();
    }
}
