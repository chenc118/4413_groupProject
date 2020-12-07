package ca.yorku.dal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Getter;
import lombok.Setter;

@DynamoDBDocument
public class Address {
    @DynamoDBAttribute
    @Setter
    @Getter
    private String name;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String street;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String city;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String province;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String postalCode;
    @DynamoDBAttribute
    @Setter
    @Getter
    private String country;
}
