package com.shortener.urlshortener.repository;

import com.shortener.urlshortener.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortUrlEncoding(String shortUrlCode);
    Optional<Url> findByLongUrl(String longUrl);

}
