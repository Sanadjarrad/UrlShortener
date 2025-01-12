package com.shortener.urlshortener.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.annotation.processing.Generated;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Document(collection = "urls")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Url {

    @Transient
    public static final String SEQUENCE_NAME = "urlSequence";

    @Id
    private String id;

    private String longUrl;

    private String shortUrlEncoding;

    private LocalDateTime creationDate;

    private int encodeId;

    private String shortUrl;

}
