package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.serverless.dal.DynamoDBAdapter;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Data
@DynamoDBTable(tableName = "orders_table2")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Order {

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private Date placedDate;
    @DynamoDBAttribute
    @DynamoDBTypeConvertedEnum
    private Status status;
    @DynamoDBAttribute
    private String comment;
    @DynamoDBAttribute
    private String itemId;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Order() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .build();
        // get the db adapter
        db_adapter = DynamoDBAdapter.getInstance();
        this.client = db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = db_adapter.createDbMapper(mapperConfig);
    }

    public Order get(String id) {
        Order order = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Order> queryExp = new DynamoDBQueryExpression<Order>()
                .withKeyConditionExpression("id = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Order> result = this.mapper.query(Order.class, queryExp);
        if (result.size() > 0) {
            order = result.get(0);
            //logger.info("Products - get(): product - " + product.toString());
        } else {
            //logger.info("Products - get(): product - Not Found.");
        }
        return order;
    }

    public List<Order> getByItem(String itemId) {
        List<Order> orderList = new ArrayList<Order>();

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(itemId));

        DynamoDBQueryExpression<Order> queryExp = new DynamoDBQueryExpression<Order>()
                .withIndexName("ItemsIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("itemId = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Order> result = this.mapper.query(Order.class, queryExp);
        if (result.size() > 0) {
            orderList.addAll(result);
        } else {
            logger.info("Orders - get(): order - Not Found.");
        }
        return orderList;
    }

    public void save() {
        mapper.save(this);
    }

    public boolean delete(String id) {
        Order order;
        order = get(id);
        if (order != null) {
            mapper.delete(order);
            return true;
        }
        return false;
    }

    public enum Status {
        Placed,
        Shipped,
        Delivered
    }
}
