import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

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
            int selectedButton = -1;
            if (button1.isSelected()) {
                selectedButton = 1;
            } else if (button2.isSelected()) {
                selectedButton = 2;
            } else if (button3.isSelected()) {
                selectedButton = 3;
            } else if (button4.isSelected()) {
                selectedButton = 4;
            }
            contentToShow = String.valueOf(selectedButton);
            try {
                clientNet(String.valueOf(selectedButton));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            contentFrame(selectedButton, contentToShow);
        }
    }

    public void contentFrame(int numer, String content){
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
        SocketChannel channel = null;
        String server = "localhost"; // adres hosta serwera
        int port = 12345; // numer portu

        try {
            // Utworzenie kanału
            channel = SocketChannel.open();

            // Ustalenie trybu nieblokującego
            channel.configureBlocking(false);

            // połączenie kanału
            channel.connect(new InetSocketAddress(server, port));

            System.out.print("Klient: łączę się z serwerem ...");

            while (!channel.finishConnect()) {
                // ew. pokazywanie czasu łączenia (np. pasek postępu)
                // lub wykonywanie jakichś innych (krótkotrwałych) działań
            }

        } catch(UnknownHostException exc) {
            System.err.println("Uknown host " + server);
            // ...
        } catch(Exception exc) {
            exc.printStackTrace();
            // ...
        }

        System.out.println("\nKlient: jestem połączony z serwerem ...");

        Charset charset  = Charset.forName("ISO-8859-2");
        Scanner scanner = new Scanner(System.in);

        // Alokowanie bufora bajtowego
        // allocateDirect pozwala na wykorzystanie mechanizmów sprzętowych
        // do przyspieszenia operacji we/wy
        // Uwaga: taki bufor powinien być alokowany jednokrotnie
        // i wielokrotnie wykorzystywany w operacjach we/wy
        int rozmiar_bufora = 1024;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(rozmiar_bufora);
        CharBuffer cbuf = null;


        System.out.println("Klient: wysyłam - Hi");
        // "Powitanie" do serwera
        channel.write(charset.encode("Hi\n"));

        // pętla czytania
        while (true) {

            //cbuf = CharBuffer.wrap("coś" + "\n");

            inBuf.clear();	// opróżnienie bufora wejściowego
            int readBytes = channel.read(inBuf); // czytanie nieblokujące
            // natychmiast zwraca liczbę
            // przeczytanych bajtów

            // System.out.println("readBytes =  " + readBytes);

            if (readBytes == 0) {                              // jeszcze nie ma danych
                //System.out.println("zero bajtów");

                // jakieś (krótkotrwałe) działania np. info o upływającym czasie

                continue;

            }
            else if (readBytes == -1) { // kanał zamknięty po stronie serwera
                // dalsze czytanie niemożlwe
                // ...
                break;
            }
            else {		// dane dostępne w buforze
                //System.out.println("coś jest od serwera");

                inBuf.flip();	// przestawienie bufora

                // pobranie danych z bufora
                // ew. decyzje o tym czy mamy komplet danych - wtedy break
                // czy też mamy jeszcze coś do odebrania z serwera - kontynuacja
                cbuf = charset.decode(inBuf);

                String odSerwera = cbuf.toString();

                System.out.println("Klient: serwer właśnie odpisał ... " + odSerwera);
                contentToShow = odSerwera;
                cbuf.clear();

                if (odSerwera.equals("Bye")) break;
            }

            // Teraz klient pisze do serwera poprzez Scanner
            String input = scanner.nextLine();
            cbuf = CharBuffer.wrap(input + "\n");
            ByteBuffer outBuf = charset.encode(cbuf);
            channel.write(outBuf);

            System.out.println("Klient: piszę " + input);
        }

        scanner.close();
    }


    public static void main(String[] args) {

        new MyApplication();
    }
}
