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
import java.util.Stack;
import java.io.*;
import java.util.concurrent.Executors;
public class WebServer {
    private static final String SERVICES_ENDPOINT = "/servicios";
    private final int port;
    private HttpServer server;
    public WebServer(int port) {
        this.port = port;
    }
    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        HttpContext statusContext = server.createContext(SERVICES_ENDPOINT);
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
        HashMap<String, Integer> params = getParams(query);
        int code;
        String responseMessage= "";
        int number = params.get("numero");
        int service = params.get("servicio");
        switch(service){
          case 1:
            code=200;
            if(isPrime(number)){
               responseMessage = "El numero "+number+" es primo";
            }else{
               responseMessage = "El numero "+number+" NO es primo";
            }
          break;
          case 2:
            code =200;
            int suma = oddAddition(number);
            responseMessage = "La suma de los impares menores a "+number+" es "+suma;
          break;
          case 3:
             String responseMessage1 = "";
            code=200;
            if(isPrime(number)){
               responseMessage1 = "El numero "+number+" es primo";
            }else{
               responseMessage1 = "El numero "+number+" NO es primo";
            }
            suma = oddAddition(number);
            String responseMessage2 = "La suma de los impares menores a "+number+" es "+suma;
            responseMessage = responseMessage1 + "\n" + responseMessage2;
          break;
          default:
            code = 400;
            responseMessage = "error";
          break;
        }
        sendResponse(responseMessage.getBytes(), exchange, code);
    }
    private HashMap<String, Integer> getParams(String link) {
        String paramArray[] = link.split("&");
        HashMap<String, Integer> params = new HashMap<String, Integer>();
        for (String param : paramArray) {
            String data[] = param.split("=");
            params.put(data[0], Integer.parseInt(data[1]));
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
     private boolean isPrime(int n){
        Stack<Integer> divisores = new Stack<Integer>();
        for(int i=1; i<n; i++){
            if(n%i==0){
                divisores.push(i);
            }
        }
        if(divisores.size()>1){
            return false;
        }else{
            return true;
        }
    }
    private int oddAddition(int n){
        int sum = 0;
        for(int j=0; j<n; j++){
            if(j%2!=0){
                sum=j+sum;
            }
        }
        return sum;
    }
  
   public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1)
            serverPort = Integer.parseInt(args[0]);
        WebServer webServer = new WebServer(serverPort);
        webServer.startServer();
        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }
}
