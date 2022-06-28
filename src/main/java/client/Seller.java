package client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Seller {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String username;

    private String receivedMessage = "inicial";


    /**
     * Constructor de la clase Seller
     * <p>
     * instancia de la clase seller, inicializa el buffer de entrada y salida.
     *
     * @param socket   socket para la comunicación con el servidor.
     * @param username usuario que se identificará el cliente en el servidor.
     */
    public Seller(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Envia mensaje para reponer ingredientes al servidor.
     * <p>
     * Cuando recibe el mensaje "REFILL" envia "INSERT" para indicar que se debe reponer
     * los ingredientes.
     */
    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();


            while (socket.isConnected()) {
                if (receivedMessage.equals("REFILL")) {
                    bufferedWriter.write("INSERT");
                    bufferedWriter.flush();
                    receivedMessage = "";
                    bufferedWriter.newLine();
                }
                TimeUnit.SECONDS.sleep(3);
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Hilo que escucha los mensajes enviados por el servidor.
     */
    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    receivedMessage = msgFromGroupChat;
                    System.out.println(msgFromGroupChat);
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    /**
     * Cierra todos los recursos de la conexión con el servidor.
     *
     * @param socket         socket para la comunicación con el servidor.
     * @param bufferedReader buffer de lectura.
     * @param bufferedWriter buffer de escritura.
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Punto de entrada del cliente vendedor
     *
     * @param args NO SE USAN
     * @throws IOException si no se puede conectar con el servidor.
     */
    public static void main(String[] args) throws IOException {
        String username = "SELLER";
        Socket socket = new Socket("192.168.1.11", 8080);
        Seller seller = new Seller(socket, username);
        seller.listenForMessage();
        seller.sendMessage();
    }

}
