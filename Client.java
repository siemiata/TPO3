import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {

        SocketChannel channel = null;
        String server = "localhost"; // adres hosta serwera
        int port = 12345; // numer portu

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(server, port));

            System.out.print("Klient: łączę się z serwerem ...");

            while (!channel.finishConnect()) {
            }

        } catch(UnknownHostException exc) {
            System.err.println("Uknown host " + server);
        } catch(Exception exc) {
            exc.printStackTrace();
        }

        System.out.println("\nKlient: jestem połączony z serwerem ...");

        Charset charset  = Charset.forName("ISO-8859-2");
        Scanner scanner = new Scanner(System.in);

        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);
        CharBuffer cbuf = null;

        System.out.println("Klient: wysyłam - Hi");
        channel.write(charset.encode("Hi\n"));

        while (true) {
            inBuf.clear();
            int readBytes = channel.read(inBuf);

            if (readBytes == 0) {
                continue;
            }
            else if (readBytes == -1) {
                break;
            }
            else {
                inBuf.flip();
                cbuf = charset.decode(inBuf);
                String odSerwera = cbuf.toString();
                System.out.println("Klient: serwer właśnie odpisał ... " + odSerwera);
                cbuf.clear();

                if (odSerwera.equals("Bye")) break;
            }
            String input = scanner.nextLine();
            cbuf = CharBuffer.wrap(input + "\n");
            ByteBuffer outBuf = charset.encode(cbuf);
            channel.write(outBuf);

            System.out.println("Klient: piszę " + input);
        }
        scanner.close();
    }
}
