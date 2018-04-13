package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ChatWindow extends JFrame {
    private final int PORT = 8189;
    private final String SERVER_IP = "localhost";
    private final String TITLE = "MyChat";
    private final String BUTTON_SEND = "Send";
    private final String BUTTON_LOGIN = "Login";
    private final String COMMAND_END = "/end";
    private final String COMMAND_AUTH_OK = "/authok";
    private final String COMMAND_AUTH = "/auth";
    private final String COMMAND_USERLIST = "/userslist";

    private JTextArea jta;
    private JTextArea jtaUsersOnline;
    private JScrollPane jspUsersOnline;
    private JTextField jtf;
    private JTextField jtfLogin;
    private JPasswordField jtfPassword;
    private JPanel bottom, top;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private void setAuthorized(boolean authorized) {
        top.setVisible(!authorized);
        bottom.setVisible(authorized);
        jspUsersOnline.setVisible(authorized);
    }

    ChatWindow() {
        setTitle(TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(400, 400));
        setLocationRelativeTo(null);
        jta = new JTextArea();
        jtaUsersOnline = new JTextArea();
        jtaUsersOnline.setEditable(false);
        jtaUsersOnline.setPreferredSize(new Dimension(150, 1));
        jspUsersOnline = new JScrollPane(jtaUsersOnline);
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        jtf = new JTextField();
        jtf.setPreferredSize(new Dimension(200, 20));
        bottom = new JPanel();
        JButton jbSend = new JButton(BUTTON_SEND);
        bottom.add(jtf, BorderLayout.CENTER);
        bottom.add(jbSend, BorderLayout.EAST);
        jtfLogin = new JTextField();
        jtfPassword = new JPasswordField();
        JButton jbAuth = new JButton(BUTTON_LOGIN);
        top = new JPanel(new GridLayout(1, 3));
        top.add(jtfLogin);
        top.add(jtfPassword);
        top.add(jbAuth);

        add(jspUsersOnline, BorderLayout.EAST);
        add(jsp, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);


        jtf.addActionListener(e -> sendMsg());

        jbSend.addActionListener(e -> sendMsg());

        jbAuth.addActionListener(e -> auth());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    out.writeUTF(COMMAND_END);
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    setAuthorized(false);
                }
            }
        });
        start();
        setAuthorized(false);
        setVisible(true);

    }

    private void start() {
        try {
            socket = new Socket(SERVER_IP, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread1 = new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(COMMAND_AUTH_OK)) {
                        setAuthorized(true);
                        break;
                    }
                    jta.append(msg + "\n");
                    jta.setCaretPosition(jta.getDocument().getLength());
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        if (msg.startsWith(COMMAND_USERLIST)) {
                            String[] users = msg.split(" ");
                            jtaUsersOnline.setText("");
                            for (int i = 1; i < users.length; i++) {
                                jtaUsersOnline.append(users[i] + "\n");
                            }
                        }
                    } else {
                        jta.append(msg + "\n");
                        jta.setCaretPosition(jta.getDocument().getLength());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                setAuthorized(false);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
    }

    private void auth() {
        if (socket == null || socket.isClosed()) start();
        try {
            out.writeUTF(COMMAND_AUTH + " " + jtfLogin.getText() + " " + jtfPassword.getText());
            jtfLogin.setText("");
            jtfPassword.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg() {
        String msg = jtf.getText();
        jtf.setText("");
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
