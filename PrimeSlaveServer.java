import java.io.*;
import java.net.*;
import java.util.List;

public class PrimeSlaveServer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java PrimeSlaveServer <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]); // Get port number from command-line argument
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Slave Server listening on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        String[] tokens = inputLine.split(" ");
                        int start = Integer.parseInt(tokens[0]);
                        int end = Integer.parseInt(tokens[1]);
                        // Assuming a fixed number of threads for simplicity, adjust as necessary
                        int numThreads = Runtime.getRuntime().availableProcessors();

                        PrimeChecker primeChecker = new PrimeChecker(start, end, numThreads);
                        primeChecker.startFindingPrimes(); // Start the prime finding process
                        List<Integer> primes = primeChecker.getPrimes(); // Retrieve the found primes
                        
                        // Send the serialized list of primes back to the master server
                        out.writeObject(primes);
                        out.flush();
                    }
                } catch (IOException e) {
                    System.err.println("Exception in Slave Server: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
}
