package view;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.Client;

/**
 * 警告弹窗，关闭登陆界面时弹出，提示用户关闭此界面将结束整个群聊程序】】6
 */
@SuppressWarnings("serial")
public class WarningView extends JFrame {
    private static final String WARNING_TEXT = "关闭此窗口将结束整个群聊程序，是否关闭？";
    private static final String OK_BUTTON_TEXT = "确定";
    private static final String CANCEL_BUTTON_TEXT = "取消";

    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HIGH = 200;
    private static final int TEXT_LABEL_WIDTH = 600;
    private static final int TEXT_LABEL_HIGH = 50;
    private static final int BUTTON_WIDTH = 170;
    private static final int BUTTON_HIGH = 50;

    private static final int END_ID = -1; // 结束群聊

    private JLabel warningLabel;
    private JButton okButton;
    private JButton cancelButton;
    private Thread endThread;

    /**
     * 实例化一个警告窗口，尝试结束群聊（关闭登陆界面）时会弹出
     */
    public WarningView() {
        setSize(FRAME_WIDTH, FRAME_HIGH);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 只关闭当前窗口
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(1, 20, 20));

        // 显示提示信息
        warningLabel = new JLabel(WARNING_TEXT);
        warningLabel.setFont(new java.awt.Font("宋体", 1, 24));
        JPanel warningPanel = new JPanel(new FlowLayout());
        warningPanel.setPreferredSize(new Dimension(TEXT_LABEL_WIDTH, TEXT_LABEL_HIGH));
        warningPanel.add(warningLabel);

        // 初始化确认按钮
        okButton = new JButton(OK_BUTTON_TEXT);
        okButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HIGH));
        okButton.setFont(new java.awt.Font("宋体", 1, 20));
        okButton.addActionListener(e -> {
            endThread = new Thread(() -> {
                Client client = new Client();
                client.applyId(END_ID); // 告诉服务终端群聊已结束
            });
            endThread.start();
            System.exit(0);
        });

        // 初始化取消按钮
        cancelButton = new JButton(CANCEL_BUTTON_TEXT);
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HIGH));
        cancelButton.setFont(new java.awt.Font("宋体", 1, 20));
        cancelButton.addActionListener(e -> {
            dispose();
        });

        add(warningPanel);
        add(okButton);
        add(cancelButton);
        setVisible(true);
    }

}


