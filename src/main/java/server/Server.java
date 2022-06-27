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
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void startBanks() {
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
        ServerSocket serverSocket = new ServerSocket(8080);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
