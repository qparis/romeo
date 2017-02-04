package fr.qparis.romeo.sql;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

public class HSQLTypeMatrixTest {
    @Test
    public void testGetFromSample() {
        assertEquals(HSQLTypeMatrix.STRING, HSQLTypeMatrix.guessFromSample(""));
        assertEquals(HSQLTypeMatrix.STRING, HSQLTypeMatrix.guessFromSample("aa"));
        assertEquals(HSQLTypeMatrix.NUMBER, HSQLTypeMatrix.guessFromSample(12));
        assertEquals(HSQLTypeMatrix.NUMBER, HSQLTypeMatrix.guessFromSample(new BigDecimal(12)));
        assertEquals(HSQLTypeMatrix.DATE, HSQLTypeMatrix.guessFromSample(new Date()));
    }

    @Test
    public void testToString() {
        assertEquals("1991-06-26", HSQLTypeMatrix.toString(new Date(91, 5, 26)));
    }

    @Test
    public void testReduce() {
        assertEquals(
                HSQLTypeMatrix.STRING,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.STRING,
                        HSQLTypeMatrix.STRING
                )
        );

        assertEquals(
                HSQLTypeMatrix.STRING,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.NUMBER,
                        HSQLTypeMatrix.STRING
                )
        );

        assertEquals(
                HSQLTypeMatrix.STRING,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.STRING,
                        HSQLTypeMatrix.NUMBER
                )
        );

        assertEquals(
                HSQLTypeMatrix.NUMBER,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.NUMBER,
                        HSQLTypeMatrix.NUMBER
                )
        );

        assertEquals(
                HSQLTypeMatrix.STRING,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.DATE,
                        HSQLTypeMatrix.NUMBER
                )
        );

        assertEquals(
                HSQLTypeMatrix.DATE,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.DATE,
                        HSQLTypeMatrix.DATE
                )
        );

        assertEquals(
                HSQLTypeMatrix.DATE,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.NULL,
                        HSQLTypeMatrix.DATE
                )
        );

        assertEquals(
                HSQLTypeMatrix.NUMBER,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.NULL,
                        HSQLTypeMatrix.NUMBER
                )
        );

        assertEquals(
                HSQLTypeMatrix.STRING,
                HSQLTypeMatrix.reduce(
                        HSQLTypeMatrix.NULL,
                        HSQLTypeMatrix.STRING
                )
        );
    }
}