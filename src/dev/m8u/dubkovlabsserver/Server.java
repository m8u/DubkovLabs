package dev.m8u.dubkovlabsserver;



import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class Server {
    private final DatagramSocket socket;

    static Henhouse henhouse;

    public Server(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public static void main(String[] args) {

        int port = 3369;

        try {
            Server server = new Server(port);
            new Thread(() -> {
                try {
                    server.service();
                } catch (IOException e) {
                    System.out.println("I/O error: " + e.getMessage());
                }
            }).start();
        } catch (SocketException e) {
            System.out.println("Socket error: " + e.getMessage());
        }

        henhouse = new Henhouse();
    }

    private void service() throws IOException {
        while (true) {
            DatagramPacket request = new DatagramPacket(new byte[1], 1);
            socket.receive(request);

            String data = henhouse.getJSON().toString();
            byte[] buffer = data.getBytes();

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }

}
