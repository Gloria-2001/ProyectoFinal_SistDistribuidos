/**
 * PROYECTO #4
 * NOMBRE: Gloria Oliva Olivares Ménez
 * GRUPO: 4CM12
 */

/*
 *  MIT License
 *
 *  Copyright (c) 2019 Michael Pogrebinsky - Distributed Systems & Cloud Computing with Java
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class BookServer {
    private static final String SEARCH_ENDPOINT = "/search";

    private final int port;
    private HttpServer server;

    public BookServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(SEARCH_ENDPOINT);

        statusContext.setHandler(this::handleSearchCheckRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleSearchCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        HashMap<String, String> params = getParams(query);

        int code = 400;
        String responseMessage= "error";
        String word = params.get("word");

        if (word.length() > 0) {
            System.out.print(word + ": ");
            int rep = searchWordInFile(word);
            System.out.println(rep);
            code = 200;
            responseMessage = "127.0.0.1:" + this.port + ". The word " + word + " is " + rep + " times.";
        }

        sendResponse(responseMessage.getBytes(), exchange, code);
    }

    private HashMap<String, String> getParams(String link) {
        String paramArray[] = link.split("&");
        HashMap<String, String> params = new HashMap<String, String>();
        for (String param : paramArray) {
            String data[] = param.split("=");
            params.put(data[0], data[1]);
        }

        return params;
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange, int code) throws IOException {
        exchange.sendResponseHeaders(code, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    private int searchWordInFile(String wordCount) {
        try {
            int count = 0;
            String filePath = "./BIBLIA_COMPLETA.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+|\\p{Punct}"); // Split the line with whitespace
                for (String word : words) {
                    if (word.equalsIgnoreCase(wordCount)) {
                        count++;
                    }
                }
            }
            reader.close();
            return count;
        } catch (Exception e) {
            System.err.println(e);
            return 0;
        }
    }

    public static void main(String[] args) {
        int serverPort = 8080;
        
        if (args.length == 1)
            serverPort = Integer.parseInt(args[0]);

        BookServer bookServer = new BookServer(serverPort);
        bookServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
}
