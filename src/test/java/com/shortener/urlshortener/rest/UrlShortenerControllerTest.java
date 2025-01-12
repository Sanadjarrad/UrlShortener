package com.shortener.urlshortener.rest;


import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import com.shortener.urlshortener.service.IdGeneratorService;
import com.shortener.urlshortener.service.RedisService;
import com.shortener.urlshortener.service.ShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UrlShortenerControllerTest {

    private static final String EXAMPLE_URL = "https://example.com/long-url-example-123456789-long-url-example";
    private static final LocalDateTime now = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlRepository urlRepository;

    @MockitoBean
    private IdGeneratorService idGeneratorService;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    private ShortenerService shortenerService;

    @MockitoBean
    private RedisTemplate redisTemplate;

    private Url url;

    @BeforeEach
    void setUp() {
        url = Url.builder()
                .id("1")
                .longUrl(EXAMPLE_URL)
                .shortUrlEncoding("0000001")
                .creationDate(now)
                .encodeId(1)
                .shortUrl(UrlHelper.BASE_URL + "/" + "0000001")
                .build();

        when(urlRepository.findByShortUrlEncoding("0000001")).thenReturn(Optional.of(url));
    }

    @Test
    void testShortenURL_Success() throws Exception {
        String expectedShortUrl = UrlHelper.BASE_URL + "/" + this.url.getShortUrlEncoding();

        when(shortenerService.shortenUrl(EXAMPLE_URL)).thenReturn(expectedShortUrl);

        mockMvc.perform(post("/UrlShortener/shorten")
                        .param("longUrl", EXAMPLE_URL))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedShortUrl));
    }

    @Test
    void testValidShortCodeRedirection() throws Exception{
        String shortUrlEncoding = url.getShortUrlEncoding();

        when(shortenerService.getLongUrl(shortUrlEncoding)).thenReturn(EXAMPLE_URL);

        mockMvc.perform(get("/UrlShortener/{shortUrlEncoding}", shortUrlEncoding))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", EXAMPLE_URL));
    }

    @Test
    void testGetAllUrls() throws Exception {
        Url url2 = Url.builder()
                .id("2")
                .longUrl("https://another-example.com")
                .shortUrlEncoding("0000002")
                .shortUrl(UrlHelper.BASE_URL + "/" + "0000002")
                .build();

        List<Url> urls = List.of(url, url2);

        when(urlRepository.findAll()).thenReturn(urls);

        mockMvc.perform(get("/UrlShortener/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].longUrl").value(EXAMPLE_URL))
                .andExpect(jsonPath("$[0].shortUrlEncoding").value("0000001"))
                .andExpect(jsonPath("$[0].shortUrl").value(UrlHelper.BASE_URL + "/" + "0000001"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].longUrl").value("https://another-example.com"))
                .andExpect(jsonPath("$[1].shortUrlEncoding").value("0000002"))
                .andExpect(jsonPath("$[1].shortUrl").value(UrlHelper.BASE_URL + "/" + "0000002"));
    }

}
