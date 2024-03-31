import java.io.*;
import java.net.*;

public class SMTPClient {
    public static void main(String[] args) {
        String msg = "\r\n Я люблю компьютерные сети!";
        String endmsg = "\r\n.\r\n";

        // Почтовый сервер и порт AOL
        String mailserver = "smtp.aol.com";
        int mailserverPort = 587;

        try {
            // Создаем сокет clientSocket и устанавливаем TCP-соединение с mailserver
            Socket clientSocket = new Socket(mailserver, mailserverPort);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Получаем ответ от сервера
            String recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("220")) {
                System.out.println("Код 220 от сервера не получен.");
            }

            // Отправляем команду EHLO и выводим ответ сервера
            outToServer.println("EHLO Alice");
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("250")) {
                System.out.println("Код 250 от сервера не получен.");
            }

            // Отправляем команду MAIL FROM и выводим ответ сервера
            outToServer.println("MAIL FROM: <lolka@lolka.lol>");
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("250")) {
                System.out.println("Код 250 от сервера не получен.");
            }

            // Отправляем команду RCPT TO и выводим ответ сервера
            outToServer.println("RCPT TO: <anotherlolka@lolka.lol>");
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("250")) {
                System.out.println("Код 250 от сервера не получен.");
            }

            // Отправляем команду DATA и выводим ответ сервера
            outToServer.println("DATA");
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("354")) {
                System.out.println("Код 354 от сервера не получен.");
            }

            // Отправляем данные сообщения
            outToServer.println(msg);

            // Отправляем завершающую одиночную точку
            outToServer.println(endmsg);
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("250")) {
                System.out.println("Код 250 от сервера не получен.");
            }

            // Отправляем команду QUIT и получаем ответ сервера
            outToServer.println("QUIT");
            recv = inFromServer.readLine();
            System.out.println(recv);
            if (!recv.startsWith("221")) {
                System.out.println("221 reply not received from server.");
            }

            // Закрываем сокет
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}