package com.hsbc.cranker.connector;

public class ContentLengthTestData {
    
    public static String validContentLengthMessage(long length, boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:" + length + "\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String nonNumericContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:abc\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String decimalContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:12.5\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String emptyContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String whitespaceContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:   \n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String noContentLengthMessage(boolean withTrailer) {
        String msg = "GET /api/test HTTP/1.1\n" +
               "Host: example.com\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String extraCharactersContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:123abc\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
    
    public static String negativeContentLengthMessage(boolean withTrailer) {
        String msg = "POST /api/test HTTP/1.1\n" +
               "Content-Length:-100\n" +
               "\n";
        return withTrailer ? msg + "_2" : msg;
    }
}
