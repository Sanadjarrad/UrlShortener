package com.shortener.urlshortener.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    public int deleteAllKeys() {
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match("*").build());

        int deletedCount = 0;
        while (cursor.hasNext()) {
            byte[] key = cursor.next();
            redisTemplate.delete(new String(key, StandardCharsets.UTF_8));
            deletedCount++;
        }
        return deletedCount;
    }

    public List<String> deleteInvalidKeys() {
        List<String> invalidKeys = getInvalidKeys();
        for (String key : invalidKeys) {
            redisTemplate.delete(key);
        }
        return invalidKeys;
    }

    private List<String> getInvalidKeys() {
        Set<String> keys = getAllKeys();
        List<String> invalidKeys = new ArrayList<>();

        if (keys != null) {
            for (String key : keys) {
                if (!key.startsWith("shortUrl:") && !key.startsWith("longUrl:")) {
                    invalidKeys.add(key);
                }
            }
        }

        return invalidKeys;
    }

    public void cacheUrl(String longUrl, String shortUrl) {
        redisTemplate.opsForValue().set("longUrl:" + longUrl, shortUrl);
        redisTemplate.opsForValue().set("shortUrl:" + shortUrl, longUrl);

        log.info("Cached in Redis: longUrl -> shortCode: {}, {}", longUrl, shortUrl);
        log.info("Cached in Redis: shortCode -> longUrl: {}, {}", shortUrl, longUrl);
    }

    public String getLongUrlFromCache(String shortUrl) {
        return (String) redisTemplate.opsForValue().get("shortUrl:" + shortUrl);
    }

}
