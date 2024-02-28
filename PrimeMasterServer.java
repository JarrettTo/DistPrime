import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PrimeMasterServer {
    private static final int PORT = 12444;
    private static ExecutorService pool = Executors.newCachedThreadPool(); // Use cached thread pool for flexibility
    // List of slave server ports for simplicity; replace with actual IP addresses and ports if needed
    private static final int[] slaveServerPorts = {12346, 12347, 12348};
    
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Master server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new MasterServerHandler(clientSocket, slaveServerPorts));
            }
        }
    }
}

class MasterServerHandler implements Runnable {
    private Socket clientSocket;
    private int[] slaveServerPorts;
    private ExecutorService executorService;

    MasterServerHandler(Socket socket, int[] slaveServerPorts) {
        this.clientSocket = socket;
        this.slaveServerPorts = slaveServerPorts;
        System.out.println("CHECK:"+ slaveServerPorts);
        executorService = Executors.newFixedThreadPool(slaveServerPorts.length);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine; // Read the input line once
            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.trim().isEmpty()) {
                    String[] tokens = inputLine.split(" ");
                    int start = Integer.parseInt(tokens[0]);
                    int end = Integer.parseInt(tokens[1]);

                    List<Future<List<Integer>>> futures = new ArrayList<>();
                    int rangeSize = (end - start + 1) / slaveServerPorts.length;

                    // Distribute the range and collect futures
                    for (int i = 0; i < slaveServerPorts.length; i++) {
                        int rangeStart = start + i * rangeSize;
                        int rangeEnd = (i == slaveServerPorts.length - 1) ? end : rangeStart + rangeSize - 1;
                        int slavePort = slaveServerPorts[i];

                        @SuppressWarnings("unchecked")
                        Future<List<Integer>> future = executorService.submit(() -> {
                            try (Socket slaveSocket = new Socket("localhost", slavePort);
                                PrintWriter slaveOut = new PrintWriter(slaveSocket.getOutputStream(), true);
                                BufferedReader slaveIn = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
                                ObjectInputStream objectIn = new ObjectInputStream(slaveSocket.getInputStream())) {

                                // Send range to slave server
                                slaveOut.println(rangeStart + " " + rangeEnd);
                                slaveOut.flush();

                                // Read primes list from slave server
                                return (List<Integer>) objectIn.readObject();
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            return Collections.emptyList();
                        });

                        futures.add(future);
                    }

                    // Wait for all results and combine them
                    List<Integer> allPrimes = new ArrayList<>();
                    for (Future<List<Integer>> future : futures) {
                        try {
                            allPrimes.addAll(future.get());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

                    // Sort the list of all primes before printing or returning
                    Collections.sort(allPrimes);
                    System.out.println("Aggregated Primes: " + allPrimes);
                    out.println("Aggregated Primes: " + allPrimes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
