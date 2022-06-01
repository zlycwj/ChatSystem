package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现了一个简单的群聊服务终端
 */

public class TerminalServer {
    private static final int TERMINAL_PORT = 8888; // 服务端的端口号
    private static final String CHARSET = "UTF-8"; // 传输数据的编码方式

    private UserSupervision userManager; // 群聊用户的管理者
    private ServerSocket terminalServerSocket;
    //创建线程池
    private ExecutorService receiveMessagePool;

    /**
     * 实例化一个服务终端对象
     */
    public TerminalServer() {
        userManager = new UserSupervision();
        receiveMessagePool = Executors.newCachedThreadPool();
        try {
            //启动服务器
            terminalServerSocket = new ServerSocket(TERMINAL_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用此方法可用使服务终端开始工作
     */
    public void startRunning() {
        while (true) {
            try {
                // 开始接收用户端建里连接的请求，打开输出流、输入流
                Socket socket = terminalServerSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                // 刚建立连接时会接收到4个字节的特殊消息，用于辅助服务端做出后续动作
                byte[] initialByte = new byte[4];
                inputStream.read(initialByte);
                int initialMessage = UserSupervision.toInt(initialByte);

                // 群聊结束，服务终端退出
                if (initialMessage == -1) {
                    userManager.end();
                }

                // 用户申请的账号
                Integer uesrId = 0;
                /**
                 * 1、创建成功
                 * 2、格式错误
                 * 3、已存在
                 * 4、未知错误
                 */
                // 用于表示账号是否可用
                boolean isAvailableId = false;
                if (userManager.isFull()) {
                    int result = 0; // 发送给客户端的数据，0 代表群聊已满
                    byte[] byteResult = UserSupervision.toByte(result);

                    outputStream.write(byteResult);
                    outputStream.flush();
                } else {
                    if (initialMessage == 0) { // initialMessage等于0表示用户正在申请一个可用账号
                        //创建新账号
                        uesrId = userManager.getFreeId();
                        int result = 3; // 3 代表未知的错误
                        byte[] byteResult = new byte[4];

                        if ((-1 != uesrId) && userManager.addUser(uesrId, socket, outputStream)) {
                            isAvailableId = true;
                            result = uesrId;
                        }

                        //返回创建账号的结果
                        byteResult = UserSupervision.toByte(result);
                        outputStream.write(byteResult);
                        outputStream.flush();
                    } else if (!userManager.isRightId(initialMessage)) {
                        int result = 1; // 1 代表账号格式错误
                        byte[] byteResult = UserSupervision.toByte(result);

                        outputStream.write(byteResult);
                        outputStream.flush();
                    } else if (!userManager.isFreeId(initialMessage)) {
                        int result = 2; // 2 代表账号已存在
                        byte[] byteResult = UserSupervision.toByte(result);

                        outputStream.write(byteResult);
                        outputStream.flush();
                    } else {
                        uesrId = initialMessage;
                        int result = 3; // 3 代表未知的错误
                        byte[] byteResult = new byte[4];

                        if (userManager.addUser(uesrId, socket, outputStream)) {
                            isAvailableId = true; // 客户端申请的账号可用
                            result = uesrId;
                        }

                        byteResult = UserSupervision.toByte(result);
                        outputStream.write(byteResult);
                        outputStream.flush();
                    }
                }

                if (isAvailableId) {
                    final Integer userFinalId = uesrId;
                    receiveMessagePool.submit(() -> {
                        // 建立连接之后就一直接收对方的消息，直至对方退出
                        while (!socket.isClosed()) {
                            try {
                                // 前四位用于记录消息总长度，或断开通讯的通知（-1表示断开连接）
                                byte[] byteHead = new byte[4];
                                inputStream.read(byteHead);
                                int head = UserSupervision.toInt(byteHead);

                                // 结束与该用户的通讯
                                if (head == -1) {
                                    userManager.removeUser(userFinalId);
                                    inputStream.close();
                                    socket.close();
                                    break;
                                } else {
                                    // 读取对方发送的消息
                                    byte[] messageByte = new byte[head];
                                    inputStream.read(messageByte);
                                    String message = new String(messageByte, CHARSET);

                                    userManager.sendMessage(userFinalId, message); // 将消息发送给其它用户
                                }
                            } catch (SocketException e) {
                                System.out.println("一位用户已退出");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

