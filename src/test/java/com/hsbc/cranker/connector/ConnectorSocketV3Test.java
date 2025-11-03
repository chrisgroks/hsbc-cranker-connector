package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConnectorSocketV3Test {

    @Test
    void crankerRequest_bodyLength_returnsLengthForValidContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.validContentLengthMessage(12345, false));
        assertThat(request.bodyLength(), is(12345L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForNonNumericContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.nonNumericContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForDecimalContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.decimalContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForEmptyContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.emptyContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneForWhitespaceOnlyContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.whitespaceContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_returnsNegativeOneWhenNoContentLengthHeader() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.noContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_handlesContentLengthWithExtraCharacters() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.extraCharactersContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-1L));
    }

    @Test
    void crankerRequest_bodyLength_handlesNegativeContentLength() {
        ConnectorSocketV3.CrankerRequest request = new ConnectorSocketV3.CrankerRequest(ContentLengthTestData.negativeContentLengthMessage(false));
        assertThat(request.bodyLength(), is(-100L));
    }
}
