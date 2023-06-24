

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.math.*;
import java.util.function.Supplier;
import java.io.*;


public class ServidorBusqueda {

    
    private static final String DIRECCION_LIBROS = "LIBROS_TXT/";
    private static final String DIRECCION_SERIALIZABLE = "ser.bin";
    private static final String LIVE_ENDPOINT = "/task";
    private static final String SEARCHTOKEN_ENDPOINT = "/searchtoken";

    private final int port;
    private HttpServer server;
    private Libro[] listaArchivos;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }


        ServidorBusqueda webServer = new ServidorBusqueda(serverPort);
        webServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext liveContext = server.createContext(LIVE_ENDPOINT);
        HttpContext searchtokenContext = server.createContext(SEARCHTOKEN_ENDPOINT);

        liveContext.setHandler(this::handleLiveRequest);
        searchtokenContext.setHandler(this::handleSearchTokenRequest);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
    }

    public ServidorBusqueda(int port) {
        this.port = port;
        
        File bin = new File(DIRECCION_SERIALIZABLE);
        if(bin.exists()){
            try{
                FileInputStream archivoEntrada = new FileInputStream(DIRECCION_SERIALIZABLE);
                ObjectInputStream objetoEntrada = new ObjectInputStream(archivoEntrada);
                listaArchivos = (Libro[]) objetoEntrada.readObject();
                objetoEntrada.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            File directorio = new File(DIRECCION_LIBROS);
            String[] arbol = directorio.list();
            long conteo;
            listaArchivos = new Libro[arbol.length];
            for (int i = 0; i < listaArchivos.length; i++) {
                File actual = new File(DIRECCION_LIBROS + arbol[i]);
                conteo = 0;
                try {
                    System.out.println(DIRECCION_LIBROS + arbol[i]);
                    Scanner scan = new Scanner(actual);
                    while (scan.hasNext()) {
                        scan.next();
                        conteo++;
                    }
                    scan.close();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                listaArchivos[i] = new Libro(DIRECCION_LIBROS + arbol[i], conteo);
            }

            try{
                FileOutputStream archivoSalida = new FileOutputStream(DIRECCION_SERIALIZABLE);
                ObjectOutputStream objetoSalida = new ObjectOutputStream(archivoSalida);
                objetoSalida.writeObject(listaArchivos);
                objetoSalida.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            
        }

        
    }

    private void handleLiveRequest(HttpExchange exchange) throws IOException {
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue().toArray()));
        }
        if (headers.containsKey("X-Test") && headers.get("X-Test").get(0).equalsIgnoreCase("true")) {
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        long startTime = System.nanoTime();


        byte[] respuesta = "200".getBytes();
        


        long finishTime = System.nanoTime();
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", finishTime - startTime);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }

        sendResponse(respuesta, exchange);
    }

    private void handleSearchTokenRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }
        Headers headers = exchange.getRequestHeaders();
        boolean isDebugMode = false;
        if (headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")) {
            isDebugMode = true;
        }
        long tiempo = System.nanoTime();


        String entrada = new String(exchange.getRequestBody().readAllBytes());
        String[] numeros = entrada.split(";")[0].split(",");
        String[] palabras = entrada.split(";")[1].split(",");
        byte[] respuesta = buscarPalabras(Integer.parseInt(numeros[0]),Integer.parseInt(numeros[1]),palabras);



        tiempo = System.nanoTime() - tiempo;
        if (isDebugMode) {
            String debugMessage = String.format("La operación tomó %d nanosegundos", tiempo);
            exchange.getResponseHeaders().put("X-Debug-Info", Arrays.asList(debugMessage));
        }
        sendResponse(respuesta, exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    private Map<String, Integer> b1uscarPalabras(int n, String[] palabra) {
        TreeMap<String, Integer> ocurrencias = new TreeMap<>();
        char[] cadenaLarga = new char[n * 4];

        for (int i = 0; i < n; i++) {
            cadenaLarga[i * 4] = (char) Math.floor(Math.random() * ('Z' - 'A' + 1) + 'A');
            cadenaLarga[i * 4 + 1] = (char) Math.floor(Math.random() * ('Z' - 'A' + 1) + 'A');
            cadenaLarga[i * 4 + 2] = (char) Math.floor(Math.random() * ('Z' - 'A' + 1) + 'A');
            cadenaLarga[i * 4 + 3] = ' ';
        }

        for (int i = 1; i < palabra.length; i++) {
            ocurrencias.put(palabra[i], 0);
            for (int j = 0; j < n; j++) {
                if (cadenaLarga[j * 4] == palabra[i].charAt(0) &&
                        cadenaLarga[j * 4 + 1] == palabra[i].charAt(1) &&
                        cadenaLarga[j * 4 + 2] == palabra[i].charAt(2)) {
                    ocurrencias.replace(palabra[i], ocurrencias.get(palabra[i]) + 1);
                }
            }
        }

        return ocurrencias;
    }


    private byte[] buscarPalabras(int inicio,int termino, String[] palabras){
        String salida = "";
        PalabraEncontrada encontradas[][] = new PalabraEncontrada[termino - inicio + 1][palabras.length];
        File actual;
        Scanner scan;
        for (int i = inicio; i <= termino; i++) {
            actual = new File(listaArchivos[i].getURL());
            for (int j = 0; j < palabras.length; j++) {
                try {
                    scan = new Scanner(actual);
                    encontradas[i - inicio][j] = new PalabraEncontrada(scan.findAll(palabras[j]).count(), i,palabras[j]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        for (int i = 0; i < encontradas.length; i++) {
            for (int j = 0; j < palabras.length; j++) {
                salida += encontradas[i][j].toString();
            }
        }
        return salida.getBytes();
    }

    class PalabraEncontrada{
        private long ocurrencias;
        private long cantidadTotalPalabras;
        private String libro;
        private String palabra;

        public PalabraEncontrada(long ocu,int lugar,String pal){
            this.ocurrencias = ocu;
            this.cantidadTotalPalabras = listaArchivos[lugar].getPalabras();
            this.libro = listaArchivos[lugar].getURL();
            this.palabra = pal;
        }

        public String toString(){
            return libro + ": "+ palabra+ " " + ocurrencias + " de " + cantidadTotalPalabras + "\n";
        }
    }

}



