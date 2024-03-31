import java.io.*;
import java.net.*;
import java.util.Date;

public class UDPPingerClient {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;

        try {
            // Создаем UDP-сокет
            clientSocket = new DatagramSocket();

            // Устанавливаем таймаут в 1 секунду
            clientSocket.setSoTimeout(1000);

            // IP-адрес и порт сервера
            InetAddress serverAddress = InetAddress.getByName("192.168.0.105");
            int port = 12000;

            // Количество пингов
            int N = 10;

            for (int i = 0; i < N; i++) {
                // Создаем сообщение для отправки на сервер
                String message = "Ping " + (i + 1) + " " + new Date().toString();
                byte[] sendData = message.getBytes();

                // Отправляем сообщение на сервер
                long sendTime = System.currentTimeMillis();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                clientSocket.send(sendPacket);

                // Получаем ответ от сервера
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);

                    // Получаем время приема ответа и выводим результат
                    long receiveTime = System.currentTimeMillis();
                    long rtt = receiveTime - sendTime;
                    System.out.println((i + 1) + ". " + "Message: " + new String(receivePacket.getData(), 0, receivePacket.getLength()) +
                            " RTT, milliseconds: " + rtt);
                } catch (SocketTimeoutException e) {
                    System.out.println((i + 1) + ". Request timed out");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }
}
