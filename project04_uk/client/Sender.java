/**
 * PROYECTO #4
 * NOMBRE: Gloria Oliva Olivares Ménez
 * GRUPO: 4CM12
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Sender implements Runnable{

    public static Stack<String> words;
    
    public String uriStr;
    public HttpClient client;

    public Sender(String ip) {
        uriStr = "http://" + ip + "/search?word=";

        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        
    }

    @Override
    public void run() {
        while(true){
            try {
                if (words.isEmpty()) break;
                String word = words.pop();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uriStr + word)).build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();

                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setStack(Stack<String> wStack) {
        Sender.words = wStack;
    }
}