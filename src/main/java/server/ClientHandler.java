package server;

import cigarrate.Match;
import cigarrate.Paper;
import cigarrate.Tobacco;
import common.RandomGenerator;
import common.logger.ActionLogger;
import common.logger.actor.MatchBankActor;
import common.logger.actor.PaperBankActor;
import common.logger.actor.SellerActor;
import common.logger.actor.TobaccoBankActor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private ActionLogger actionLogger;
    private final SellerActor sellerActor = new SellerActor();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            this.actionLogger = new ActionLogger("server-" + clientUsername + "-");
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }

    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient.equals("INSERT") && RandomGenerator.generateNumber() > 1) {
                    insertInBank();
                    broadcastMessageToClients("WAIT");
                    TimeUnit.SECONDS.sleep(3);
                    broadcastMessageToClients("READY");
                } else if (messageFromClient.equals("INSERT")) {
                    actionLogger.log("No inserto ingrediente", new SellerActor());
                }
                if (messageFromClient.equals("NEED")) {
                    int result = popBank();

                    broadcastMessageToClient(String.valueOf(result));
                }

                if (messageFromClient.equals("REFILL")) {
                    broadcastMessageToSeller(messageFromClient);
                }
                TimeUnit.SECONDS.sleep(3);


            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void broadcastMessageToSeller(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals("SELLER")) {

                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void broadcastMessageToClients(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals("SELLER")) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void broadcastMessageToClient(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals("SELLER") && clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void resume() {
// System.out.println("Quedan: ");
// System.out.println(Server.bankMatch.matches.size() + " fosforos");
// System.out.println(Server.bankPaper.papers.size() + " papeles");
// System.out.println(Server.bankTobacco.tobaccos.size() + " tabacos");
        actionLogger.log("Stock", new PaperBankActor(), Server.bankPaper.getSize());
        actionLogger.log("Stock", new TobaccoBankActor(), Server.bankTobacco.getSize());
        actionLogger.log("Stock", new MatchBankActor(), Server.bankMatch.getSize());

    }

    public int popBank() {
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0 -> {
                if (Server.bankMatch.matches.size() > 0) {
                    Server.bankMatch.popMatch();
                    resume();
                    return 0;
                }
                return -1;
            }
            case 1 -> {
                if (Server.bankPaper.papers.size() > 0) {
                    Server.bankPaper.popPaper();
                    resume();
                    return 1;
                }
                return -1;
            }
            default -> {
                if (Server.bankTobacco.tobaccos.size() > 0) {
                    Server.bankTobacco.popTobacco();
                    resume();
                    return 2;
                }
                return -1;
            }
        }
    }

    public void insertInBank() {
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0 -> {
                Server.bankMatch.pushMatch(new Match());
//                System.out.println(Server.bankMatch.matches.size());
//                System.out.println("El vendedor repuso 1 fósforo");
                actionLogger.log("[" + clientUsername +
                        "] repuso fosforo stock: [" + Server.bankMatch.getSize() +
                        "]", sellerActor, 1);
            }
            case 1 -> {
                Server.bankPaper.pushPaper(new Paper());
//                System.out.println(Server.bankPaper.papers.size());
//                System.out.println("El vendedor repuso 1 papel");
                actionLogger.log("[" + clientUsername +
                        "] repuso papel stock: [" + Server.bankPaper.getSize() +
                        "]", sellerActor, 1);
            }
            default -> {
                Server.bankTobacco.pushTobacco(new Tobacco());
//                System.out.println(Server.bankTobacco.tobaccos.size());
//                System.out.println("El vendedor repuso 1 tabaco");
                actionLogger.log("[" + clientUsername +
                        "] repuso tabaco stock: [" + Server.bankTobacco.getSize() +
                        "]", sellerActor, 1);
            }
        }
        resume();
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClientHandler();
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

}
