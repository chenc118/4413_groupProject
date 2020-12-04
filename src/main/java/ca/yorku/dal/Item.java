package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.serverless.dal.DynamoDBAdapter;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Data
@DynamoDBTable(tableName = "item_table6")
public class Item {

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private double price;
    @DynamoDBRangeKey
    private String name;
    @DynamoDBAttribute
    private long quantity;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CategoryIndex")
    private String category;
    @DynamoDBAttribute
    private String soldBy;
    @DynamoDBAttribute
    private int numSold;
    @DynamoDBAttribute
    private List<ReviewId> reviews;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Item() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .build();
        // get the db adapter
        db_adapter = DynamoDBAdapter.getInstance();
        this.client = db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = db_adapter.createDbMapper(mapperConfig);
    }

    public Item get(String id) {
        Item item = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Item> queryExp = new DynamoDBQueryExpression<Item>()
                .withKeyConditionExpression("id = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Item> result = this.mapper.query(Item.class, queryExp);
        if (result.size() > 0) {
            item = result.get(0);
            //logger.info("Products - get(): product - " + product.toString());
        } else {
            //logger.info("Products - get(): product - Not Found.");
        }
        return item;
    }

    @DynamoDBIgnore
    public List<Item> getByCategory(String categoryId) {
        List<Item> itemList = new ArrayList<Item>();

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(categoryId));

        DynamoDBQueryExpression<Item> queryExp = new DynamoDBQueryExpression<Item>()
                .withIndexName("CategoryIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("category = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Item> result = this.mapper.query(Item.class, queryExp);
        if (result.size() > 0) {
            itemList.addAll(result);
        } else {
            logger.info("Products - get(): product - Not Found.");
        }
        return itemList;
    }

    @DynamoDBIgnore
    public List<Map<String, AttributeValue>> listOfItems() {
        ScanRequest scanReq = new ScanRequest()
                .withTableName("item_table6")
                .withLimit(5)
                .withAttributesToGet("id", "numSold")
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        ScanResult result = client.scan(scanReq);
        return result.getItems();
    }

    public void save() {
        mapper.save(this);
    }

    @DynamoDBIgnore
    public boolean delete(String id) {
        Item item;
        item = get(id);
        if (item != null) {
            mapper.delete(item);
            return true;
        }
        return false;
    }

    @DynamoDBDocument
    public static class ReviewId {
        private String reviewId;

        @DynamoDBAttribute(attributeName = "id")
        public String getReviewId() {
            return reviewId;
        }

        public void setReviewId(String reviewId) {
            this.reviewId = reviewId;
        }
    }
}
