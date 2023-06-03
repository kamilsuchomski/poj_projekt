import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Prosty klient czatu, oparty na Swing'u. Prosty GUI oparty na frame'ie z text field'em do wpisywania
 * wiadomosci i textarea do wyswietlania calego dialogu.
 *
 * Klient dziala w nastepujacy sposob. Po wyslaniu przez serwer "SUBMITNAME" klient odpowiada,
 * wysylajac pobrana nazwe uzytkownika. Serwer bedzie tak dlugo wysylal "SUBMITNAME", dopoki
 * nazwa nie bedzie unikalna. Kiedy serwer wysle tekst zaczynajacy sie od "NAMEACCEPTED", klient bedzie
 * mial mozliwosc wysylania wiadomosci do serwera, ktore nastepnie beda nadawane do reszty klientow
 * podlaczonych do serwera. Po wyslaniu przez serwer wiadomosci zaczynajacej sie od "MESSAGE", wszystkie
 * znaki znajdujace sie po tym prefiksie beda wyswietlone w elemencie textarea.
 *
 * "//q" - powoduje wyjscie z czatu i zamkniecie klienta*
 */
public class Client extends initConnection{

    String serverAddress;
    Scanner in;
    PrintWriter out;
    // Utworzenie okna z tytulem
    JFrame frame = new JFrame("Projekt czat");
    // Utworzenie pola tekstowego(do wpisywania wiadomosci)
    JTextField textField = new JTextField(50);
    // Utworzenie obszaru tekstowego(do wyswietlania wszystkich wiadomosci)
    JTextArea messageArea = new JTextArea(16, 50);

    /**
     * W konstruktorze nastepuje inicjacja GUI, dopoki klient nie otrzyma od serwera wiadomosci
     * "NAMEACCPETED" pole tekstowe do wpisywania wiaodmosci pozostaje nieaktywne.     *
     */
    public Client(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Wysyla wiadomosc po nacisnieciu enter, nastepnie czysci pole tekstowe
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Wprowadz swoja nazwe na czacie: ", "Wybor nazwy uzytkownika",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            Socket socket = new Socket(serverAddress, 21974);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Projekt czat - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception{



        initConnection config = new initConnection("127.0.0.1");

//        if (args.length != 1) {
//            System.err.println("Pass the server IP as the sole command line argument");
//            return;
//        }

        Client client = new Client(config.getAddressIP());
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}