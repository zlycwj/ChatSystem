package view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import server.TerminalServer;

/**
 * 服务终端界面，只包含一个结束按钮，用于结束终端服务器
 */

@SuppressWarnings("serial")
public class TerminalServerView extends JFrame {
    private static final String TITLE = "服务终端";
    private static final String END_BUTTON_TEXT = "结束服务终端";

    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HIGH = 150;
    private static final int BUTTON_WIDTH = FRAME_WIDTH - 100;
    private static final int BUTTON_HIGH = FRAME_HIGH - 100;

    private JButton endButton;
    private TerminalServer terminalServer;
    private Thread terminalThread;

    /**
     * 实例化了一个服务终端界面
     */
    public TerminalServerView() {
        // 开启服务终端
        terminalServer = new TerminalServer();
        terminalThread = new Thread(() -> {
            terminalServer.startRunning();
        });
        terminalThread.start();

        // 初始化界面
        setTitle(TITLE);
        setSize(FRAME_WIDTH, FRAME_HIGH);
        setResizable(false);
        setLayout(new FlowLayout(1, 30, 30));

        // 为右上角的叉号添加监听器和动作
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                terminalServer = null;
                System.exit(0);
            }
        });

        // 初始化结束按钮
        endButton = new JButton(END_BUTTON_TEXT);
        endButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HIGH));
        endButton.setFont(new java.awt.Font("宋体", 1, 20));
        endButton.addActionListener(e -> {
            terminalServer = null;
            System.exit(0);
        });

        add(endButton);
        setVisible(true);
    }

    public static void main(String[] args) {
        // 启动终端界面
        new TerminalServerView();
    }

}


