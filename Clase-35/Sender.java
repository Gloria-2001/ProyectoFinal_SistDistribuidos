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
    public static int number;
    public static int service;
    public String uriStr;
    public HttpClient client;
    public Sender(String ip) {
        uriStr = "http://" + ip + "/servicios?numero=&servicio=";
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
  
   @Override
    public void run() {
        while(true){
            try {
                String uriStrDivided[] = uriStr.split("&");
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uriStrDivided[0] + number + uriStrDivided[1] + service)).build();
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
}