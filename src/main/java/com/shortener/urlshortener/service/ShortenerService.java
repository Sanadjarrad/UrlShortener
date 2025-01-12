package com.shortener.urlshortener.service;
import com.shortener.urlshortener.helper.Base62Encoder;
import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDateTime;

@Service
public class ShortenerService {

    private static final int SHORT_URL_LENGTH = 7;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private RedisService redisService;

    public String shortenUrl(String longUrl) throws MalformedURLException {
        return UrlHelper.isValidURL(longUrl) ? urlRepository.findByLongUrl(longUrl)
                .map(url -> {
                    String shortCode = url.getShortUrlEncoding();
                    String shortUrl =  UrlHelper.BASE_URL + "/" + shortCode;
                    redisService.cacheUrl(longUrl, shortCode);
                    return shortUrl;
                })
                .orElseGet(() -> {
                    int numericId = idGeneratorService.getSequenceNumber("url_sequence");
                    String shortCode = generateShortCode(numericId);
                    String shortUrl = UrlHelper.BASE_URL + "/" + shortCode;

                    Url newUrl = Url.builder()
                            .longUrl(longUrl)
                            .shortUrl(shortUrl)
                            .shortUrlEncoding(shortCode)
                            .creationDate(LocalDateTime.now())
                            .encodeId(numericId)
                            .build();

                    urlRepository.save(newUrl);
                    redisService.cacheUrl(longUrl, shortUrl);

                    return shortUrl;
                }) : new MalformedURLException("Invalid URL format").getMessage();
    }

    public String getLongUrl(String shortCode) {
        String fullShortUrl = UrlHelper.BASE_URL + "/" + shortCode;

        String longUrl = redisService.getLongUrlFromCache(fullShortUrl);

        if (longUrl == null) {
            Url url = urlRepository.findByShortUrlEncoding(shortCode)
                    .orElseThrow(() -> new IllegalArgumentException("Short URL not found or Does not Exist"));
            longUrl = url.getLongUrl();
            redisService.cacheUrl(longUrl, fullShortUrl);
        }
        return longUrl;
    }

    private String generateShortCode(int numericId) {
        String shortCode = Base62Encoder.encode(numericId);
        return String.format("%1$" + SHORT_URL_LENGTH + "s", shortCode).replace(' ', '0');
    }

}
