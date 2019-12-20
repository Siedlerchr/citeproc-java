package de.undercouch.citeproc.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SmartQuotes}
 * @author MicheL Kraemer
 */
public class SmartQuotesTest {
    /**
     * Test the {@link SmartQuotes#apply(String)} method
     */
    @Test
    public void apply() {
        SmartQuotes sq = new SmartQuotes();

        // tests from smartquotes.js (https://smartquotes.js.org/)
        // written by Kelly Martin released under the MIT license
        assertEquals("\u201ctest\u201d", sq.apply("\"test\""));
        assertEquals("the\u2014 \u201ctest\u201d", sq.apply("the\u2014 \"test\""));
        assertEquals("\u2018test\u2019", sq.apply("'test'"));
        assertEquals("ma\u2019am", sq.apply("ma'am"));
        assertEquals("\u2019em", sq.apply("'em"));
        assertEquals("Marshiness of \u2019Ammercloth\u2019s",
                sq.apply("Marshiness of 'Ammercloth's"));
        assertEquals("\u201995", sq.apply("'95"));
        assertEquals("\u2034", sq.apply("'''"));
        assertEquals("\u2033", sq.apply("''"));
        assertEquals("\u201cBetter than a 6\u20325\u2033 whale.\u201d",
                sq.apply("\"Better than a 6'5\" whale.\""));
        assertEquals("\u201cIt\u2019s my \u2018#1\u2019 choice!\u201d - 12\u2033 Foam Finger from \u201993",
                sq.apply("\"It's my '#1' choice!\" - 12\" Foam Finger from '93"));
        assertEquals("\u201cSay \u2018what?\u2019\u201d says a Mill\u2019s Pet Barn employee.",
                sq.apply("\"Say 'what?'\" says a Mill's Pet Barn employee."));
        assertEquals("\u201cQuote?\u201d: Description",
                sq.apply("\"Quote?\": Description"));
        assertEquals("\u2018Quo Te?\u2019: Description",
                sq.apply("'Quo Te?': Description"));
        assertEquals("\u201cDe Poesjes van Kevin?\u201d: Something, something",
                sq.apply("\"De Poesjes van Kevin?\": Something, something"));
        assertEquals("And then she blurted, \u201cI thought you said, \u2018I don\u2019t like \u201980s music\u2019?\u201d",
                sq.apply("And then she blurted, \"I thought you said, 'I don't like '80s music'?\""));

        // further tests
        assertEquals("That\u2019s and it\u2019s and couldn\u2019t.",
                sq.apply("That's and it's and couldn't."));
        assertEquals("\u201C\u2018That\u2019s so cool,\u2019 he said.\u201D",
                sq.apply("\"'That's so cool,' he said.\""));
        assertEquals("\u201C\u2018That\u2019s so \u201Ccool\u201D,\u2019 he said.\u201D",
                sq.apply("\"'That's so \"cool\",' he said.\""));
    }
}
