package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.serverless.dal.DynamoDBAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "item_table5")
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
    private List<String> reviews;



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

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "price")
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @DynamoDBRangeKey(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "quantity")
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

    public void getByCategory(String categoryId){
        Item item = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS("testCategory"));

        DynamoDBQueryExpression<Item> queryExp = new DynamoDBQueryExpression<Item>()
                .withKeyConditionExpression("category = :v1")
                .withExpressionAttributeValues(av);

        PaginatedQueryList<Item> result = this.mapper.query(Item.class, queryExp);
        if (result.size() > 0) {
            for(Item i: result) {
                logger.info("Products - get(): product - " + i.getId());
            }
        } else {
            logger.info("Products - get(): product - Not Found.");
        }
    }

    public void save(){
        mapper.save(this);
    }

    public boolean delete(String id){
        Item item;
        item = get(id);
        if(item != null){
            mapper.delete(item);
        }
        return false;
    }

    @DynamoDBIndexHashKey(attributeName="category",globalSecondaryIndexName = "CategoryIndex")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @DynamoDBAttribute(attributeName = "soldBy")
    public String getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(String soldBy) {
        this.soldBy = soldBy;
    }

    @DynamoDBAttribute(attributeName = "reviews")
    public List<String> getReviews() {
        return reviews;
    }

    public void setReviews(List<String> reviews) {
        this.reviews = reviews;
    }
}
