package com.shortener.urlshortener.repository;
import com.shortener.urlshortener.helper.UrlHelper;
import com.shortener.urlshortener.model.Url;
import com.shortener.urlshortener.service.IdGeneratorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class RepositoryTests {

    private final static String EXAMPLE_URL = "https://example.com/long-url-example-123456789-long-url-example";

    private final static LocalDateTime now = LocalDateTime.now();

    @Autowired
    private UrlRepository urlRepository;

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
        urlRepository.save(url);
    }

    @AfterEach
    void teardown() {
        url = null;
        urlRepository.deleteAll();
    }

    @Test
    void testFindByShortUrlEncoding() {
        Optional<Url> retrievedUrl = urlRepository.findByShortUrlEncoding("0000001");
        assertThat(retrievedUrl.isPresent());
        Url objectUrl = retrievedUrl.get();
        assertThat(objectUrl.getId()).isEqualTo(url.getId());
        assertThat(objectUrl.getLongUrl()).isEqualTo(url.getLongUrl());
        assertThat(objectUrl.getShortUrlEncoding()).isEqualTo(url.getShortUrlEncoding());
        //assertThat(objectUrl.getCreationDate()).isEqualTo(now);
        assertThat(objectUrl.getEncodeId()).isEqualTo(url.getEncodeId());
        assertThat(objectUrl.getShortUrl()).isEqualTo(url.getShortUrl());
    }

    @Test
    void testFindByShortUrlEncoding_NotFound() {
        Optional<Url> retrievedUrl = urlRepository.findByShortUrlEncoding("0000002");
        assertThat(retrievedUrl.isEmpty()).isTrue();
    }

    @Test
    void testFindByLongUrl() {
        Optional<Url> retrievedUrl = urlRepository.findByLongUrl(EXAMPLE_URL);
        assertThat(retrievedUrl.isPresent());
        Url objectUrl = retrievedUrl.get();
        assertThat(objectUrl.getId()).isEqualTo(url.getId());
        assertThat(objectUrl.getLongUrl()).isEqualTo(url.getLongUrl());
        assertThat(objectUrl.getShortUrlEncoding()).isEqualTo(url.getShortUrlEncoding());
        //assertThat(objectUrl.getCreationDate()).isEqualTo(now);
        assertThat(objectUrl.getEncodeId()).isEqualTo(url.getEncodeId());
        assertThat(objectUrl.getShortUrl()).isEqualTo(url.getShortUrl());
    }

    @Test
    void testFindByLongUrl_NotFound() {
        Optional<Url> retrievedUrl = urlRepository.findByLongUrl("https://thisExampleDoesNotExistInDb.com/example");
        assertThat(retrievedUrl.isEmpty()).isTrue();
    }

    @Nested
    class dbSequenceTests {

        @Mock
        IdGeneratorService idGeneratorService;

        @Test
        void sequenceOrderTest() {
            when(idGeneratorService.getSequenceNumber("url_sequence"))
                    .thenReturn(1, 2, 3, 4, 5);

            int firstSequence = idGeneratorService.getSequenceNumber("url_sequence");
            int secondSequence = idGeneratorService.getSequenceNumber("url_sequence");
            int thirdSequence = idGeneratorService.getSequenceNumber("url_sequence");
            int fourthSequence = idGeneratorService.getSequenceNumber("url_sequence");
            int fifthSequence = idGeneratorService.getSequenceNumber("url_sequence");

            assertAll(
                    () -> assertThat(firstSequence).isEqualTo(1),
                    () -> assertThat(secondSequence).isEqualTo(2),
                    () -> assertThat(thirdSequence).isEqualTo(3),
                    () -> assertThat(fourthSequence).isEqualTo(4),
                    () -> assertThat(fifthSequence).isEqualTo(5)
            );
        }
    }


}
