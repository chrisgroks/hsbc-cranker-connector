package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ContentLengthParsingTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "Content-Length: abc",
        "Content-Length: 12.5",
        "Content-Length: not-a-number",
        "content-length: xyz123",
        "content-length: 123abc",
        "Content-Length: ",
        "Content-Length:   ",
        "Content-Length:\t",
        "content-length: 999999999999999999999999999999",
        "content-length: 99999999999999999999",
        "Content-Length: 100$",
        "Content-Length: 1,000",
        "Content-Length: 1_000"
    })
    void crankerRequestParser_bodyLength_returnsMinusOneForInvalidContentLength(String header) {
        assertThat(parseCrankerRequestBodyLength(header), is(-1L));
    }

    @ParameterizedTest
    @CsvSource({
        "Content-Length: 100, 100",
        "Content-Length: 0, 0",
        "content-length: 12345, 12345",
        "Content-Length:   500  , 500",
        "CONTENT-LENGTH: 999, 999"
    })
    void crankerRequestParser_bodyLength_returnsCorrectValueForValidContentLength(String header, long expected) {
        assertThat(parseCrankerRequestBodyLength(header), is(expected));
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

    @ParameterizedTest
    @ValueSource(strings = {
        "Content-Length: abc",
        "Content-Length: 12.5",
        "Content-Length: not-a-number",
        "content-length: xyz123",
        "content-length: 123abc",
        "Content-Length: ",
        "Content-Length:   ",
        "Content-Length:\t",
        "content-length: 999999999999999999999999999999",
        "content-length: 99999999999999999999",
        "Content-Length: 100$",
        "Content-Length: 1,000",
        "Content-Length: 1_000"
    })
    void connectorSocketV3_bodyLength_returnsMinusOneForInvalidContentLength(String header) throws Exception {
        assertThat(parseConnectorSocketV3BodyLength(header), is(-1L));
    }

    @ParameterizedTest
    @CsvSource({
        "Content-Length: 100, 100",
        "Content-Length: 0, 0",
        "content-length: 12345, 12345",
        "Content-Length:   500  , 500",
        "CONTENT-LENGTH: 999, 999"
    })
    void connectorSocketV3_bodyLength_returnsCorrectValueForValidContentLength(String header, long expected) throws Exception {
        assertThat(parseConnectorSocketV3BodyLength(header), is(expected));
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
            if ("CrankerRequest".equals(innerClass.getSimpleName())) {
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
