package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Content-Length header parsing with malformed values.
 * This ensures the NumberFormatException vulnerability is properly handled.
 */
public class ContentLengthParsingTest {

    @Test
    public void validContentLengthReturnsCorrectValue() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 12345\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(12345L, parser.bodyLength());
    }

    @Test
    public void contentLengthWithWhitespaceIsTrimmedAndParsed() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length:   999   \n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(999L, parser.bodyLength());
    }

    @Test
    public void nonNumericContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: abc\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void decimalContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 12.5\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void emptyContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: \n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void partiallyNumericContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 123abc\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void oversizedContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 999999999999999999999999999999\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void missingContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-1L, parser.bodyLength());
    }

    @Test
    public void caseInsensitiveContentLengthHeader() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "CoNtEnT-LeNgTh: 456\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(456L, parser.bodyLength());
    }

    @Test
    public void negativeContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: -100\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(-100L, parser.bodyLength());
    }

    @Test
    public void zeroContentLength() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 0\n" +
                        "\n" +
                        CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertEquals(0L, parser.bodyLength());
    }

    @Test
    public void connectorSocketV3_validContentLengthReturnsCorrectValue() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 54321\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(54321L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_nonNumericContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: invalid\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_decimalContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 99.9\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_emptyContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: \n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_oversizedContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 99999999999999999999999999\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_contentLengthWithWhitespaceIsTrimmedAndParsed() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length:   888   \n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(888L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_partiallyNumericContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 456xyz\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_missingContentLengthReturnsMinusOne() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-1L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_caseInsensitiveContentLengthHeader() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "CoNtEnT-LeNgTh: 777\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(777L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_negativeContentLength() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: -200\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(-200L, request.bodyLength());
    }

    @Test
    public void connectorSocketV3_zeroContentLength() {
        String message = "GET /path HTTP/1.1\n" +
                        "Host: example.com\n" +
                        "Content-Length: 0\n";
        
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(message);
        assertEquals(0L, request.bodyLength());
    }
}
