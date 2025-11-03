package com.hsbc.cranker.connector;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CrankerRequestParserTest {

    @Test
    void bodyLength_returnsLengthForValidContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.validContentLengthMessage(12345, true));
        assertThat(parser.bodyLength(), is(12345L));
    }

    @Test
    void bodyLength_returnsNegativeOneForNonNumericContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.nonNumericContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForDecimalContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.decimalContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForEmptyContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.emptyContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneForWhitespaceOnlyContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.whitespaceContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_returnsNegativeOneWhenNoContentLengthHeader() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.noContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_handlesContentLengthWithExtraCharacters() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.extraCharactersContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-1L));
    }

    @Test
    void bodyLength_handlesNegativeContentLength() {
        CrankerRequestParser parser = new CrankerRequestParser(ContentLengthTestData.negativeContentLengthMessage(true));
        assertThat(parser.bodyLength(), is(-100L));
    }
}
