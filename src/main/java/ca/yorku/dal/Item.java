package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.dal.DynamoDBAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "item_table6")
public class Item {

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private String id;
    private double price;
    private String name;
    private long quantity;
    private String category;
    private String soldBy;
    private int numSold;
    @DynamoDBTypeConverted(converter = ReviewIdConverter.class)
    private List<ReviewId> reviews;



    private Logger logger = Logger.getLogger(this.getClass().getName());

    public Item(){
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @DynamoDBRangeKey
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute
    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public Item get(String id){
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

    public List<Item> getByCategory(String categoryId){
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
            for(Item i: result) {
                itemList.add(i);
            }
        } else {
            logger.info("Products - get(): product - Not Found.");
        }
        return itemList;
    }

    public List<Map<String, AttributeValue>> getAll(){
        ScanRequest scanReq = new ScanRequest()
                .withTableName("item_table6")
                .withLimit(5)
                .withAttributesToGet(new String[]{"id","numSold"})
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
        ScanResult result = client.scan(scanReq);
        return result.getItems();
    }
    public void save(){
        mapper.save(this);
    }

    public boolean delete(String id){
        Item item;
        item = get(id);
        if(item != null){
            mapper.delete(item);
            return true;
        }
        return false;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CategoryIndex")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @DynamoDBAttribute
    public String getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(String soldBy) {
        this.soldBy = soldBy;
    }

    @DynamoDBAttribute
    public List<ReviewId> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewId> reviews) {
        this.reviews = reviews;
    }

    @DynamoDBAttribute
    public int getNumSold() {
        return numSold;
    }

    public void setNumSold(int numSold) {
        this.numSold = numSold;
    }

    @DynamoDBDocument
    public static class ReviewId{
        private String reviewId;

        @DynamoDBAttribute
        public String getReviewId() {
            return reviewId;
        }

        public void setReviewId(String reviewId) {
            this.reviewId = reviewId;
        }
    }
    public static class ReviewIdConverter implements DynamoDBTypeConverter<String,List<ReviewId>> {
        private Logger logger = Logger.getLogger(this.getClass().getName());
        @Override
        public String convert(List<ReviewId> reviewId) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for(ReviewId rId: reviewId) {
                if(!first) sb.append(",");
                first = false;
                sb.append("{\"reviewId\": \"" + rId.getReviewId() + "\"}");
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public List<ReviewId> unconvert(String s) {
            List<ReviewId> rId = new ArrayList<ReviewId>();
            try {
                JsonNode body = new ObjectMapper().readTree(s);
                if(body.isArray()) {
                    for(JsonNode node: body){
                        if(node.has("reviewId")){
                            ReviewId r = new ReviewId();
                            r.setReviewId(node.get("reviewId").asText());
                            rId.add(r);
                        }
                    }
                }
            }catch(Exception ex){
                logger.severe(ex.getMessage());
            }
            return rId;

        }
    }
}
