import java.io.*;
import java.net.*;

public class WebServer {
    private static final int PORT = 80;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Готов к обслуживанию... (IP-адрес: " + localhost.getHostAddress() + ")");

            while (true) {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Подключение от " + connectionSocket.getInetAddress());

                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleRequest(connectionSocket);
                    }
                });
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket connectionSocket) {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String requestMessageLine = inFromClient.readLine();
            if (requestMessageLine != null) {
                // Извлекаем имя файла из строки запроса
                String filename = requestMessageLine.split(" ")[1].substring(1);

                try {
                    // Открываем файл и отправляем его содержимое клиенту
                    FileInputStream fileInputStream = new FileInputStream(filename);
                    byte[] fileBytes = new byte[fileInputStream.available()];
                    fileInputStream.read(fileBytes);

                    // Отправляем HTTP-заголовок с кодом 200 OK
                    outToClient.writeBytes("HTTP/1.1 200 OK\r\n\r\n");

                    // Отправляем содержимое файла
                    outToClient.write(fileBytes, 0, fileBytes.length);

                    // Закрываем потоки
                    fileInputStream.close();
                } catch (FileNotFoundException e) {
                    // Если файл не найден, отправляем код 404 Not Found
                    outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n\r\nFile Not Found");
                }
            } else {
                // Если строка запроса пустая или такого файла нет, выводим сообщение об ошибке
                System.err.println("Пустая строка запроса или файл отсутствует");
            }

            // Закрываем потоки и сокет
            outToClient.close();
            inFromClient.close();
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}