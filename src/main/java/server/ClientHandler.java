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

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); //Array estatico de todos los clientes conectados que sirve para el pase de mensajes
    private ActionLogger actionLogger;
    private final SellerActor sellerActor = new SellerActor();

    private Socket socket;
    private BufferedReader bufferedReader; //Canal de entrada de mensajes al servidor
    private BufferedWriter bufferedWriter; // Canal de salida de mensajes del servidor

    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine(); //obtenemos el username del cliente
            clientHandlers.add(this); //agrega el cliente al array compartido
            this.actionLogger = new ActionLogger("server-" + clientUsername + "-"); // se loggea la conexion del cliente
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }

    }

    @Override
    public void run() {
        //Ejecucion del metodo run del hilo
        String messageFromClient;
        while (socket.isConnected()) { //ciclo que permitira la escucha mientras el socket este conectado
            try {
                messageFromClient = bufferedReader.readLine(); //leemos el mensaje del cliente
                //verificamos que el mensaje sea para insertar y si sera exitosa a traves de un numero random
                if (messageFromClient.equals("INSERT") && RandomGenerator.generateNumber() > 1) {
                    insertInBank();
                    broadcastMessageToClients("WAIT"); //avisa a los clientes que se pongan en espera
                    TimeUnit.SECONDS.sleep(3);
                    broadcastMessageToClients("READY"); //notifica a los clientes que termino la ejecucion
                } else if (messageFromClient.equals("INSERT")) {
                    actionLogger.log("No inserto ingrediente", new SellerActor());
                }
                if (messageFromClient.equals("NEED")) {
                    int result = popBank(); //numero que se obtiene pqara ver si obtiene ingrediente y cual es

                    broadcastMessageToClient(String.valueOf(result)); //notifica al cliente el resultado
                }

                if (messageFromClient.equals("REFILL")) {
                    broadcastMessageToSeller(messageFromClient); // notifica al seller para que reponga ingrediente
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


    //Metodo para enviar mensajes al seller
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

    //metodo para enviar mensajes a los clientes que no son sellers
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

    //metodo para enviar mensajes al mismo cliente
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

    //loggea el estado actual de los bancos
    public void resume() {
        actionLogger.log("Stock", new PaperBankActor(), Server.bankPaper.getSize());
        actionLogger.log("Stock", new TobaccoBankActor(), Server.bankTobacco.getSize());
        actionLogger.log("Stock", new MatchBankActor(), Server.bankMatch.getSize());

    }

    //metodo que obtiene el resultado del pedido de ingredientes
    public int popBank() {
        //genera un numero al azar
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0 -> { //si es 0 intentara mandar fosforos
                if (Server.bankMatch.matches.size() > 0) {
                    Server.bankMatch.popMatch(); //remueve el fosforo
                    resume(); //loggea el estado de los bancos
                    return 0;
                }
                return -1; // notifica que no hay suficientes fosforos
            }
            case 1 -> { //intenta enviar papel
                if (Server.bankPaper.papers.size() > 0) {
                    Server.bankPaper.popPaper();
                    resume(); //loggea el estado de los bancos
                    return 1;
                }
                return -1;
            }
            default -> {
                //intenta mandar tabacos
                if (Server.bankTobacco.tobaccos.size() > 0) {
                    Server.bankTobacco.popTobacco();
                    resume(); //loggea el estado de los bancos
                    return 2;
                }
                return -1;
            }
        }
    }

    //metodo para reponer en los bancos
    public void insertInBank() {
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0 -> {
                //repone fosforos
                Server.bankMatch.pushMatch(new Match());
                actionLogger.log("[" + clientUsername +
                        "] repuso fosforo stock: [" + Server.bankMatch.getSize() +
                        "]", sellerActor, 1);
            }
            case 1 -> {
                //repone papel
                Server.bankPaper.pushPaper(new Paper());
                actionLogger.log("[" + clientUsername +
                        "] repuso papel stock: [" + Server.bankPaper.getSize() +
                        "]", sellerActor, 1);
            }
            default -> {
                //repone tabaco
                Server.bankTobacco.pushTobacco(new Tobacco());
                actionLogger.log("[" + clientUsername +
                        "] repuso tabaco stock: [" + Server.bankTobacco.getSize() +
                        "]", sellerActor, 1);
            }
        }
        resume(); //loggea el estado de los bancos
    }

    //remueve el cliente de la lista de clientes
    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    //metodo para cerrar el socket y todos los metodos de entrada y salida del socket
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
