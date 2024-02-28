import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PrimeChecker {
    private List<Integer> primes = Collections.synchronizedList(new ArrayList<>());
  
    private int lowerBound;
    private int upperBound;
    private int numThreads;
    
    public PrimeChecker(int lowerBound, int upperBound, int numThreads){

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.numThreads = numThreads;
    }
    private List<int[]> partitionRange() {
        List<int[]> ranges = new ArrayList<>();
  
        int totalNumbers = upperBound - lowerBound + 1;
        int chunkSize = totalNumbers / numThreads;
        int remaining = totalNumbers % numThreads;
        
        int start = lowerBound;
        for (int i = 0; i < numThreads; i++) {
            int end = start + chunkSize - 1 + (i < remaining ? 1 : 0); // Distribute the remainder
            if (end > upperBound) end = upperBound;
            ranges.add(new int[]{start, end});
            start = end + 1;
        }
    
        return ranges;
    }

    private void findAndAddPrimes(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                synchronized (this.primes) {
                    this.primes.add(i);
                }
            }
        }
    }

    private boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public void startFindingPrimes() {
        List<int[]> ranges = partitionRange();
        
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int start = ranges.get(i)[0];
            final int end = ranges.get(i)[1];
            threads[i] = new Thread(() -> findAndAddPrimes(start, end));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted: " + e.getMessage());
            }
        }
    }

    public List<Integer> getPrimes() {
        return primes;
    }


}
