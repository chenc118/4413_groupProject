package ca.yorku.item;

import ca.yorku.dal.Item;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.Base64;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.serverless.ApiGatewayResponse;
import com.serverless.dal.DynamoDBAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class GetItemHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String itemId = pathParameters.get("itemId");
        try {
            Map<String, String> headers = (Map<String, String>) input.get("headers");
            System.out.println("headers" + headers);
            Map<String, String[]> mHeaders = (Map<String, String[]>) input.get("multiValueHeaders");
            System.out.println(input);
            System.out.println("mHeaders" + mHeaders);
            String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAieFN2HmyeG1uD3vroMvK";
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] res = Base64.decode(publicKey);
            X509EncodedKeySpec KeySpec = new X509EncodedKeySpec(res);
            RSAPublicKey pubKey = (RSAPublicKey)keyFactory.generatePublic(KeySpec);
            Algorithm alg = Algorithm.RSA256(pubKey,null);
            System.out.println(JWT.require(alg).build().verify(headers.get("Authorization")).getPayload());

        }catch(Exception ex){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: " + ex + sw.toString());
        }
        Item item = new Item().get(itemId);

        if(item != null){
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(item)
                    .build();
        }
        else{

            return ApiGatewayResponse.builder()
                    .setStatusCode(404)
                    .setObjectBody("Item with id: '" + itemId + "' not found.")
                    .build();
        }
    }

}
