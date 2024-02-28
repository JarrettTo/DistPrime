import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PrimeClient {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the port number: ");
        int port = scanner.nextInt();
        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Enter the start and end points for the prime search (e.g., \"1 100\"): ");
            String userInput;
            while ((userInput = stdIn.readLine()) != null && !userInput.isEmpty()) {
                long startTime = System.currentTimeMillis();
                out.println(userInput); // Send range to master server
                System.out.println("Response from server: " + in.readLine()); // Read response from master server
                long endTime = System.currentTimeMillis();
                long elapsedTimeMillis = endTime - startTime;
                System.out.println("Runtime: " + elapsedTimeMillis + " milliseconds");
                System.out.println("Enter the start and end points for the prime search (e.g., \"1 100\"): ");
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host 'localhost'");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to 'localhost'");
            System.exit(1);
        }
        scanner.close();
    }
}
