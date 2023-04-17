import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class MyApplication extends JFrame implements ActionListener {

    private JToggleButton button1;
    private JToggleButton button2;
    private JToggleButton button3;
    private JToggleButton button4;
    private JButton showButton;
    private String contentToShow = "";

    public MyApplication() {
        super("Moja aplikacja");

        button1 = new JToggleButton("Przycisk 1");
        button2 = new JToggleButton("Przycisk 2");
        button3 = new JToggleButton("Przycisk 3");
        button4 = new JToggleButton("Przycisk 4");
        showButton = new JButton("Pokaż numer");

        showButton.addActionListener(this);

        JPanel panel = new JPanel();
        panel.add(button1);
        panel.add(button2);
        panel.add(button3);
        panel.add(button4);
        panel.add(showButton);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 200);
        this.add(panel);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showButton) {
            int selectedButton = 0;
            if (button1.isSelected()) {
                selectedButton = 1;
            } else if (button2.isSelected()) {
                selectedButton = 2;
            } else if (button3.isSelected()) {
                selectedButton = 3;
            } else if (button4.isSelected()) {
                selectedButton = 4;
            }

            try {
                clientNet(String.valueOf(selectedButton));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            contentFrame(contentToShow);


        }
    }

    public void contentFrame(String content){
        JFrame frame = new JFrame("Najnowsze wiadomosci");
        JTextField textField = new JTextField(20);
        textField.setText("Numer wciśniętego okna to: " + content);
        JButton closeButton = new JButton("Zamknij");

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        JPanel panel = new JPanel();
        panel.add(textField);
        panel.add(closeButton);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public void clientNet(String contentNumber) throws IOException {
        final int BUFFER_SIZE = 1024;
        InetSocketAddress address = new InetSocketAddress("localhost", 8080);
        SocketChannel clientChannel = SocketChannel.open(address);
        clientChannel.configureBlocking(false);

        Selector selector = Selector.open();
        clientChannel.register(selector, SelectionKey.OP_WRITE);

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.put(contentNumber.getBytes());
        buffer.flip();

        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.write(buffer);
                    key.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer clientBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    client.read(clientBuffer);
                    clientBuffer.flip();
                    String response = new String(clientBuffer.array());
                    System.out.println("Received: " + response);
                    contentToShow = response;
                    client.close();
                    selector.close();
                    return;
                }
            }
        }
    }
    public static void main(String[] args) {

        new MyApplication();
    }
}
