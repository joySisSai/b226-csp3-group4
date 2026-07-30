package com.barangayconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MainSmokeTest {
    @Test
    void printsApplicationNameAndExits() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Main.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(
                Main.APPLICATION_NAME + System.lineSeparator(),
                output.toString(StandardCharsets.UTF_8));
    }
}
