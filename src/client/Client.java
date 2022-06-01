package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 实现了客户端的功能
 */
public class Client {
    private static final String LOCALHOST = "127.0.0.1";
    private static final String CHARSET = "UTF-8"; // 编码方式
    private static final int TERMINAL_PORT = 8888; // 服务端的端口号

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * 实例化一个客户端对象
     */
    public Client() {
        try {
            //插一个管子，连接
            socket = new Socket(LOCALHOST, TERMINAL_PORT);
            //获取socket通道的输入流
            inputStream = socket.getInputStream();
            //获取socket通道的输出流
            outputStream = socket.getOutputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务终端申请账号的请求
     *
     * @param id 用户申请的账号，id为0代表申请一个随机的账号
     */
    public void applyId(int id) {
        byte[] byteId = toByte(id);
        try {
            //将申请的账号写进输出流
            outputStream.write(byteId);
            //刷新
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 返回申请账号的结果
     */
    public int getApplyIdResult() {
        int result = -1;
        byte[] byteResult = new byte[4];

        try {
            //获取申请账号结果的输入流
            inputStream.read(byteResult);
            //转化结果
            result = toInt(byteResult);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 调用此方法以发送消息
     *
     * @param message 需发送的消息
     */
    public void sendMessage(String message) {
        try {
            //调整编码方式
            byte[] byteMessage = message.getBytes(CHARSET);
            //记录长度
            int length = byteMessage.length;
            // 字节流前的报头，代表了消息字节流的长度，用于辅助接收端判断是否接收完毕
            byte[] head = toByte(length);

            // 得到最终被传输的字节流：长度+报头
            byte[] outByte = new byte[length + head.length];
            //数组之间的复制
            System.arraycopy(head, 0, outByte, 0, head.length);
            System.arraycopy(byteMessage, 0, outByte, head.length, byteMessage.length);

            outputStream.write(outByte);
            outputStream.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于接收新消息，每次调用只可接收一条新消息
     *
     * @return 返回接收到的信息（字符串类型）
     */
    public String startReceiveMessage() {
        String receiveMessage = "";
        byte[] head = new byte[4]; // 消息的长度

        try {
            //读消息的报头
            inputStream.read(head);
            //存储byte类型的消息
            int messageLength = toInt(head);
            byte[] byteMessage = new byte[messageLength];

            //读消息
            inputStream.read(byteMessage);
            //调整编码（转换）
            receiveMessage = new String(byteMessage, CHARSET);
        } catch (SocketException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

        return receiveMessage;
    }

    /**
     * 调用此方法可用结束与服务终端的通讯
     */
    public void endCommunication() {
        try {
            // -1 表示结束整个群聊
            int result = -1;
            //用byte形式存储结果
            byte[] end = toByte(result);

            //报告出去：结束了
            outputStream.write(end);
            outputStream.flush();
            //关闭socket通道
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将int类型的数据转换为4位byte类型的数据
    private byte[] toByte(int intData) {
        byte[] byteData = new byte[4];
        byteData[0] = (byte) ((intData >> 24) & 0xFF);
        byteData[1] = (byte) ((intData >> 16) & 0xFF);
        byteData[2] = (byte) ((intData >> 8) & 0xFF);
        byteData[3] = (byte) (intData & 0xFF);
        return byteData;
    }

    // 将4位byte类型的数据转换为int类型的数据
    private int toInt(byte[] byteData) {
        int intData = 0;

        for (int i = 0; i < byteData.length; i++) {
            int shift = (3 - i) * 8;
            intData += ((byteData[i] & 0xFF) << shift);
        }

        return intData;
    }

}


