package com.hkx.tinyurler.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString


@Document(collection = "urls")
public class Url {

    @Id
    private String id;
    private String originalUrl;
    private String shortedUrl;

}
