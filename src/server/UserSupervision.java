package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.*;

/**
 * 用于存储已注册用户的信息，同时提供向已注册用户发送消息、增加用户、删除用户以及其它一些辅助功能
 *
 * @author JuJunjian
 * @version 1.0 2020-11-20
 */
public class UserSupervision {
    private static final String CHARSET = "UTF-8"; // 传输数据的编码方式

    private static final int SEND_THREADS_NUMBER = 10;
    private static final int MAX_SIZE = 1000; // 最大聊天人数
    private static final Integer MIN_ID = 9000;
    private static final Integer MAX_ID = 9999;

    private  Map<Integer, OutputStream> userOutputStreamMap;
    private  Map<Integer, Socket> userSocketMap;
    private  ExecutorService sendMessagePool;
    private  int size; // 用于记录当前用户的数量

    private File file = new File("Content.txt");


    /**
     * 实例化一个管理群聊用户的对象
     */
    public UserSupervision() {
        userOutputStreamMap = new HashMap<>();
        userSocketMap = new HashMap<>();
        sendMessagePool = Executors.newFixedThreadPool(SEND_THREADS_NUMBER);
        size = 0;
    }

    /**
     * 检查注册账号是否满足格式要求
     *
     * @param id 需检测的账号
     * @return 账号符合要求返回true；否则返回false
     */
    public boolean isRightId(Integer id) {
        if (id >= MIN_ID && id <= MAX_ID) {
            return true;
        }
        return false;
    }

    /**
     * 检测群聊人数是否已满
     *
     * @return 聊人数是否已满返回true；否则返回false
     */
    public boolean isFull() {

        return (MAX_SIZE == size);
    }

    /**
     * 检测用户申请的账号是否已经被占用
     * @param id 需检测的账号
     * @return 未被占用返回true；否则返回false
     * 使用synchronized同步锁，修饰方法，当一个线程调用isFreeId时，其他线程排队等候
     */
    public synchronized boolean isFreeId(Integer id) {
        if (userOutputStreamMap.containsKey(id)) {
            return false;
        }
        return true;
    }

    /**
     * 向群聊中添加新用户
     *
     * @param id 新注册用户的账号
     * @param socket 用于和此用户通讯的套接字
     * @param outputStream 用于向此用户发送消息的输出流
     * @return 添加成功返回true；否则返回false
     */
    public synchronized boolean addUser(Integer id, Socket socket , OutputStream outputStream) {
        if (isFull()) {
            return false;
        } else {
            // 将此用户添加到群聊中
            userSocketMap.put(id, socket);
            userOutputStreamMap.put(id, outputStream);

            size++; // 当前群聊人数加一
            return true;
        }
    }

    /**
     * 从群聊中移除指定的用户
     *
     * @param deletedUserId 需被移除的用户的账号
     * @return 删除成功返回true；否则返回false
     */
    public synchronized boolean removeUser(Integer deletedUserId) {
        //表内为空，返回错误
        if (userOutputStreamMap.isEmpty()) {
            return false;
        }

        if (userOutputStreamMap.containsKey(deletedUserId)) {
            try {
                // 关闭并移除此用户对应的OutputStream和Socket
                userOutputStreamMap.get(deletedUserId).close();
                userOutputStreamMap.remove(deletedUserId);
                userSocketMap.get(deletedUserId).close();
                userSocketMap.remove(deletedUserId);
                size--; // 当前群聊人数减一

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 向群内的其它用户（除了提供消息的用户）发送消息
     *Integer封装：一个引用指向这个对象
     * @param senderId 发送者的账号
     * @param message 发送的消息
     */
    public synchronized void sendMessage(Integer senderId, String message) {
        //将聊天记录存储到文件中去
        if(!file.exists()){
            file.exists();
        }
        try {
            FileWriter fileWriter = new FileWriter(file.getName(),true);
            fileWriter.write(message);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //在hashmap里找这个id:ForEach迭代键值对方法
        for (Integer id : userOutputStreamMap.keySet()) {
            if(id != senderId) {
                sendMessagePool.submit(() -> {
                    OutputStream out = userOutputStreamMap.get(id);

                    // 将所需发送的消息转换为字节流，编码方式为UTF-8
                    byte[] byteMessage = null;
                    try {
                        //用UTF-8编码，将字符串编码为byte序列，并将结果存储到一个新的byte数组中。
                        byteMessage = message.getBytes(CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    // head是消息字节前面的报头，代表消息的长度，可辅助接收端判断是否接收完毕
                    int length = byteMessage.length;
                    byte[] head = toByte(length);

                    // 得到最终需要传输的字节流outByte：报头+信息
                    byte[] outByte = new byte[length + head.length];
                    System.arraycopy(head, 0, outByte, 0, head.length);
                    System.arraycopy(byteMessage, 0, outByte, head.length, byteMessage.length);

                    // 发送消息
                    try {
                        out.write(outByte);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }



    /**
     * 获得空闲的账号
     *
     * @return 存在可用账号时返回一个可用账号，否则返回-1
     */
    public synchronized Integer getFreeId() {
        Integer freeId = -1;
        if (isFull()) {
            return freeId;
        }

        // 遍历以寻找可用账号，即hashmap里没有key值的key
        for (Integer id = MIN_ID; id < MAX_ID; id++) {
            if (!userOutputStreamMap.containsKey(id)) {
                freeId = id;
                break;
            }
        }

        return freeId;
    }

    /**
     * 调用此方法可结束群聊
     */
    public synchronized void end() {
        userOutputStreamMap.clear();

        // 关闭所有socket
        for (Integer id : userSocketMap.keySet()) {
            try {
                userSocketMap.get(id).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userSocketMap.clear();
    }

    /**
     * 将int类型的数据转换为4位byte类型的数据
     *
     * @param intData 传入的int类型的参数
     * @return 返回转换之后的4位byte类型的数据
     */
    public static synchronized byte[] toByte(int intData) {
        byte[] byteData = new byte[4];

        byteData[0] = (byte) ((intData >> 24) & 0xFF);
        byteData[1] = (byte) ((intData >> 16) & 0xFF);
        byteData[2] = (byte) ((intData >> 8) & 0xFF);
        byteData[3] = (byte) (intData & 0xFF);

        return byteData;
    }

    /**
     * 将4位byte类型的数据转换为int类型的数据
     *
     * @param byteData 传入的4位byte类型的参数
     * @return 返回转换之后的int类型的数据
     */
    public static synchronized int toInt(byte[] byteData) {
        int intData = 0;

        for (int i = 0; i < byteData.length; i++) {
            int shift = (3 - i) * 8;
            intData += ((byteData[i] & 0xFF) << shift);
        }

        return intData;
    }
}


