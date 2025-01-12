package com.shortener.urlshortener.rest;


import com.shortener.urlshortener.filter.RateLimit;
import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import com.shortener.urlshortener.service.ShortenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import java.net.MalformedURLException;
import java.util.*;

@RestController
@RequestMapping("/UrlShortener")
@Slf4j
public class UrlShortenerController {

    @Autowired
    private ShortenerService shortenerService;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/shorten")
    @RateLimit
    public ResponseEntity<String> shortenURL(@RequestParam String longUrl) {
        try {
            String shortUrl = shortenerService.shortenUrl(longUrl);
            return ResponseEntity.ok(shortUrl);
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{shortUrlEncoding}")
    @RateLimit
    public RedirectView redirectToLongUrl(@PathVariable String shortUrlEncoding) {
        try {
            String longUrl = shortenerService.getLongUrl(shortUrlEncoding);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(longUrl);
            return redirectView;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Short URL with encoding: %s not found", shortUrlEncoding));
        }
    }

    @GetMapping("/getAll")
    public List<Url> getAll() {
        return urlRepository.findAll();
    }
}
