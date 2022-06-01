package view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import client.Client;

import java.io.*;

/**
 * 用户登陆界面，大致包括：提示面板、账号输入面板、必须的按钮
 */
@SuppressWarnings("serial")
public class LoginView extends JFrame {
    private static final Integer MIN_ID = 9000;
    private static final Integer MAX_ID = 9999;

    private static final String ERROR_TEXT = "账号有误";
    private static final String ID_EXIST_TEXT = "账号已存在";
    private static final String USER_FULL_TEXT = "群聊人数已满";
    private static final String UNKNOWN_ERROR_TEXT = "未知的错误";

    private static final String TITLE = "登陆界面";
    private static final String TIP_TEXT = "请输入账号(9000-9999):";
    private static final String OK_BUTTON_TEXT = "确定";
    private static final String GET_FREE_ID_TEXT = "新建账号";
    private static final int FRAME_WIDTH = 400;
    private static final int FRAME_HIGH = 240;
    private static final int TEXT_AREA_WIDTH = 350;
    private static final int TEXT_AREA_HIGH = 35;
    private static final int BUTTON_WIDTH = 170;
    private static final int BUTTON_HIGH = 50;

    private JTextArea inputIdTextArea;
    private JLabel errorTip;
    private JButton okButton;
    private JButton getFreeIdButton;

    /**
     * 实例化一个用户登陆界面
     */
    public LoginView() {
        setTitle(TITLE);
        setSize(FRAME_WIDTH, FRAME_HIGH);
        setResizable(false);
        setLayout(new FlowLayout(1, 10, 10));
        setLocation(0, 150);

        // 为右上角的叉号添加监听器和动作
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            //关闭窗口时跳出警示窗口
            @Override
            public void windowClosing(WindowEvent e) {
                new WarningView();
            }
        });

        // 显示提示信息
        JLabel showTip = new JLabel(TIP_TEXT);
        showTip.setFont(new java.awt.Font("宋体", 1, 24));

        // 输入账号的面板
        inputIdTextArea = new JTextArea();
        inputIdTextArea.setPreferredSize(new Dimension(TEXT_AREA_WIDTH, TEXT_AREA_HIGH));
        inputIdTextArea.setFont(new java.awt.Font("宋体", 1, 24));

        // 输入账号有误时，给出的错误提示
        errorTip = new JLabel("");
        errorTip.setFont(new java.awt.Font("宋体", 1, 18));
        JPanel errorTipPanel = new JPanel(new FlowLayout());
        errorTipPanel.setPreferredSize(new Dimension(TEXT_AREA_WIDTH, TEXT_AREA_HIGH - 10));
        errorTipPanel.add(errorTip);

        // 初始化确认按钮
        okButton = new JButton(OK_BUTTON_TEXT);
        okButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HIGH));
        okButton.setFont(new java.awt.Font("宋体", 1, 20));
        okButton.addActionListener(e -> {
            final int uesrId = getInputId(); // 得到用户输入的账号
            if (-1 == uesrId) { // 账号中存在非数字
                errorTip.setText(ERROR_TEXT);
            } else {
                Client client = new Client(); // 每一个群聊用户都对应了一个客户端对象
                client.applyId(uesrId); // 向终端申请申请账号
                int result = client.getApplyIdResult(); // 得到申请账号的结果

                // 根据申请账号的结果进行处理
                switch (result) {
                    case 0:  // 群聊人数已满
                        errorTip.setText(USER_FULL_TEXT);
                        break;

                    case 1: // 账号格式错误
                        errorTip.setText(ERROR_TEXT);
                        break;

                    case 2: // 账号已存在
                        errorTip.setText(ID_EXIST_TEXT);
                        break;

                    case 3: // 未知的错误
                        errorTip.setText(UNKNOWN_ERROR_TEXT);
                        break;

                    default:
                        if (uesrId == result) { // 申请成功
                            inputIdTextArea.setText("");
                            new ChatView(client, uesrId);
                        } else { // 未知的错误
                            errorTip.setText(UNKNOWN_ERROR_TEXT);
                        }
                        break;
                }
            }
        });

        // 初始化随机获得账号按钮
        getFreeIdButton = new JButton(GET_FREE_ID_TEXT);
        getFreeIdButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HIGH));
        getFreeIdButton.setFont(new java.awt.Font("宋体", 1, 20));
        getFreeIdButton.addActionListener(e -> {
            Client client = new Client();
            client.applyId(0); // 申请随机的账号

            int result = client.getApplyIdResult();
            if (0 == result) { // 0 代表群聊人数已满
                errorTip.setText(USER_FULL_TEXT);
            }  else if ((result >= MIN_ID) && (result <= MAX_ID)) { // 申请成功
                inputIdTextArea.setText("");
                new ChatView(client, result);
            } else {
                errorTip.setText("未知的错误");
            }
        });

        add(showTip);
        add(inputIdTextArea);
        add(errorTipPanel);
        add(okButton);
        add(getFreeIdButton);
        setVisible(true);
    }

    // 得到用户输入的Id，读取失败时返回-1
    private Integer getInputId() {
        Integer inputId = -1;
        String stringId = inputIdTextArea.getText();
        try {
            inputId = Integer.valueOf(stringId);
        } catch (Exception e) {

        }
        return inputId;
    }

    public static void main(String[] args) {

        new LoginView();

    }

}


