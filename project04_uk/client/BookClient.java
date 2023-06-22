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

public class BookClient {
    
    private ArrayList<String> address;
    private Stack<String> words;

    public BookClient() {
        address = new ArrayList<String>();
        words = new Stack<String>();
    }

    public void init() {
        try {
            Sender.setStack(words);

            ArrayList<Thread> threadz = new ArrayList<Thread>();

            for (String ip : address) {
                threadz.add(new Thread(new Sender(ip), ip));
            }

            for (Thread th : threadz) {
                th.start();
            }

            for (Thread th : threadz) {
                th.join();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void addAddress(String address) {
        this.address.add(address);
    }

    public int addressLength() {
        return address.size();
    }

    public void addWord(String word) {
        this.words.push(word);
    }

    public int wordsLength() {
        return words.size();
    }

    public static boolean isIpAddress(String address) {
        String regex = "^(localhost|(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))(?::\\d+)?$";
        return Pattern.compile(regex).matcher(address).matches();
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("./BookClient ipaddress:port words");
            return;
        }

        BookClient bookClient = new BookClient();

        for (String arg : args) {
            if (BookClient.isIpAddress(arg)) bookClient.addAddress(arg);
            else bookClient.addWord(arg);
        }

        if (bookClient.addressLength() < 0 || bookClient.wordsLength() < 0) {
            System.out.println("./BookClient ipAddress:port words");
            return;
        }

        
        try {
            bookClient.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
