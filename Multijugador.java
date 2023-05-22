import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Multijugador {
    private JFrame frame;
    private JButton unirseSalaButton;

    // Crear instancia de cliente y establecer la conexión

    private static String ip;

    private static final int PORT = 8080; // Puerto en el que el servidor está escuchando

    public Multijugador() {
        frame = new JFrame("Multijugador");
        frame.setSize(300, 200); // Establecer un tamaño fijo para la ventana

        JPanel panel = new JPanel();

        unirseSalaButton = new JButton("Unirse a Sala");
        unirseSalaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ip = JOptionPane.showInputDialog(frame, "Ingrese la IP del servidor:");
            }
        });
        panel.add(unirseSalaButton);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }


    public String getIp() {
        return ip;
    }

    public int getPort() {
        return PORT;
    }
}
