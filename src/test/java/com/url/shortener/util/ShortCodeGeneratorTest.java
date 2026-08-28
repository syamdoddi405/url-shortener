package com.url.shortener.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ShortCodeGenerator();
    }

    @Test
    void generate_shouldReturnEightCharacterCode() {

        String url =
                "https://www.google.com";

        String result =
                generator.generate(url);

        assertEquals(8, result.length());
    }

    @Test
    void generate_shouldReturnDeterministicResult() {

        String url =
                "https://www.google.com";

        String result1 =
                generator.generate(url);

        String result2 =
                generator.generate(url);

        assertEquals(result1, result2);
    }

    @Test
    void generate_shouldReturnHexadecimalCode() {

        String url =
                "https://www.google.com";

        String result =
                generator.generate(url);

        assertTrue(
                result.matches("[0-9a-f]{8}")
        );
    }

    @Test
    void generate_shouldThrowException_whenUrlIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> generator.generate(null)
                );

        assertEquals(
                "URL cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void generate_shouldThrowException_whenUrlIsEmpty() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> generator.generate("")
                );

        assertEquals(
                "URL cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void generate_shouldThrowException_whenUrlIsBlank() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> generator.generate("   ")
                );

        assertEquals(
                "URL cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void generate_shouldGenerateDifferentCodesForDifferentUrls() {

        String result1 =
                generator.generate(
                        "https://google.com"
                );

        String result2 =
                generator.generate(
                        "https://amazon.com"
                );

        assertFalse(result1.equals(result2));
    }


    @Test
    void generateBase62_shouldReturnDeterministicResult() {

        String url =
                "https://www.google.com";

        String result1 =
                generator.generateBase62(url);

        String result2 =
                generator.generateBase62(url);

        assertEquals(result1, result2);
    }

    @Test
    void generateBase62_shouldUseValidBase62Characters() {

        String url =
                "https://www.google.com";

        String result =
                generator.generateBase62(url);

        assertTrue(
                result.matches("[0-9a-zA-Z]+")
        );
    }

    @Test
    void generateBase62_shouldThrowException_whenUrlIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> generator.generateBase62(null)
                );

        assertEquals(
                "URL cannot be null or empty",
                exception.getMessage()
        );
    }

    @Test
    void generateBase62_shouldThrowException_whenUrlIsBlank() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> generator.generateBase62("   ")
                );

        assertEquals(
                "URL cannot be null or empty",
                exception.getMessage()
        );
    }
}
