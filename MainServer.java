import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class MainServer {
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception {

        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress("localhost", 8080));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        Map<SocketChannel, ByteBuffer> dataMap = new HashMap<>();

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    dataMap.put(client, ByteBuffer.allocate(BUFFER_SIZE));
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer clientBuffer = dataMap.get(client);
                    client.read(clientBuffer);
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer clientBuffer = dataMap.get(client);
                    clientBuffer.flip();
                    client.write(clientBuffer);
                    client.close();
                }
            }
        }
    }
}
