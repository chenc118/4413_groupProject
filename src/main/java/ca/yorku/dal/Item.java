package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.serverless.dal.DynamoDBAdapter;
import com.serverless.dal.Product;

import java.util.HashMap;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "item_table2")
public class Item {

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private String id;
    private double price;
    private String name;
    private long quantity;



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

}
