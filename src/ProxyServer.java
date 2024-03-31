import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProxyServer {
    private Socket remoteSocket;
    private final ServerSocket serverSocket;
    private final String server;
    private BufferedReader remoteReader;
    private PrintWriter remoteWriter;
    private volatile boolean isReady = true;
    private final AtomicInteger clients = new AtomicInteger(0);

    public ProxyServer(String server, int remotePort, int port) throws IOException {
        this.server = server;
        serverSocket = new ServerSocket(port);
        reopenRemote(remotePort);
    }

    private void reopenRemote(int remotePort) throws IOException {
        if (remoteSocket != null) remoteSocket.close();
        remoteSocket = new Socket(server, remotePort);
        remoteReader = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream()));
        remoteWriter = new PrintWriter(new OutputStreamWriter(remoteSocket.getOutputStream()), true);
    }

    public void start(boolean stopAfter) throws IOException {
        if (remoteSocket.isClosed()) return;
        isReady = true;

        while (isReady) {
            System.out.println("Waiting for a connection at port: " + serverSocket.getLocalPort());
            Socket client;

            try {
                client = serverSocket.accept();
            } catch (Exception e) {
                break;
            }

            new Thread(() -> {
                clients.incrementAndGet();
                System.out.println("Established a connection with a client: " + client.getInetAddress() + ":" + client.getPort());

                try {
                    handleClient(client);
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                var clientsAfter = clients.decrementAndGet();
                if (clientsAfter == 0 && stopAfter) {
                    isReady = false;
                }
            }).start();
        }
        isReady = false;
    }

    private void handleClient(Socket client) throws IOException {
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter clientWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

        var clientRequest = clientReader.readLine();
        if (clientRequest == null) return;
        clientRequest = clientRequest.replace("HTTP/1.1", "");

        System.out.println("Received request from a client: " + clientRequest);
        var request = clientRequest.split("\\s+");
        var method = request[0];
        if (!method.equalsIgnoreCase("GET")) {
            clientWriter.println("HTTP/1.1 501\n\n");
            return;
        }
        var requestedFile = request[1];
        var systemFile = requestedFile.replace("http://", "").replaceAll("/", "_");

        System.out.println("Received request from a client: " + clientRequest);
        var file = new File(Path.of("").toAbsolutePath().toString(), systemFile);

        if (file.isFile()) {
            System.out.println("File is found in cache: " + file);
            var cachedContent = buildResponse(Files.readString(file.toPath()));
            clientWriter.println(cachedContent);
            System.out.println("Cached content: ");
            System.out.println(cachedContent);
        } else {
            System.out.println("File does NOT found in cache: " + file);
            System.out.println("Requesting file from server: " + server + ":" + remoteSocket.getPort());

            remoteWriter.println(clientRequest);
            var remoteResponse = remoteReader.lines().collect(Collectors.joining("\n"));
            System.out.println("Remote response: ");
            System.out.println(remoteResponse);

            var content = remoteResponse.substring(remoteResponse.indexOf("\n\n"));
            Files.write(file.toPath(), content.getBytes());
            System.out.println("File is saved in cache: " + file);
            clientWriter.println(buildResponse(content));
        }
    }

    private String buildResponse(String responseContent) {
        return "HTTP/1.1 200 OK\n\n" + responseContent;
    }

    public void close() throws IOException {
        System.out.println("Server is closed");
        isReady = false;
        try {
            if (!remoteSocket.isClosed()) remoteSocket.close();
        } finally {
            if (!serverSocket.isClosed()) serverSocket.close();
        }
    }

    public boolean isReady() {
        return isReady;
    }

    public static void main(String[] args) {
        ProxyServer proxyServer = null;
        try {
            proxyServer = new ProxyServer("google.com", 80, 44235);
            proxyServer.start(false);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (proxyServer != null) {
                try {
                    proxyServer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}