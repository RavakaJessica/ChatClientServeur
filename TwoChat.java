import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

public class TwoChat extends javax.swing.JFrame {

  ServerSocket ss;
  Socket s;
  JTextField text = new JTextField();
  JButton send = new JButton();
  JTextArea messages = new JTextArea();
  PrintWriter pout;
  BufferedReader br;
  ActionListener al;
  String ipstring;
  boolean ready2send = false;
  TwoChat pt;
  boolean HorC;
  String cliOrServ;

  public TwoChat(boolean hostOrConnect, String ip) {

    ipstring = ip;
    setLayout(null);
    setSize(500, 500);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    add(text);
    text.setLocation(5, 5);
    text.setSize(getWidth() - 40 - 100, 30);
    text.setEnabled(false);
    add(send);
    send.setLocation(10 + text.getWidth(), text.getY());
    send.setText("Envoyer");
    send.setSize(100, 30);
    add(messages);
    messages.setEditable(false);
    messages.setBorder(new EtchedBorder());
    messages.setLocation(5, text.getHeight() + text.getY() + 5);
    messages.setSize(getWidth() - 30, getHeight() - text.getY() - text.getHeight() - 50);

    al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ready2send = true;
      }
    };

    send.addActionListener(al);
    pt = this;
    HorC = hostOrConnect;
    if (HorC)
      cliOrServ = "\nServer: ";
    else
      cliOrServ = "\nClient: ";
    Messenger.start();

  }

  public static void main(String args[]) {

    int inp = JOptionPane.showConfirmDialog(null,
        "Vous voulez etre le serveur?\nOui - Agir en tant que serveur\nNon - Agir en tant que client",
        "Voulez-vous hoster?", JOptionPane.YES_NO_OPTION);
    if (inp == 0) {
      new TwoChat(true, null).setVisible(true);
    } else {
      String ipstring = JOptionPane.showInputDialog("Veuillez entrer l'adresse IP cible");
      try {
        InetAddress.getByName(ipstring);
        new TwoChat(false, ipstring).setVisible(true);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "IP Invalide");
      }
    }

  }

  Thread Messenger = new Thread() {
    public void run() {
      try {
        if (HorC) {
          messages.setText("En attente d'une connexion.\nEntrer mon IP au panel du client.\nMon IP: "
              + InetAddress.getLocalHost().getHostAddress());
          ss = new ServerSocket(9999);
          s = ss.accept();
          s.setKeepAlive(true);
        } else {
          messages.setText("Connexion vers:" + ipstring + ":9999");
          s = new Socket(InetAddress.getByName(ipstring), 9999);
        }
        text.setEnabled(true);
        pout = new PrintWriter(s.getOutputStream(), true);
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        messages
            .setText(messages.getText() + "\nConnexion a:" + s.getInetAddress().getHostAddress() + ":" + s.getPort());
        while (true) {
          if (ready2send == true) {
            pout.println(text.getText());
            messages.setText(messages.getText() + "\nMoi: " + text.getText());
            text.setText("");
            ready2send = false;
          }
          if (br.ready()) {
            messages.setText(messages.getText() + cliOrServ + br.readLine());
          }
          Thread.sleep(80);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(pt, ex.getMessage());
        messages.setText("Ne peut pas se connecter");
        try {
          wait(3000);
        } catch (InterruptedException ex1) {
        }
        System.exit(0);
      }
    }
  };

}