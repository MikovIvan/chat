package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    String getName() {
        return name;
    }

    ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            name = "undefined";
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/auth")) {
                        String[] elements = msg.split(" ");
                        String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                        System.out.println(nick);
                        if (nick != null) {
                            if (!server.isNickBusy(nick)) {
                                sendMsg("/authok " + nick);
                                this.name = nick;
                                server.broadcastUsersList();
                                server.broadcast(this.name + " enter the chat");
                                break;
                            } else sendMsg("Login is busy");
                        } else sendMsg("Wrong login/password");
                    } else sendMsg("You should authorized first!");
                }
                while (true) {
                    String msg = in.readUTF();
                    System.out.println("client: " + msg);
                    if (msg.startsWith("/")) {
                        if (msg.equalsIgnoreCase("/end")) break;
                        else if (msg.startsWith("/w")) {
                            String nameTo = msg.split(" ")[1];
                            String message = msg.substring(4 + nameTo.length());
                            server.sendPrivateMsg(this, nameTo, message);
                        } else
                            sendMsg("Такой команды нет!");
                    } else {
                        server.broadcast(this.name + " " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.unSubscribeMe(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
