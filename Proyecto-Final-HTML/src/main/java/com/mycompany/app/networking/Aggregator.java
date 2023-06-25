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
        for (int i = 0; i < workersAddresses.size(); i++) {
            // Se obtiene la dirección del trabajador
            String workerAddress = workersAddresses.get(i);
            // Se obtiene una tarea a enviar
            String task = tasks.get(i);
            // Se almacenan las tareas en bytes
            byte[] requestPayload = task.getBytes();
            // Se envian las tareas al trabajador usando el método sendTask y se asocian a cada una de los futures
            futures[i] = webClient.sendTask(workerAddress, requestPayload);
        }
        // Se declara una lista donde se reciben los resultados
        List<String> results = new ArrayList();
        for (int i = 0; i < tasks.size(); i++) {
            results.add(futures[i].join());
        }        
        return results;
	
    }
}

