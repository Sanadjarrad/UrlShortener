package com.shortener.urlshortener.helper;

import io.lettuce.core.dynamic.annotation.CommandNaming;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Helper;
import org.springframework.stereotype.Component;


@NoArgsConstructor(force = true)
@Component
public class Base62Encoder {

    private static final int BASE = 62;
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // Base 10 to base 62
    public static String encode(long token) throws IllegalArgumentException {

        if (token < 0) {
            throw new IllegalArgumentException("Negative numbers are not supported for Base62 encoding.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.insert(0, chars.charAt((int) (token % BASE)));
            token /= BASE;
        } while (token > 0);

        return stringBuilder.toString();
    }

    // Base 62 to base 10
    public static long decode(String token) {
        long res = 0L;
        int length = token.length();
        for (int i = 0; i < length; i++) {
            res += (long) Math.pow(BASE, i) * chars.indexOf(token.charAt(length - i - 1));
        }

        return res;
    }
}
