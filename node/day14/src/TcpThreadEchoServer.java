
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TcpThreadEchoServer {
    // 但是在 Java socket 中是体现不出来 "监听" 的含义的~~
    // 之所以这么叫, 其实是 操作系统原生的 API 里有一个操作叫做 listen
    // private ServerSocket listenSocket = null;
    private ServerSocket serverSocket = null;

    public TcpThreadEchoServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("服务器启动!");
        while (true) {
            // 由于 TCP 是有连接的, 不能一上来就读数据, 而要先建立连接. (接电话)
            // accept 就是在 "接电话", 接电话的前提是, 有人给你打了~~, 如果当前没有客户端尝试建立连接, 此处的 accept 就会阻塞.
            // accept 返回了 一个 socket 对象, 称为 clientSocket. 后续和客户端之间的沟通, 都是通过 clientSocket 来完成的.
            // 进一步讲, serverSocket 就干了一件事, 接电话~~
            Socket clientSocket = serverSocket.accept();
            // [改进方法] 在这个地方, 每次 accept 成功, 都创建一个新的线程, 由新线程负责执行这个 processConnection 方法~
            Thread t = new Thread(() -> {
                processConnection(clientSocket);
            });
            t.start();
        }
    }

    private void processConnection(Socket clientSocket) {
        System.out.printf("[%s:%d] 客户端建立连接!\n", clientSocket.getInetAddress().toString(), clientSocket.getPort());
        // 接下来来处理请求和响应
        // 这里的针对 TCP socket 的读写就和文件读写是一模一样的!!
        try (InputStream inputStream = clientSocket.getInputStream()) {
            try (OutputStream outputStream = clientSocket.getOutputStream()) {
                // 循环的处理每个请求, 分别返回响应
                Scanner scanner = new Scanner(inputStream);
                while (true) {
                    // 1. 读取请求
                    if (!scanner.hasNext()) {
                        System.out.printf("[%s:%d] 客户端断开连接!\n", clientSocket.getInetAddress().toString(), clientSocket.getPort());
                        break;
                    }
                    // 此处用 Scanner 更方便. 如果不用 Scanner 就用原生的 InputStream 的 read 也是可以的
                    String request = scanner.next();
                    // 2. 根据请求, 计算响应
                    String response = process(request);
                    // 3. 把这个响应返回给客户端
                    // 为了方便起见, 可以使用 PrintWriter 把 OutputStream 包裹一下
                    PrintWriter printWriter = new PrintWriter(outputStream);
                    printWriter.println(response);
                    // 刷新缓冲区, 如果没有这个刷新, 可能客户端就不能第一时间看到响应结果.
                    printWriter.flush();

                    System.out.printf("[%s:%d] req: %s, resp: %s\n", clientSocket.getInetAddress().toString(),
                            clientSocket.getPort(), request, response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 此处要记得来个关闭操作.
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String process(String request) {
        return request;
    }

    public static void main(String[] args) throws IOException {
        TcpThreadEchoServer server = new TcpThreadEchoServer(9090);
        server.start();
    }
}
