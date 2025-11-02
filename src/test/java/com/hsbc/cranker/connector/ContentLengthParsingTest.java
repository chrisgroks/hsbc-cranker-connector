package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ContentLengthParsingTest {

    @Test
    void crankerRequestParser_bodyLength_returnsMinusOneForMalformedNumericContentLength() {
        assertThat(parseCrankerRequestBodyLength("Content-Length: abc"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length: 12.5"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length: not-a-number"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("content-length: xyz123"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("content-length: 123abc"), is(-1L));
    }

    @Test
    void crankerRequestParser_bodyLength_returnsMinusOneForEmptyContentLength() {
        assertThat(parseCrankerRequestBodyLength("Content-Length: "), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length:   "), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length:\t"), is(-1L));
    }

    @Test
    void crankerRequestParser_bodyLength_returnsMinusOneForOverflowContentLength() {
        assertThat(parseCrankerRequestBodyLength("content-length: 999999999999999999999999999999"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("content-length: 99999999999999999999"), is(-1L));
    }

    @Test
    void crankerRequestParser_bodyLength_returnsMinusOneForSpecialCharactersInContentLength() {
        assertThat(parseCrankerRequestBodyLength("Content-Length: 100$"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length: 1,000"), is(-1L));
        assertThat(parseCrankerRequestBodyLength("Content-Length: 1_000"), is(-1L));
    }

    @Test
    void crankerRequestParser_bodyLength_returnsCorrectValueForValidContentLength() {
        assertThat(parseCrankerRequestBodyLength("Content-Length: 100"), is(100L));
        assertThat(parseCrankerRequestBodyLength("Content-Length: 0"), is(0L));
        assertThat(parseCrankerRequestBodyLength("content-length: 12345"), is(12345L));
        assertThat(parseCrankerRequestBodyLength("Content-Length:   500  "), is(500L));
        assertThat(parseCrankerRequestBodyLength("CONTENT-LENGTH: 999"), is(999L));
    }

    @Test
    void crankerRequestParser_bodyLength_returnsMinusOneWhenNoContentLengthHeader() {
        String message = "GET /test HTTP/1.1\n" +
                         "Host: example.com\n" +
                         "User-Agent: test\n" +
                         CrankerRequestParser.REQUEST_HAS_NO_BODY_MARKER;
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequestParser_bodyLength_handlesMultipleHeadersCorrectly() {
        String message = "POST /test HTTP/1.1\n" +
                         "Host: example.com\n" +
                         "Content-Length: 12345\n" +
                         "User-Agent: test\n" +
                         CrankerRequestParser.REQUEST_BODY_PENDING_MARKER;
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertThat(parser.bodyLength(), is(12345L));
    }

    @Test
    void crankerRequestParser_bodyLength_handlesContentLengthAsFirstHeader() {
        String message = "POST /test HTTP/1.1\n" +
                         "Content-Length: 999\n" +
                         "Host: example.com\n" +
                         CrankerRequestParser.REQUEST_BODY_PENDING_MARKER;
        CrankerRequestParser parser = new CrankerRequestParser(message);
        assertThat(parser.bodyLength(), is(999L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsMinusOneForMalformedNumericContentLength() throws Exception {
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: abc"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 12.5"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: not-a-number"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("content-length: xyz123"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("content-length: 123abc"), is(-1L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsMinusOneForEmptyContentLength() throws Exception {
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: "), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length:   "), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length:\t"), is(-1L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsMinusOneForOverflowContentLength() throws Exception {
        assertThat(parseConnectorSocketV3BodyLength("content-length: 999999999999999999999999999999"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("content-length: 99999999999999999999"), is(-1L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsMinusOneForSpecialCharactersInContentLength() throws Exception {
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 100$"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 1,000"), is(-1L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 1_000"), is(-1L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsCorrectValueForValidContentLength() throws Exception {
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 100"), is(100L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length: 0"), is(0L));
        assertThat(parseConnectorSocketV3BodyLength("content-length: 12345"), is(12345L));
        assertThat(parseConnectorSocketV3BodyLength("Content-Length:   500  "), is(500L));
        assertThat(parseConnectorSocketV3BodyLength("CONTENT-LENGTH: 999"), is(999L));
    }

    @Test
    void connectorSocketV3_bodyLength_returnsMinusOneWhenNoContentLengthHeader() throws Exception {
        String message = "GET /test HTTP/1.1\n" +
                         "Host: example.com\n" +
                         "User-Agent: test";
        Object crankerRequest = createConnectorSocketV3CrankerRequest(message);
        long result = invokeBodyLength(crankerRequest);
        assertThat(result, is(-1L));
    }

    @Test
    void connectorSocketV3_bodyLength_handlesMultipleHeadersCorrectly() throws Exception {
        String message = "POST /test HTTP/1.1\n" +
                         "Host: example.com\n" +
                         "Content-Length: 12345\n" +
                         "User-Agent: test";
        Object crankerRequest = createConnectorSocketV3CrankerRequest(message);
        long result = invokeBodyLength(crankerRequest);
        assertThat(result, is(12345L));
    }

    @Test
    void connectorSocketV3_bodyLength_handlesContentLengthAsFirstHeader() throws Exception {
        String message = "POST /test HTTP/1.1\n" +
                         "Content-Length: 999\n" +
                         "Host: example.com";
        Object crankerRequest = createConnectorSocketV3CrankerRequest(message);
        long result = invokeBodyLength(crankerRequest);
        assertThat(result, is(999L));
    }

    private long parseCrankerRequestBodyLength(String contentLengthHeader) {
        String message = "POST /test HTTP/1.1\n" +
                         contentLengthHeader + "\n" +
                         CrankerRequestParser.REQUEST_BODY_PENDING_MARKER;
        CrankerRequestParser parser = new CrankerRequestParser(message);
        return parser.bodyLength();
    }

    private long parseConnectorSocketV3BodyLength(String contentLengthHeader) throws Exception {
        String message = "POST /test HTTP/1.1\n" +
                         contentLengthHeader;
        Object crankerRequest = createConnectorSocketV3CrankerRequest(message);
        return invokeBodyLength(crankerRequest);
    }

    private Object createConnectorSocketV3CrankerRequest(String message) throws Exception {
        Class<?>[] innerClasses = ConnectorSocketV3.class.getDeclaredClasses();
        Class<?> crankerRequestClass = null;
        for (Class<?> innerClass : innerClasses) {
            if (innerClass.getSimpleName().equals("CrankerRequest")) {
                crankerRequestClass = innerClass;
                break;
            }
        }
        if (crankerRequestClass == null) {
            throw new IllegalStateException("CrankerRequest inner class not found");
        }
        Constructor<?> constructor = crankerRequestClass.getDeclaredConstructor(ConnectorSocketV3.class, CharSequence.class);
        constructor.setAccessible(true);
        return constructor.newInstance(null, message);
    }

    private long invokeBodyLength(Object crankerRequest) throws Exception {
        Method bodyLengthMethod = crankerRequest.getClass().getDeclaredMethod("bodyLength");
        bodyLengthMethod.setAccessible(true);
        return (long) bodyLengthMethod.invoke(crankerRequest);
    }
}
