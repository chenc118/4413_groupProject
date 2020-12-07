package ca.yorku;

import java.util.Base64;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;
import lombok.Getter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;

public class BBCAuth {
    private static String idPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiILq5rV+7jJclbJwMQ9RBXKef+pMhjUoWJjlRfKGoNQG2PRGQ5by9xgB5MPfgN/NqGWu4HE5nzna+QpciF4vtj7KI7+sOvubzyjNXn98TXErJoaQcZ5WKt/ww4FFXocZuHtf0AyeF4ZQ62fU0uOCHlkUELJpu/Seb9LwUPq/Km5MLnRd+lasMxSYGwcsixhpEOX0EDQhNKt1dp3SWPtwsDyjVHU+loGN/4wl9B11LeH8HsrSFFTfFaWjHC1TSRr7S6ZDAj4h0P0naS0gn9Y/nCLGrBb5ZQWbNG7AgJK42xynPaUE5A5b3dVAECoKYpi9AGBjAUSUUnnvK2OUDf39vwIDAQAB";
    private static String authPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArYZv8CGo4/It+ZwLYeV9VKnNjPxmlwVjdd7U2KmE+c/nmMgJfPWxfFL3uCApiJ+IjrUF8wMBotQaUWc8s0LHairEV6okSzklQ0XaHFQYOXNg7/PpB71JLenOLf5R3svuO8vHHDdND83E5/jp9tX3azam227+V2iO4xv9hPbge+wwHPOdAIHkUT7Vr7k9LjwxR41MHsp3mdQoSukRKm+/HGEMsDRHjJE/mxdzrafNejelfnqkXfBl/mxk/Te+F/qdthsaHSLk6GSBkp4ydIvcTz2Vgwrxw9Se6/LnOO31l4ahtYP3Pigolsnp9x5AReqPu62MwY/iOMF23LcPgF6DUQIDAQAB";
    private DecodedJWT authToken;
    private DecodedJWT idToken;
    private JsonNode authJson;
    private JsonNode idJson;
    private boolean verified = false;
    @Getter
    private boolean isPartner = false;
    @Getter
    private boolean isAdmin = false;
    @Getter
    private String userId;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public BBCAuth(String authHeader, String idHeader) {
        try {
            String token = authHeader.substring("Bearer ".length());
            String token2 = idHeader.substring("Bearer ".length());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] res = Base64.getDecoder().decode(authPublicKey);
            X509EncodedKeySpec KeySpec = new X509EncodedKeySpec(res);
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(KeySpec);
            Algorithm alg = Algorithm.RSA256(pubKey, null);
            JWTVerifier veri = JWT.require(alg).acceptLeeway(1).build();
            authToken = veri.verify(token);
            res = Base64.getDecoder().decode(idPublicKey);
            KeySpec = new X509EncodedKeySpec(res);
            pubKey = (RSAPublicKey) keyFactory.generatePublic(KeySpec);
            alg = Algorithm.RSA256(pubKey, null);
            veri = JWT.require(alg).acceptLeeway(1).build();
            idToken = veri.verify(token2);
            verified = true;
            authJson = new ObjectMapper().readTree(Base64.getDecoder().decode(authToken.getPayload()));
            if (authJson.has("cognito:groups")) {
                for (JsonNode group : authJson.get("cognito:groups")) {
                    if (group.asText().equalsIgnoreCase("Administrators")) {
                        isAdmin = true;
                        //admin also has all partner privileges
                        isPartner = true;
                    }
                    if (group.asText().equalsIgnoreCase("Partners")) {
                        isPartner = true;
                    }
                }
            }
            logger.info(authJson.asText());
            userId = authJson.get("username").asText();
            idJson = new ObjectMapper().readTree(Base64.getDecoder().decode(idToken.getPayload()));
            logger.info(idJson.asText());
        } catch (JWTVerificationException ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in adding category: " + ex + sw.toString());
            verified = false;
            ex.printStackTrace();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.severe("Error in adding category: " + e + sw.toString());
            e.printStackTrace();
        }
        ;
    }

    public JsonNode getPayload() {
        return authJson;
    }

    public JsonNode getIDPayload() {
        return idJson;
    }

    public boolean verified() {
        return verified;
    }

    public ApiGatewayResponse forbidden() {
        return ApiGatewayResponse.builder()
                .setStatusCode(403)
                .setRawBody("{\"error\":\"Forbidden\"}")
                .build();
    }

    public ApiGatewayResponse deny() {
        return ApiGatewayResponse.builder()
                .setStatusCode(401)
                .setRawBody("{\"error\":\"Not Authorized\"}")
                .build();
    }
}
