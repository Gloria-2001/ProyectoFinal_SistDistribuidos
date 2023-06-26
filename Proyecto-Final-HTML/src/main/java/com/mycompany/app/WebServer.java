package com.mycompany.app;

import com.mycompany.app.networking.Aggregator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.io.InputStream;
import java.util.*;

import com.fasterxml.jackson.databind.DeserializationFeature;   
import com.fasterxml.jackson.databind.ObjectMapper;    
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;  
import com.fasterxml.jackson.databind.PropertyNamingStrategy;   

public class WebServer {
   
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HOME_PAGE_UI_ASSETS_BASE_DIR = "/ui_assets/";
    private static final String ENDPOINT_PROCESS = "/procesar_datos";
    private static final String BOOKS_ENDPOINT = "/LIBROS_TXT/";

    private static final String WORKER_ADDRESS_1 = "http://127.0.0.1:8080";
    private static final String WORKER_ADDRESS_2 = "http://127.0.0.1:8081";
    private static final String WORKER_ADDRESS_3 = "http://127.0.0.1:8082";
    
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
        HttpContext contentPageContext = server.createContext(BOOKS_ENDPOINT);
        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);
        homePageContext.setHandler(this::handleRequestForAsset);
        contentPageContext.setHandler(this::handleRequestForAsset);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleRequestForAsset(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        byte[] response;

        String asset = exchange.getRequestURI().getPath(); 

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
        } else if (asset.endsWith("txt")) {
            contentType = "text/plain";
        }
        exchange.getResponseHeaders().add("Content-Type", contentType);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) { 
            exchange.close();
            return;
        }

        try {
            Aggregator aggregator = new Aggregator();
            String task1,task2,task3;
            String frase = new String(exchange.getRequestBody().readAllBytes());
            
            StringTokenizer st = new StringTokenizer(frase);
            String aux = ";" + st.nextToken();
            while(st.hasMoreTokens()){
                aux = aux + "," + st.nextToken();
            }
            task1 = "0,14"+aux;
            task2 = "15,29"+aux;
            task3 = "30,45"+aux;
            System.out.println(task1);

            List<String> results = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1, WORKER_ADDRESS_2, WORKER_ADDRESS_3),Arrays.asList(task1, task2, task3));

            String respuestafinal = "";
            for (String string : results) {
                respuestafinal += string;
            }
            
            respuestafinal = respuestafinal.substring(0, respuestafinal.length()-2);
            String[] respuestas = respuestafinal.split(";");

            Map<String,Integer> conteo = new HashMap<>();
            ArrayList<Conteo> registro = new ArrayList<>();
            
            for (int i = 0; i < respuestas.length; i++) {
                String nombreLibro = respuestas[i].split(":")[0];
                String[] parametros = respuestas[i].split(":")[1].split(",");
                
                if (!conteo.containsKey(parametros[0])) {
                    conteo.put(parametros[0], 0);
                }

                if (!parametros[1].equals("0")) {
                    conteo.replace(parametros[0], conteo.get(parametros[0])+1);
                }

                boolean existe = true;
                for (int j = 0; j < registro.size(); j++) {
                    if(registro.get(j).libro.equals(nombreLibro)){
                        registro.get(j).agregarPalabra(parametros[0], Long.parseLong(parametros[1]), Double.parseDouble(parametros[1]) / Double.valueOf(parametros[2]));
                        existe = false;
                    }                    
                }

                if(existe){
                    registro.add(new Conteo(nombreLibro));
                    registro.get(registro.size()-1). agregarPalabra(parametros[0], Long.parseLong(parametros[1]), Double.parseDouble(parametros[1]) / Double.valueOf(parametros[2]));
                }
            }

            for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
                System.out.println( entry.getKey() + "  "+ entry.getValue() );
            }

            for (int i = 0; i < registro.size(); i++) {
                registro.get(i).calcularPuntuacion(conteo);
            }

            Collections.sort(registro,new Comparator<Conteo>() {
                public int compare(Conteo o1, Conteo o2) {
                    if(o1.puntuacion > o2.puntuacion){
                        return -1;
                    }else if(o1.puntuacion < o2.puntuacion){
                        return 1;
                    }else{
                        return 0;
                    }
                };
            });
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode array = root.putArray("Libros");
            for(Conteo obj : registro){
                FrontendSearchResponse fsr = new FrontendSearchResponse(obj.libro,obj.puntuacion,obj.getpalabras());
                array.addPOJO(fsr);
            }
            byte[] responseBytes = objectMapper.writeValueAsBytes(root);
            sendResponse(responseBytes, exchange);
        } catch (Exception e) {
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

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }


    class Conteo {
        String libro;
        double puntuacion;
        Map<String,Cantidad> palabras;

        public Conteo(String nombre){
            libro = nombre;
            palabras = new HashMap<>();
        }

        public void agregarPalabra(String nom, long cantidad, double tf){
            palabras.put(nom, new Cantidad(cantidad,tf));
        }

        public double calcularPuntuacion(Map<String,Integer> cantidad){
            puntuacion = 0;
            for (Map.Entry<String, Integer> entry : cantidad.entrySet()) {
                String clave = entry.getKey();
                Integer valor = entry.getValue();
                puntuacion += palabras.get(clave).tf * Math.log10((double)46/(double)valor);
            }
            return puntuacion;
        }

        public String getpalabras(){
            String salida = "";
            for (Map.Entry<String, Cantidad> entry : palabras.entrySet()) {
                salida += entry.getKey() + "  "+ entry.getValue().cantidad + "\n";
            }
            return salida;
        }
        
        @Override
        public String toString() {
            String salida = "" + libro + "  "+puntuacion+"\n";
            for (Map.Entry<String, Cantidad> entry : palabras.entrySet()) {
                salida += entry.getKey() + "  "+ entry.getValue().cantidad + "\n";
            }
            return salida;
        }
    }

    class Cantidad{
        long cantidad;
        double tf;

        public Cantidad(long cantidad, double tf){
            this.cantidad = cantidad;
            this.tf = tf;
        }
    }
}