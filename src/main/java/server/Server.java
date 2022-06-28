package server;

import bank.BankMatch;
import bank.BankPaper;
import bank.BankTobacco;
import cigarrate.Match;
import cigarrate.Paper;
import cigarrate.Tobacco;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;
    public static BankMatch bankMatch = new BankMatch();
    public static BankPaper bankPaper = new BankPaper();
    public static BankTobacco bankTobacco = new BankTobacco();
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        startBanks();
        try {
            while(!serverSocket.isClosed()){ //Mientras no se cierre el socket el servidor esta escuchando para nuevas conexiones
                Socket socket = serverSocket.accept(); // Acepta la conexion este metodo espera hasta que llegue un cliente
                System.out.println("Se ha conectado una persona");
                ClientHandler clientHandler = new ClientHandler(socket); // Crea una instancia de la clase client handler y le injecta el socket
                Thread thread = new Thread(clientHandler); // Crea un hilo nuevo para el acceso del cliente
                thread.start(); // Inicia el hilo
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void startBanks() {
        // Agrega los dos primeros ingredientes a cada banco
        bankMatch.pushMatch(new Match());
        bankMatch.pushMatch(new Match());
        bankTobacco.pushTobacco(new Tobacco());
        bankTobacco.pushTobacco(new Tobacco());
        bankPaper.pushPaper(new Paper());
        bankPaper.pushPaper(new Paper());
    }
    public void closeServerSocket() {
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080); // Iniciamos una conexion del tipo socket en el puerto 8080
        Server server = new Server(serverSocket); // Creamos el server
        server.startServer(); // Inicia el servidor
    }
}
