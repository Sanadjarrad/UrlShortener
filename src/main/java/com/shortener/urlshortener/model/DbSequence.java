package com.shortener.urlshortener.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "database_sequences")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DbSequence {

    @Id
    private String id;

    private int seq;
}
