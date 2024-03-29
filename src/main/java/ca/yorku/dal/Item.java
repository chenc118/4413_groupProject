package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.serverless.dal.DynamoDBAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "item_table8")
public class Item {

    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;
    private final Logger logger;

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    @Getter
    @Setter
    private String itemId;
    @DynamoDBAttribute
    @Getter
    @Setter
    private double price;
    @DynamoDBRangeKey
    @Getter
    @Setter
    private String name;
    @DynamoDBAttribute
    @Getter
    @Setter
    private long quantityForSale;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CategoryIndex")
    @Getter
    @Setter
    private String categoryId;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String soldBy;
    @DynamoDBAttribute
    @Getter
    @Setter
    private int numSold;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String image;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String description;
    @DynamoDBAttribute
    @Getter
    @Setter
    private List<Review.ReviewId> reviews;

    public Item() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder().build();
        DynamoDBAdapter db_adapter = DynamoDBAdapter.getInstance();
        this.client = db_adapter.getDbClient();
        this.mapper = db_adapter.createDbMapper(mapperConfig);

        logger = Logger.getLogger(this.getClass().getName());
    }

    public Item get(String itemId) {
        Item item = null;

        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(itemId));

        DynamoDBQueryExpression<Item> queryExp = new DynamoDBQueryExpression<Item>()
                .withKeyConditionExpression("itemId = :v1")
                .withExpressionAttributeValues(av);

        List<Item> result = this.mapper.query(Item.class, queryExp);
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
        List<Item> itemList = new ArrayList<>();

        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(categoryId));

        DynamoDBQueryExpression<Item> queryExp = new DynamoDBQueryExpression<Item>()
                .withIndexName("CategoryIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("categoryId = :v1")
                .withExpressionAttributeValues(av);

        List<Item> result = this.mapper.query(Item.class, queryExp);
        if (result.size() > 0) {
            itemList.addAll(result);
        } else {
            logger.info("Products - get(): product - Not Found.");
        }
        return itemList;
    }

    @DynamoDBIgnore
    public List<TopItemInfo> topItems() {
        ScanRequest scanReq = new ScanRequest()
                .withTableName("item_table8")
                //.withLimit(5)
                .withAttributesToGet("itemId", "numSold")
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        ScanResult result = client.scan(scanReq);
        List<Map<String, AttributeValue>> itemList = result.getItems();
        List<TopItemInfo> topInfo = new ArrayList<>();
        for (Map<String, AttributeValue> i : itemList) {
            TopItemInfo ti = new TopItemInfo();
            ti.setItemId(i.get("itemId").getS());
            ti.setNumSold(Integer.parseInt(i.get("numSold").getN()));
            topInfo.add(ti);
        }
        return topInfo;
    }

    @DynamoDBIgnore
    public List<ItemId> itemByName(String name) {
        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":n", new AttributeValue().withS(name));
        Map<String, String> an = new HashMap<>();
        an.put("#name", "name");

        ScanRequest scanReq = new ScanRequest()
                .withTableName("item_table8")
                //.withAttributesToGet("itemId")
                .withFilterExpression("contains(#name,:n)")
                .withExpressionAttributeValues(av)
                .withExpressionAttributeNames(an);

        ScanResult result = client.scan(scanReq);
        List<Map<String, AttributeValue>> itemList = result.getItems();
        List<ItemId> itemsId = new ArrayList<>();
        for (Map<String, AttributeValue> i : itemList) {
            ItemId id = new ItemId();
            id.setItemId(i.get("itemId").getS());
            itemsId.add(id);
        }
        return itemsId;
    }

    public void save() {
        mapper.save(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        Item i = (Item) o;
        return getItemId().equals(i.getItemId());
    }

    @DynamoDBIgnore
    public boolean delete(String itemId) {
        Item item = get(itemId);
        if (item != null) {
            mapper.delete(item);
            return true;
        }
        return false;
    }

    public static class TopItemInfo {

        @Getter
        @Setter
        private String itemId;

        @JsonIgnore
        @Getter
        @Setter
        private int numSold;
    }

    @DynamoDBDocument
    public static class ItemId {

        @DynamoDBAttribute
        @Getter
        @Setter
        private String itemId;
    }
}
