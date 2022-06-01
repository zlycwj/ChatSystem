package view;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.*;
import javax.imageio.*;

import java.io.*;

import client.Client;

/**
 * 用户群聊天界面，大致包括：消息显示面板、输入面板、发送按钮
 */
@SuppressWarnings("serial")
public class ChatView extends JFrame {
    private static final String TITLE = "用户：";
    private static final String SEND_BUTTON_TEXT = "发送";

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HIGH = 600;
    private static final int PANEL_WIDTH = FRAME_WIDTH - 50;
    private static final int CHAT_PANEL_HIGH = 500;
    private static final int INPUT_PANEL_HIGH = 50;
    private static final int SEND_BUTTON_WIDTH = 80;
    private static final int SEND_BUTTON_HIGH = 50;

    private JPanel chatPanel;
    private JPanel inputPanel;
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;

    // 用户账号
    private Integer id;
    // 用户对应的客户端对象
    private Client client;
    //对于一个客户来说，有两个流，一个发送信息，一个接受信息
    private Thread receiveThread;
    private Thread sendThread;

    // 用户的状态（false表示用户已退出群聊
    private boolean isRunning;

    File file = new File("Content.txt");

    private Image background;

    /**
     * 实例化一个群聊界面
     *
     * @param client 注册界面申请成功之后得到的客户端对象
     * @param id 用户的账号
     */
    public ChatView(Client client, int id) {
        // 初始客户端
        this.client = client;
        this.id = id;
        isRunning = true;

        // 界面初始化
        try {
            background = ImageIO.read(new File("/Users/zhulinyu/Desktop/picture.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle(TITLE + this.id);
        setSize(FRAME_WIDTH, FRAME_HIGH);
        setResizable(false);
        setLocationRelativeTo(null); // 窗口居中
        setLayout(new FlowLayout(2, 10, 10));


        // 为右上角的叉号添加监听器和动作
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { // 用户退出
                isRunning = false;
                client.endCommunication(); // 结束通讯

                dispose();
            }
        });

        // 消息显示面板
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        initialJTextArea(chatArea);
        chatPanel = new JPanel();
        initialJPanel(chatPanel, PANEL_WIDTH, CHAT_PANEL_HIGH);
        chatPanel.add(new JScrollPane(chatArea));

        // 消息发送面板
        inputArea = new JTextArea();
        initialJTextArea(inputArea);
        inputPanel = new JPanel();
        initialJPanel(inputPanel, PANEL_WIDTH-100, INPUT_PANEL_HIGH);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        // 发送按钮
        sendButton = new JButton(SEND_BUTTON_TEXT);
        sendButton.setPreferredSize(new Dimension(SEND_BUTTON_WIDTH, SEND_BUTTON_HIGH));
        sendButton.setFont(new Font("宋体", 1, 24));
        sendButton.addActionListener(e -> {
            sendMessage("用户" + id + ":\n" + inputArea.getText() + "\n\n");
            chatArea.append("我:\n" + inputArea.getText() + "\n\n");
            inputArea.setText("");
        });

        startReceive(); // 开始接收消息

        add(chatPanel);
        add(inputPanel);
        add(sendButton);
        setVisible(true);
    }

    // 调用此方法发送一次消息
    private void sendMessage(String message) {
        sendThread = new Thread(() -> {
            client.sendMessage(message);
        });

        sendThread.start();
    }

    // 调用此方法开始接收消息
    private void startReceive() {
        receiveThread = new Thread(() -> {
            while (isRunning) {
                String message = client.startReceiveMessage();
                chatArea.append(message);
            }
        });
        receiveThread.start();
    }

    // 设置JTextArea的字体
    private void initialJTextArea(JTextArea textArea) {
        textArea.setFont(new Font("宋体", 0, 24));
    }

    // 设置JPanel的大小和排版
    private void initialJPanel(JPanel panel, int width, int high) {
        panel.setPreferredSize(new Dimension(width, high));
        panel.setLayout(new BorderLayout());
    }

    public void paint(Graphics g){
        g.drawImage(background,0,0,800,600,this);
    }
}


