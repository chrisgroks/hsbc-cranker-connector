package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CrankerRequestParserTest {

    @Test
    void bodyLength_returnsLengthForValidContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:12345\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(12345L));
    }

    @Test
    void bodyLength_returnsNegativeOneForNonNumericContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:abc\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForDecimalContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:12.5\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForEmptyContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForWhitespaceOnlyContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:   \n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneWhenNoContentLengthHeader() {
        String msg = "GET /api/test HTTP/1.1\n" +
                    "Host: example.com\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_handlesContentLengthWithExtraCharacters() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:123abc\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_handlesNegativeContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:-100\n" +
                    "\n" +
                    "_2";
        CrankerRequestParser parser = new CrankerRequestParser(msg);
        assertThat(parser.bodyLength(), is(-100L));
    }
}
