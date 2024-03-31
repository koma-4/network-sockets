import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;


public class Pinger {
    public Pinger(String server, int iterations) {
        this.server = server;
        this.iterations = iterations;
    }

    final private String server;
    final private int iterations;

    public void ping() throws IOException {
        var address = InetAddress.getByName(server);
        var rtts = new ArrayList<Long>();

        int passedIterations = 0;
        while (passedIterations != iterations) {
            passedIterations++;
            var startTime = Instant.now().toEpochMilli();
            var isReachable = address.isReachable(1000);
            var endTime = Instant.now().toEpochMilli();

            if (isReachable) {
                var duration = endTime - startTime;
                rtts.add(duration);
                System.out.println("Reply from " + address.getHostAddress() + ", time = " + duration + "ms");
            } else {
                System.out.println("Request timed out");
            }
        }

        var failures = iterations - rtts.size();
        var received = iterations - failures;
        var lossPerc = ((double) failures / iterations) * 100;
        var sumMs = rtts.stream().mapToLong(Long::longValue).sum();

        System.out.println("Ping statistics for " + server + " [" + address.getHostAddress() + "]");
        System.out.println("Packets: Sent = " + iterations + ", Received = " + received + ", Lost = " + failures + ", " + lossPerc + "% loss");
        System.out.println("Approximate round trip times in ms");
        var minMs = rtts.isEmpty() ? 0 : Collections.min(rtts);
        var maxMs = rtts.isEmpty() ? 0 : Collections.max(rtts);
        var averageMs = rtts.isEmpty() ? 0 : sumMs / rtts.size();
        System.out.println("Minimum = " + minMs + "ms, Maximum = " + maxMs + "ms, Average = " + averageMs + "ms");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Pinger <server> <iterations>");
            return;
        }

        String server = args[0];
        int iterations = Integer.parseInt(args[1]);

        try {
            new Pinger(server, iterations).ping();
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}
