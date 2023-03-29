package pl.asie.zima.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

public class MiscUtilsTest {
    @SuppressWarnings("StringBufferReplaceableByString")
    @Test
    public void zztToUtfTest() {
        StringBuilder s = new StringBuilder();
        s.appendCodePoint(0xDB);
        s.appendCodePoint(0xAA);
        String converted = MiscUtils.zztToUtf(s.toString());
        Assertions.assertEquals("█¬", converted);
    }
}
