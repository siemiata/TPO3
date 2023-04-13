import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class MainServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        new MainServer();
    }

    MainServer () throws IOException {

        String host = "localhost";
        int port = 12345;
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.socket().bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Serwer: czekam ... ");

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while(iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isAcceptable()) {

                    System.out.println("Serwer: ktoś się połączył ..., akceptuję go ... ");
                    SocketChannel cc = serverChannel.accept();
                    cc.configureBlocking(false);
                    cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    continue;
                }

                if (key.isReadable()) {

                    SocketChannel cc = (SocketChannel) key.channel();
                    serviceRequest(cc);
                    continue;
                }
                if (key.isWritable()) {
                    continue;
                }
            }
        }
    }

    private static Charset charset  = Charset.forName("ISO-8859-2");
    private static final int BSIZE = 1024;
    private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);
    private StringBuffer reqString = new StringBuffer();


    private void serviceRequest(SocketChannel sc) {
        if (!sc.isOpen()) return;

        System.out.print("Serwer: czytam komunikat od klienta ... ");
        reqString.setLength(0);
        bbuf.clear();

        try {
            readLoop:
            while (true) {
                int n = sc.read(bbuf);
                if (n > 0) {
                    bbuf.flip();
                    CharBuffer cbuf = charset.decode(bbuf);
                    while(cbuf.hasRemaining()) {
                        char c = cbuf.get();
                        //System.out.println(c);
                        if (c == '\r' || c == '\n') break readLoop;
                        else {
                            //System.out.println(c);
                            reqString.append(c);
                        }
                    }
                }
            }

            String cmd = reqString.toString();
            System.out.println(reqString);

            if (cmd.equals("Hi")) {
                sc.write(charset.encode(CharBuffer.wrap("Hi")));
            }
            else if (cmd.equals("Bye")) {

                sc.write(charset.encode(CharBuffer.wrap("Bye")));
                System.out.println("Serwer: mówię \"Bye\" do klienta ...\n\n");
                sc.close();
                sc.socket().close();

            } else
                sc.write(charset.encode(CharBuffer.wrap(reqString)));

        } catch (Exception exc) {
            exc.printStackTrace();
            try { sc.close();
                sc.socket().close();
            } catch (Exception e) {}
        }

    }

}
