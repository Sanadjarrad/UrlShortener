package com.shortener.urlshortener.Redis;


import com.shortener.urlshortener.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RedisTests {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisService redisService;

    private List<String> keys;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        keys = new ArrayList<>();
        keys.add("shortUrl:https://example.com/0000001");
        keys.add("longUrl:https://example.com/long-url-example-123456789-long-url-example");
        keys.add("invalidKey1:invalid1");
        keys.add("invalidKey2:invalid2");
    }

    @AfterEach
    void teardown() {

    }

    @Test
    void testAddToCache() {
        String longUrl = "https://example.com";
        String shortUrl = "https://example.com/0000001";

        redisTemplate.opsForValue().set("longUrl:" + longUrl, shortUrl);
        redisTemplate.opsForValue().set("shortUrl:" + shortUrl, longUrl);
        when(redisTemplate.opsForValue().get("longUrl:" + longUrl)).thenReturn(shortUrl);
        when(redisTemplate.opsForValue().get("shortUrl:" + shortUrl)).thenReturn(longUrl);

        String cachedShortUrl = redisTemplate.opsForValue().get("longUrl:" + longUrl);
        String cachedLongUrl = redisTemplate.opsForValue().get("shortUrl:" + shortUrl);

        assertThat(cachedShortUrl).isEqualTo(shortUrl);
        assertThat(cachedLongUrl).isEqualTo(longUrl);
    }

    /*
    @Test
    void testDeleteInvalidKeys() {
        Set<String> mockKeys = new HashSet<>(keys);
        List<String> deletedKeys = new ArrayList<>();
        when(redisTemplate.keys("*")).thenReturn(mockKeys);
        System.out.println("Mocked Keys: " + redisTemplate.keys("*"));
        List<String> deletedKeys = redisService.deleteInvalidKeys();

        assertThat(deletedKeys).contains("invalidKey1", "invalidKey2");
        verify(redisTemplate, times(1)).delete("invalidKey1");
        verify(redisTemplate, times(1)).delete("invalidKey2");
        verify(redisTemplate, never()).delete("shortUrl:123");
        verify(redisTemplate, never()).delete("longUrl:https://example.com");
    }
     */

}
