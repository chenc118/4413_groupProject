package ca.yorku;

import com.serverless.ApiGatewayResponse;

public class StandardResponses {
    public static ApiGatewayResponse forbidden() {
        return ApiGatewayResponse.builder()
                .setStatusCode(403)
                .setRawBody("{\"error\":\"Forbidden\"}")
                .build();
    }

    public static ApiGatewayResponse deny() {
        return ApiGatewayResponse.builder()
                .setStatusCode(401)
                .setRawBody("{\"error\":\"Not Authorized\"}")
                .build();
    }

    public static ApiGatewayResponse error(String message) {
        return ApiGatewayResponse.builder()
                .setStatusCode(400)
                .setRawBody("{\"error\":\"" + message + "\"}")
                .build();
    }

    public static ApiGatewayResponse error(String message, int status) {
        return ApiGatewayResponse.builder()
                .setStatusCode(status)
                .setRawBody("{\"error\":\"" + message + "\"}")
                .build();
    }

    public static ApiGatewayResponse message(String message, int status) {
        return ApiGatewayResponse.builder()
                .setStatusCode(status)
                .setRawBody("{\"status\":" + status + ",\"message\":\"" + message + "\"}")
                .build();
    }
}
