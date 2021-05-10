package ie.d00216118.models;

import ie.d00216118.utils.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 *  @author D00216118@DkIT
 *  @date 2021/4/30
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "goods")
public class Goods implements Serializable, Entity {

    @Id
    long id;

    @Field(type = FieldType.Text, name = "goodsName")
    private String goodsName;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Double, name = "price")
    private Double price;

    @Field(type = FieldType.Keyword, name="category")
    private String category;

    @Field(type = FieldType.Integer, name = "quantity")
    private Integer quantity;

    @Field(type = FieldType.Keyword, name = "manufacturer")
    private String manufacturer;
}

