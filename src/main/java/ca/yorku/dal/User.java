package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.serverless.dal.DynamoDBAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@DynamoDBTable(tableName = "user_table")
public class User {

    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;
    private final Logger logger;

    @DynamoDBAutoGeneratedKey
    @DynamoDBHashKey
    @Getter
    @Setter
    private int userId;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String firstName;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String lastName;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String email;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String password;
    @DynamoDBAttribute
    @Getter
    @Setter
    private String phone;

    public User() {
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder().build();
        DynamoDBAdapter db_adapter = DynamoDBAdapter.getInstance();
        this.client = db_adapter.getDbClient();
        this.mapper = db_adapter.createDbMapper(mapperConfig);

        logger = Logger.getLogger(this.getClass().getName());
    }

    public User get(String uuid) {
        User user = null;

        Map<String, AttributeValue> av = new HashMap<>();
        av.put(":v1", new AttributeValue().withS(uuid));

        DynamoDBQueryExpression<User> queryExp = new DynamoDBQueryExpression<User>()
                .withKeyConditionExpression("uuid = :v1")
                .withExpressionAttributeValues(av);

        List<User> result = this.mapper.query(User.class, queryExp);
        if (result.size() > 0) {
            user = result.get(0);
            //logger.info("Products - get(): product - " + product.toString());
        } else {
            //logger.info("Products - get(): product - Not Found.");
        }
        return user;
    }

    public void save() {
        mapper.save(this);
    }

    public boolean delete(String uuid) {
        User user = get(uuid);
        if (user != null) {
            mapper.delete(user);
            return true;
        }
        return false;
    }
}
