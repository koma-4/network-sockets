import java.io.*;
import java.net.*;

public class UDPPingerServer {
    public static void main(String[] args) {
        DatagramSocket serverSocket = null;

        try {
            // Создаем UDP-сокет
            serverSocket = new DatagramSocket(12000);

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Получаем пакеты от клиента
                serverSocket.receive(receivePacket);

                // Получаем данные и адрес клиента
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress address = receivePacket.getAddress();
                int port = receivePacket.getPort();

                // Делаем символы клиентского сообщения заглавными
                message = message.toUpperCase();

                // Генерируем случайное число от 0 до 10
                int rand = (int) (Math.random() * 10);

                // Если rand меньше 4, считаем пакет потерянным и не выдаем ответ
                if (rand < 4) {
                    continue;
                }

                // Отправляем ответ клиенту
                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}