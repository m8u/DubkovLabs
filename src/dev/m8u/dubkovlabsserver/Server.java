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

        int port = 3368;

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
            byte[] requestBuffer = new byte[128];
            DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length);
            socket.receive(request);

            JSONObject requestData = new JSONObject(new String(requestBuffer, 0, request.getLength()));
            
            if (requestData.has("parameter")) {
                switch (requestData.getString("parameter")) {
                    case "N1":
                        henhouse.N1 = requestData.getInt("value");
                        break;
                    case "N2":
                        henhouse.N2 = requestData.getInt("value");
                        break;
                    case "K":
                        henhouse.K = requestData.getInt("value") / 100.0f;
                        break;
                    case "P":
                        henhouse.P = requestData.getInt("value") / 100.0f;
                        break;
                    case "birdsLifespan":
                        henhouse.birdsLifespan = requestData.getInt("value");
                        break;
                }
            } else if (requestData.has("action")) {
                switch (requestData.getString("action")) {
                    case "togglePause":
                        henhouse.togglePause(true);
                        break;
                    case "save":
                        henhouse.save();
                        break;
                    case "load":
                        henhouse.load();
                        break;
                }
            } else {
                String data = henhouse.getJSON().toString();
                byte[] responseBuffer = data.getBytes();

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddress, clientPort);
                socket.send(response);
            }
        }
    }
}
