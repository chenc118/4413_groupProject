package ca.yorku.order;

import ca.yorku.BBCAuth;
import ca.yorku.StandardResponses;
import ca.yorku.dal.Address;
import ca.yorku.dal.Item;
import ca.yorku.dal.Order;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.ApiGatewayResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

public class AddOrderHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        Map<String, String> headers = (Map<String, String>) input.get("headers");
        BBCAuth auth = new BBCAuth(headers.get("Authorization"), headers.get("identification"));
        if (!auth.verified()) {
            return auth.deny();
        }
        try {
            JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

            Order newOrder = new Order();
            if (body.has("comment")) {
                if (body.get("comment").asText().equalsIgnoreCase("")) {
                    return StandardResponses.error("The comment field must be non-empty if supplied");
                }
                newOrder.setComment(body.get("comment").asText());
            }
            if (body.has("items") && body.get("items").isArray()) {
                ArrayList<Order.ItemInfo> items = new ArrayList<>();
                for (JsonNode i : body.get("items")) {
                    Order.ItemInfo it = new Order.ItemInfo();
                    it.setItemId(i.get("itemId").asText());
                    it.setQuantity(i.get("quantity").asInt());
                    if (it.getQuantity() <= 0) {
                        return StandardResponses.error("Item quantity must be greater than zero");
                    }
                    items.add(it);
                    Item item = new Item().get(it.getItemId());
                    if (item == null) {
                        return StandardResponses.error("Invalid item id in items array");
                    }
                    //this stuff might be screwed up by eventual consistency
                    if (item.getQuantityForSale() < it.getQuantity()) {
                        return StandardResponses.error("Quantity of item " + it.getItemId() + " exceeds available stock");
                    } else {
                        item.setQuantityForSale(item.getQuantityForSale() - it.getQuantity());
                    }
                    item.setNumSold(item.getNumSold() + it.getQuantity());
                    item.save();
                }
                if (items.size() <= 0) {
                    return StandardResponses.error("An order must have 1 or more items");
                }
                newOrder.setItems(items);
            } else {
                return StandardResponses.error("An order must have 1 or more items");
            }

            newOrder.setPlacedDate(new Date());
            newOrder.setStatus(Order.Status.Placed);
            newOrder.setUserId(auth.getUserId());

            //setting addresses
            JsonNode idPayload = auth.getIDPayload();
            Address shipping = new Address();
            if(idPayload.has("custom:shipping-name")) {
                shipping.setName(idPayload.get("custom:shipping-name").asText());
            }
            if(idPayload.has("custom:shipping-street")){
                shipping.setStreet(idPayload.get("custom:shipping-street").asText());
            }
            if(idPayload.has("custom:shipping-city")){
                shipping.setCity(idPayload.get("custom:shipping-city").asText());
            }
            if(idPayload.has("custom:shipping-province")){
                shipping.setProvince(idPayload.get("custom:shipping-province").asText());
            }
            if(idPayload.has("custom:shipping-postalCode")){
                shipping.setPostalCode(idPayload.get("custom:shipping-postalCode").asText());
            }
            if(idPayload.has("custom:shipping-country")){
                shipping.setCountry(idPayload.get("custom:shipping-country").asText());
            }
            newOrder.setShippingAddress(shipping);
            Address billing = new Address();
            if(idPayload.has("custom:billing-name")) {
                billing.setName(idPayload.get("custom:billing-name").asText());
            }
            if(idPayload.has("custom:billing-street")){
                billing.setStreet(idPayload.get("custom:billing-street").asText());
            }
            if(idPayload.has("custom:billing-city")){
                billing.setCity(idPayload.get("custom:billing-city").asText());
            }
            if(idPayload.has("custom:billing-province")){
                billing.setProvince(idPayload.get("custom:billing-province").asText());
            }
            if(idPayload.has("custom:billing-postalCode")){
                billing.setPostalCode(idPayload.get("custom:billing-postalCode").asText());
            }
            if(idPayload.has("custom:billing-country")){
                billing.setCountry(idPayload.get("custom:billing-country").asText());
            }
            newOrder.setBillingAddress(billing);
            newOrder.save();
            return ApiGatewayResponse.builder()
                    .setStatusCode(201)
                    .setObjectBody(newOrder)
                    .build();
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            logger.severe("Error in saving product: " + ex + sw.toString());

            return StandardResponses.error("InvalidInput");
        }
    }

}
