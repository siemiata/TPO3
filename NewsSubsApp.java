import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NewsSubsApp {
    private JFrame mainFrame;
    private JPanel buttonPanel;
    private JButton openWindowButton;
    private JLabel messageLabel;


    public NewsSubsApp() {
        prepareGUI();
        addComponents();
    }

    private void prepareGUI() {
        mainFrame = new JFrame("Gazeta PJATKA");
        mainFrame.setSize(450, 100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setLocationRelativeTo(null);
    }

    private void addComponents() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JToggleButton button1 = new JToggleButton("Celebryci");
        JToggleButton button2 = new JToggleButton("Gotowanie");
        JToggleButton button3 = new JToggleButton("Polityka");
        JToggleButton button4 = new JToggleButton("Sport");

        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);
        buttonPanel.add(button4);

        openWindowButton = new JButton("Wyświetl treści");
        openWindowButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openWindow();
                if(button1.isSelected())
                System.out.println("Celebryci");

                if(button2.isSelected())
                    System.out.println("Gotowanie");

                if(button3.isSelected())
                    System.out.println("Polityka");

                if(button4.isSelected())
                    System.out.println("Sport");
            }
        });
        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(openWindowButton, BorderLayout.SOUTH);
    }

    private void openWindow() {
        JFrame window = new JFrame("New Window");
        window.setSize(200, 200);
        window.setLocationRelativeTo(mainFrame);
        System.out.println();

        messageLabel = new JLabel("Witam", SwingConstants.CENTER);
        window.add(messageLabel, BorderLayout.CENTER);
        window.setVisible(true);
    }


    public void show() {
        mainFrame.setVisible(true);
    }

    public void Client() throws IOException {
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
    public static void main(String[] args) {
        NewsSubsApp app = new NewsSubsApp();
        try {
            app.Client();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        app.show();

    }
}
