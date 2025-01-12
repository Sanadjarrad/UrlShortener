package com.shortener.urlshortener.service;

import com.shortener.urlshortener.helper.Base62Encoder;
import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ServiceTests {

    private final static String EXAMPLE_URL = "https://example.com/long-url-example-123456789-long-url-example";

    private final static LocalDateTime now = LocalDateTime.now();

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private ShortenerService shortenerService;

    @MockitoBean
    private RedisService redisService;

    @Mock
    private Base62Encoder base62Encoder;

    private Url url;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

    @Nested
    class Base62EncoderTests {

        private static Stream<String> provideTestValues() {
            return Stream.of(
                    "1", "10", "100", "1000", "10000",
                    "111111111", "22222222222222", "3333333333333333333", "44444444444444444", "555555555555555555"
            );
        }

        @ParameterizedTest
        @MethodSource("provideTestValues")
        public void base62EncoderReverseTest(String value) {
            long numericValue = Long.parseLong(value);
            String encoded = Base62Encoder.encode(numericValue);
            long decoded = Base62Encoder.decode(encoded);

            assertThat(decoded).isEqualTo(numericValue);
        }

        @ParameterizedTest
        @MethodSource("provideTestValues")
        void Base62EncoderUniquenessTest(String value) {
            Set<String> encodedSet = new HashSet<>();

            long numericValue = Long.parseLong(value);
            String encoded = Base62Encoder.encode(numericValue);

            assertThat(encodedSet.contains(encoded))
                    .withFailMessage("Duplicate encoding found for value: %s (encoded as %s)", numericValue, encoded)
                    .isFalse();

            encodedSet.add(encoded);
        }

        @Test
        void Base62EncoderSpecificTest() {
            String[][] testValues = {
                    {"1", "1"},
                    {"10", "A"},
                    {"100", "1c"}
            };

            for (String[] testValue : testValues) {
                long numericValue = Long.parseLong(testValue[0]);
                String expectedEncoding = testValue[1];

                String encoded = Base62Encoder.encode(numericValue);

                assertThat(encoded)
                        .withFailMessage("Encoding mismatch for value: %s (expected: %s, actual: %s)", numericValue, expectedEncoding, encoded)
                        .isEqualTo(expectedEncoding);
            }
        }

        @Nested
        class HelperFunctionalityTests {

            private static Stream<String> validUrls() {
                return Stream.of(
                        "https://www.example.com",
                        "https://www.google.com",
                        "http://localhost:8080",
                        "https://github.com/Sanadjarrad",
                        "https://spring.io/guides/gs/testing-web",
                        "https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-aop/3.4.1",
                        "https://www.youtube.com/watch?v=0ZtU3X9n6tI",
                        "https://youtrack.jetbrains.com/issue/IDEA-364333/Problem-with-lombok-in-intelliJ#focus=Comments-27-11277810.0-0",
                        "https://docs.spring.io/spring-framework/docs/4.0.x/spring-framework-reference/html/aop.html",
                        "https://circleci.com/blog/unit-testing-vs-integration-testing/#:~:text=While%20unit%20tests%20always%20take,works%20in%20an%20integrated%20way.",
                        "https://medium.com/@mbanaee61/efficient-rate-limiting-in-reactive-spring-boot-applications-with-redis-and-junit-testing-20675e73104a"
                );
            }


            private static Stream<String> invalidUrls() {
                return Stream.of(
                        "htp:/example.com",     // Invalid scheme
                        "http:// example.com",          // Space in URL
                        "example.com",                  // Missing scheme
                        "://example.com",               // Missing scheme prefix
                        "http://",                      // Missing host
                        "http://.com",                  // Invalid host
                        "https://example..com",         // Double dots
                        "ftp://example.com",            // Unsupported scheme
                        "https//example.com",           // Missing colon
                        ""                              // Empty URL
                );
            }

            @ParameterizedTest
            @MethodSource("validUrls")
            void testValidUrls(String url) throws MalformedURLException {
                assertThat(UrlHelper.isValidURL(url)).isTrue();
            }

            @ParameterizedTest
            @MethodSource("invalidUrls")
            void testInvalidUrls(String url) {
                assertThatThrownBy(() -> UrlHelper.isValidURL(url)).isInstanceOf(MalformedURLException.class)
                        .hasMessageContaining("Invalid URL format provided");
            }

        }
    }
}