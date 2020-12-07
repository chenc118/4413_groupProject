package ca.yorku;

import com.amazonaws.util.Base64;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class BBCAuth {
    private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAieFN2HmyeG1uD3vroMvK\n" +
            "zHF/2Cpk1DLavlOLCXfKrpju3HD+ksOwweiOVen17jklEjRV3DlJpuNbXF2iNwDY\n" +
            "0GPM8P4x2obgoj1tmu6DXP4pHKRnFL+ZY9Shjpk5R8EiMNzZ8M/sJX1cc8M3uyb+\n" +
            "b/BdSK2UhSpqkhF1yj7r7nSyLPdCLFGFsDe88n+7qUmkkfNfCp7CvLYCwq3VNlri\n" +
            "1O52ie+ZLuxqHeOhrjpEe8WSghTrkINPhaIo8dO/XouCXSsYt1HSLOIP2jMxZlTo\n" +
            "23r9m7Y26cufDMNg6TrOLv2NL1Y19360CY0OZRYVYa0T9c9cGj5OhHGFDp604syq\n" +
            "UQIDAQAB";

    private DecodedJWT authToken;
    private boolean verified;
    public BBCAuth(String authHeader) {
        try {
            String token = authHeader.substring("Bearer ".length());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] res = Base64.decode(publicKey);
            X509EncodedKeySpec KeySpec = new X509EncodedKeySpec(res);
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(KeySpec);
            Algorithm alg = Algorithm.RSA256(pubKey, null);
            authToken = JWT.require(alg).acceptLeeway(1).build().verify(token);
            verified = true;
        }catch(JWTVerificationException ex){
            verified = false;
        }
        catch(Exception e){};
    }

    public JsonNode getPayload() {
        try {
            return new ObjectMapper().readTree(authToken.getPayload());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean verified(){
        return verified;
    }


}
