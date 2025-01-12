package com.shortener.urlshortener.filter;

import com.shortener.urlshortener.exception.RateLimiter.RateLimitException;
import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import com.shortener.urlshortener.rest.UrlShortenerController;
import com.shortener.urlshortener.service.ShortenerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RateLimiterTests {

    final static int HTTP_STATUS_TOO_MANY_REQUESTS = 429;
    final static String CLIENT_IP = "192.168.1.1";
    final static LocalDateTime now = LocalDateTime.now();
    final static String EXAMPLE_URL = "https://example.com/long-url-example-123456789-long-url-example";

    @Value("${rate.limit.max.requests}")
    private int MAX_NUM_OF_REQUESTS_PM;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlShortenerController urlShortenerController;

    @MockitoBean
    private ShortenerService shortenerService;

    @MockitoBean
    private UrlRepository urlRepository;

    private Url url;

    @BeforeEach
    void setUp() throws MalformedURLException {
        url = Url.builder()
                .id("1")
                .longUrl(EXAMPLE_URL)
                .shortUrlEncoding("0000001")
                .creationDate(now)
                .encodeId(1)
                .shortUrl(UrlHelper.BASE_URL + "/" + "0000001")
                .build();
        when(shortenerService.shortenUrl(EXAMPLE_URL)).thenReturn(url.getShortUrl());
    }


    @Test
    void testRequestsWithinLimit() throws Exception {
        for (int i = 0; i < MAX_NUM_OF_REQUESTS_PM; i++) {
            mockMvc.perform(post("/UrlShortener/shorten")
                            .param("longUrl", EXAMPLE_URL)
                            .header("X-Forwarded-For", CLIENT_IP))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testRequestsExceedingLimit() {
        for (int i = 0; i < MAX_NUM_OF_REQUESTS_PM; i++) {
            urlShortenerController.shortenURL(EXAMPLE_URL);
        }

        assertThatThrownBy(() -> urlShortenerController.shortenURL(EXAMPLE_URL))
                .isInstanceOf(RateLimitException.class);
    }

    /*
    @Test
    void testRequestsExceedingLimit() throws Exception {
        for (int i = 0; i < MAX_NUM_OF_REQUESTS_PM; i++) {
            mockMvc.perform(post("/UrlShortener/shorten")
                            .param("longUrl", "https://example.com")
                            .header("X-Forwarded-For", CLIENT_IP))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/UrlShortener/shorten")
                        .param("longUrl", "https://example.com")
                        .header("X-Forwarded-For", CLIENT_IP))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Too many requests from IP: 192.168.1.1. Please try again later."));
    }

     */
}
