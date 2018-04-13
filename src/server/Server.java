package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private final int PORT = 8189;
    private Vector<ClientHandler> clients;
    private AuthService authService;

    AuthService getAuthService() {
        return authService;
    }

    Server() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        clients = new Vector<>();
        try {
            serverSocket = new ServerSocket(PORT);
            authService = new BaseAuthService();
            authService.start();
            System.out.println("Server launch, waiting for clients");
            while (true) {
                socket = serverSocket.accept();
                clients.add(new ClientHandler(this, socket));
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            authService.stop();
        }
    }

    boolean isNickBusy(String nick) {
        System.out.println(nick);
        for (ClientHandler c : clients) {
            if (c.getName().equals(nick)) return true;
        }
        return false;
    }

    void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    void broadcastUsersList() {
        StringBuilder sb = new StringBuilder("/userslist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getName());
        }
        for (ClientHandler c : clients) {
            c.sendMsg(sb.toString());
        }
    }

    void unSubscribeMe(ClientHandler c) {
        clients.remove(c);
    }

    void sendPrivateMsg(ClientHandler from, String to, String msg) {
        for (ClientHandler c : clients) {
            if (c.getName().equals(to)) {
                c.sendMsg("from " + from.getName() + ": " + msg);
                from.sendMsg("to " + to + " msg: " + msg);
                break;
            }
        }
    }
}
