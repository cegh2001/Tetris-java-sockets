import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TetrisClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendScore(String nombre, int score) throws IOException {
        out.println(nombre);
        out.println(score);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        TetrisClient client = new TetrisClient();
        try {
            String serverIP = "127.0.0.1"; // IP del servidor
            int serverPort = 8080; // Puerto del servidor
            client.startConnection(serverIP, serverPort);

            // Enviar nombre y puntuación al servidor
            client.sendScore("jugador 1", 1);
            client.sendScore("jugador 2", 6);
            client.sendScore("jugador 3", 7);

            // Cerrar la conexión con el servidor
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

