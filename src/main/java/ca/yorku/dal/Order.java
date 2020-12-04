package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serverless.dal.DynamoDBAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "orders_table6")
public class Order {

    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;
    private final Logger logger;

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    @Getter
    @Setter
    private String orderId;
    @DynamoDBAttribute
    @Getter
    @Setter
    private Date placedDate;
    @DynamoDBAttribute
    @Getter
    @Setter
    private Date shippedDate;
    @DynamoDBAttribute
    @Getter
    @Setter
    private Date deliveredDate;
    @DynamoDBAttribute
    @DynamoDBTypeConvertedEnum
    @Getter
    @Setter
    private Status status;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String comment;
    @DynamoDBAttribute
    @Getter
    @Setter
    private List<ItemInfo> items;

    @JsonIgnore
    @DynamoDBAttribute
    private String partitionKeyDummy = "orders";

    public Order() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder().build();
        DynamoDBAdapter db_adapter = DynamoDBAdapter.getInstance();
        this.client = db_adapter.getDbClient();
        this.mapper = db_adapter.createDbMapper(mapperConfig);

        logger = Logger.getLogger(this.getClass().getName());
    }

    @DynamoDBIgnore
    public Order get(String orderId) {
        Order order = null;

        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(orderId));

        DynamoDBQueryExpression<Order> queryExp = new DynamoDBQueryExpression<Order>()
                .withKeyConditionExpression("orderId = :v1")
                .withExpressionAttributeValues(av);

        List<Order> result = this.mapper.query(Order.class, queryExp);
        if (result.size() > 0) {
            order = result.get(0);
            //logger.info("Products - get(): product - " + product.toString());
        } else {
            //logger.info("Products - get(): product - Not Found.");
        }
        return order;
    }

    @DynamoDBIgnore
    public List<Order> getByItem(String itemId) {
        List<Order> orderList = new ArrayList<>();

        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(itemId));

        DynamoDBQueryExpression<Order> queryExp = new DynamoDBQueryExpression<Order>()
                .withIndexName("ItemsIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("itemId = :v1")
                .withExpressionAttributeValues(av);

        List<Order> result = this.mapper.query(Order.class, queryExp);
        if (result.size() > 0) {
            orderList.addAll(result);
        } else {
            logger.info("Orders - get(): order - Not Found.");
        }
        return orderList;
    }

    @DynamoDBIgnore
    public List<ItemInfo> monthlyReport(String yearMonth) {
        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":dateMonth", new AttributeValue().withS(yearMonth));
        av.put(":pkd", new AttributeValue().withS("orders"));
        DynamoDBQueryExpression<Order> queryExp = new DynamoDBQueryExpression<Order>()
                .withIndexName("DateIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("placedDate BEGINS_WITH :dateMonth AND partitionKeyDummy = :pkd")
                .withExpressionAttributeValues(av);

        HashMap<String,ItemInfo> items = new HashMap<>();
        PaginatedQueryList<Order> result = this.mapper.query(Order.class, queryExp);
        if (result.size() > 0) {
            for(Order o:result){
                for(ItemInfo i:o.getItems()){
                    if(items.containsKey(i.itemId)){
                        ItemInfo cur = items.get(i.itemId);
                        cur.setQuantity(i.quantity+cur.quantity);
                        //items.put(i.itemId, cur);
                    }
                    else{
                        items.put(i.itemId,i);
                    }
                }
            }
            //logger.info("Orders found for year " + year + " month " + month + ": " + result.size());
        } else {
            logger.info("Orders - get(): order - Not Found.");
        }

        return List.copyOf(items.values());
    }

    public void save() {
        mapper.save(this);
    }

    public boolean delete(String orderId) {
        Order order = get(orderId);
        if (order != null) {
            mapper.delete(order);
            return true;
        }
        return false;
    }

    public String getPartitionKeyDummy() {
        return "orders";
    }

    public void setPartitionKeyDummy(String partitionKeyDummy) {
        this.partitionKeyDummy = "orders";
    }

    public enum Status {
        Placed,
        Shipped,
        Delivered
    }

    @DynamoDBDocument
    public static class ItemInfo{
        @DynamoDBAttribute
        @Getter
        @Setter
        private String itemId;
        @DynamoDBAttribute
        @Getter
        @Setter
        private int quantity;
    }
}
