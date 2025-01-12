package com.shortener.urlshortener.rest;


import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import com.shortener.urlshortener.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/redis")
public class RedisOperationsController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UrlRepository urlRepository;

    @GetMapping("/getAllKeys")
    private ResponseEntity<Set<String>> listAllRedisKeys() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            return ResponseEntity.ok(keys != null ? keys : new HashSet<>());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/cleanupKeys")
    ResponseEntity<String> cleanupKeys() {
        List<String> deletedKeys = redisService.deleteInvalidKeys();
        return ResponseEntity.ok("Deleted keys: " + deletedKeys);
    }

    @PostMapping("/flushAllKeys")
    public ResponseEntity<String> flushAllKeys() {
        redisService.deleteAllKeys();
        return ResponseEntity.ok("All keys deleted successfully");
    }
}
