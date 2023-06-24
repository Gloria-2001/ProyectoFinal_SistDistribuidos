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

package com.mycompany.app;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.InputStream;  
import java.math.BigInteger;
import java.util.Arrays;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.DeserializationFeature;   
import com.fasterxml.jackson.databind.ObjectMapper;             
import com.fasterxml.jackson.databind.PropertyNamingStrategy;   

import java.io.IOException;
import java.net.URI;
import java.io.*;
import java.util.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.lang.Object;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpRequest.BodyPublishers;



public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";
    private static final String SUB_PROCESS = "/dividir";

    private static final HttpClient httpClient = HttpClient.newBuilder()
	.version(HttpClient.Version.HTTP_1_1)
	.connectTimeout(Duration.ofSeconds(10))
	.build();

    private final int port; 
    private HttpServer server; 
    private final ObjectMapper objectMapper;

    public WebServer(int port) {
        this.port = port;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT); 
        HttpContext taskContext = server.createContext(ENDPOINT_PROCESS);
        HttpContext homePageContext = server.createContext(HOME_PAGE_ENDPOINT);
        HttpContext subProcessContext = server.createContext(SUB_PROCESS);
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleRequestForAsset);
        subProcessContext.setHandler(this::handleSubRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
    
    

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
       // System.out.println("Primer método HTTP: " + exchange.getRequestMethod());

        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath(); 
        //System.out.println("Asset: " + asset);
        if (asset.equals(HOME_PAGE_ENDPOINT)) { 
            response = readUiAsset(HOME_PAGE_UI_ASSETS_BASE_DIR + "index.html");
        } else {
            response = readUiAsset(asset); 
        }
        addContentType(asset, exchange);
        sendResponse(response, exchange);
    }

    private byte[] readUiAsset(String asset) throws IOException {
        InputStream assetStream = getClass().getResourceAsStream(asset);

        if (assetStream == null) {
            return new byte[]{};
        }
        return assetStream.readAllBytes(); 
    }

    private static void addContentType(String asset, HttpExchange exchange) {

        String contentType = "text/html";  
        if (asset.endsWith("js")) {
            contentType = "text/javascript";
        } else if (asset.endsWith("css")) {
            contentType = "text/css";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }

        try {
            FrontendSearchRequest frontendSearchRequest = objectMapper.readValue(exchange.getRequestBody().readAllBytes(), FrontendSearchRequest.class); 
            String frase = frontendSearchRequest.getSearchQuery(); //Obtenemos la frase directo de la pagina
            int numero = Integer.parseInt(frase);
            //voy a crear el cliente que manda 3 solicitudes diferentes a los servidores con los rangos
            BigInteger result = BigInteger.ONE;
            String ports[] = {"3001","3002","3003"};
 
	List<Range> ranges = divideIntoRanges(numero, 3);
	int i = 0;
	for (Range range : ranges) {
	System.out.println("Inicio: " + range.getStart() + ", Fin: " + range.getEnd());
	byte[] responseSubBytes = Sender("localhost:"+ports[i],range.getStart()+","+range.getEnd());  
	FrontendSearchResponse objeto = objectMapper.readValue(responseSubBytes,FrontendSearchResponse.class);
	BigInteger bigInteger = objeto.getnumero();
	result = result.multiply(bigInteger); 
	i++;
	}
                    
            System.out.println("Enviando: "+result);              
            FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(result);
            byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
	    
            sendResponse(responseBytes, exchange);
            
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
    private void handleSubRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }

        try {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes(); //LEE ALGO ASI "32,34"
            byte[] responseBytes = calculateResponse(requestBytes);
            sendResponse(responseBytes, exchange);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage.getBytes(), exchange);
    }
    
    private byte[] calculateResponse(byte[] requestBytes) {
	String rango = new String(requestBytes);
        String[] limites = rango.split(",");
        int inicio = Integer.parseInt(limites[0]);
        int fin = Integer.parseInt(limites[1]);
        String bodyString = new String("1");
	for (int i=inicio;i<=fin;i++)
	{
	bodyString =  bodyString + "," + Integer.toString(i);
	}
	
        String[] stringNumbers = bodyString.split(",");
        BigInteger result = BigInteger.ONE;
        
        for (String number : stringNumbers) {
            BigInteger bigInteger = new BigInteger(number);
            result = result.multiply(bigInteger);
        
        }
        try{
        //System.out.println(Arrays.toString(stringNumbers) +" = "+result );
        FrontendSearchResponse frontendSearchResponse = new FrontendSearchResponse(result);
        byte[] responseBytes = objectMapper.writeValueAsBytes(frontendSearchResponse);
        return responseBytes;
        }catch(Exception ex)
	{
	System.out.println(ex.getStackTrace());
	}
	return null;
}

	private static byte[] Sender(String ip, String Rango)
	{
	byte[] requestBytesAux = null;
	try{
	// Crear la solicitud POST
	HttpRequest request = HttpRequest.newBuilder()
	.POST(HttpRequest.BodyPublishers.ofString(Rango))
	.uri(URI.create("http://"+ip+"/dividir"))
	.setHeader("Content-Type", "text/plain")
	.build();

	// Enviar la solicitud al servidor
	HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

	// print response headers
	HttpHeaders headers = response.headers();
	headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

	// print status code
	System.out.println(response.statusCode());

	// print response body
	byte[] requestBytes = response.body();
	return requestBytes;
	}catch(Exception ex)
	{
	System.out.println(ex.getStackTrace());
	}
	return requestBytesAux;
	}
	
    
	
    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {

        exchange.sendResponseHeaders(200, responseBytes.length);
        //System.out.println("Bytes sent: " + responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }
    
    public static List<Range> divideIntoRanges(int number, int n) {
        List<Range> ranges = new ArrayList<>();
        
        // Calcula el tamaño de cada rango
        int rangeSize = number / n;
        
        // Calcula el residuo, que se distribuirá entre los rangos
        int remainder = number % n;
        
        // Crea los rangos
        int start = 1;
        int end;
        for (int i = 0; i < n; i++) {
            // Incrementa el tamaño del rango si hay residuo
            int extra = i < remainder ? 1 : 0;
            
            // Calcula los límites del rango actual
            end = start + rangeSize + extra -1;
            
            // Crea un objeto Range con el inicio y fin del rango
            Range range = new Range(start, end);
            
            // Agrega el rango a la lista
            ranges.add(range);
            
            // Actualiza el inicio del próximo rango
            start = end +1;
        }
        
        return ranges;
    }


}


class Range {
    private int start;
    private int end;
    
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
}


