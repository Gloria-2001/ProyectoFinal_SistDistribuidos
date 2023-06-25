package com.mycompany.app.networking;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Aggregator {
    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    public List<String> sendTasksToWorkers(List<String> workersAddresses, List<String> tasks) {
        CompletableFuture<String>[] futures = new CompletableFuture[tasks.size()];
        
        int j=0;

        //i = solicitud
        //j = servidor
        for (int i = 0; i < futures.length; i++) {
            if(webClient.conexion(workersAddresses.get(j) + ENDPOINT_ESTADO)){
                String workerAddress = workersAddresses.get(j)+ENDPOINT_BUSCAR;
                String task = tasks.get(i);
                byte[] requestPayload = task.getBytes();
                futures[i] = webClient.sendTask(workerAddress, requestPayload);
            }else{
                i--;
            }
            j++;

            if(j == 3){
                j = 0;
            }
        }
        

        // Se declara una lista donde se reciben los resultados
        List<String> results = new ArrayList();
        for (int i = 0; i < tasks.size(); i++) {
            results.add(futures[i].join());
        }        
        return results;
	
    }
}

