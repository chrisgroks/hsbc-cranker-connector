package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConnectorSocketV3Test {

    @Test
    void crankerRequest_bodyLength_returnsLengthForValidContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:12345\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(12345L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForNonNumericContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:abc\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForDecimalContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:12.5\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForEmptyContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForWhitespaceOnlyContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:   \n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneWhenNoContentLengthHeader() {
        String msg = "GET /api/test HTTP/1.1\n" +
                    "Host: example.com\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_handlesContentLengthWithExtraCharacters() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:123abc\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_handlesNegativeContentLength() {
        String msg = "POST /api/test HTTP/1.1\n" +
                    "Content-Length:-100\n" +
                    "\n";
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(msg);
        assertThat(request.bodyLength(), is(-100L));
    }
}
