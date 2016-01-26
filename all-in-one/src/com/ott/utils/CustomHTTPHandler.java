package com.ott.utils;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CustomHTTPHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange paramHttpExchange) throws IOException {
		String response = "This is the response";
		paramHttpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = paramHttpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
	}

}
