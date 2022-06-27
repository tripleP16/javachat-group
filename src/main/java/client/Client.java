package client;

import bank.BankMatch;
import bank.BankPaper;
import bank.BankTobacco;
import cigarrate.*;
import common.RandomGenerator;
import common.logger.ActionLogger;
import common.logger.actor.SellerActor;
import common.logger.actor.SmokerActor;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final ActionLogger actionLogger = new ActionLogger("client-smoker-");
    private SmokerActor smokerActor;

    private String username;
    public BankMatch bankMatch = new BankMatch();
    public BankPaper bankPaper = new BankPaper();
    public BankTobacco bankTobacco = new BankTobacco();

    public Ingredient infiniteIngredient = null;

    public int tries = 0;

    public boolean check = false;

    public boolean ask = false;

    public String messageReceived = "";


    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            this.bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            setInfiniteIngredient();
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while (socket.isConnected()) {

                if (!messageReceived.equals("WAIT")) {
                    if (tries == 2) {
                        bufferedWriter.write("REFILL");
                        tries = 0;
                    } else {
                        bufferedWriter.write("NEED");
                    }
                }


                bufferedWriter.newLine();
                bufferedWriter.flush();
                TimeUnit.SECONDS.sleep(3);

            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    messageReceived = msgFromGroupChat;
                    switch (msgFromGroupChat) {
                        case "WAIT":
                            check = true;
//                            System.out.println("El vendedor esta reponiendo");
                            actionLogger.log("esta reponiendo", new SellerActor());
                            break;
                        case "READY":
                            check = false;
//                            System.out.println("El vendedor ya repuso");
                            actionLogger.log("ya repuso", new SellerActor());
                            break;
                        case "-1":
                            tries++;
//                            System.out.println("No recibe nada");
                            actionLogger.log("no recibe nada", new SmokerActor());
                            break;
                        case "0":
                            bankMatch.pushMatch(new Match());
//                            System.out.println("Recibe fosforos");
                            actionLogger.log("recibe fósforos", new SmokerActor());
                            tries = 0;
                            break;
                        case "1":
                            bankPaper.pushPaper(new Paper());
//                            System.out.println("Recibe papel");
                            actionLogger.log("recibe papel", new SmokerActor());
                            tries = 0;
                            break;
                        case "2":
                            bankTobacco.pushTobacco(new Tobacco());
//                            System.out.println("Recibe tabaco");
                            actionLogger.log("recibe tabaco", new SmokerActor());
                            tries = 0;
                            break;

                    }
                    buildCigarette();

                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void buildCigarette() throws InterruptedException {
        switch (infiniteIngredient.id) {
            case "TOBACCO":
                if (bankPaper.papers.size() > 0 && bankMatch.matches.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    bankMatch.popMatch();
                    bankPaper.popPaper();
                    actionLogger.log("Armando cigarro tabacos[infinitos] " +
                            "fósforos[" + bankMatch.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());

//                    System.out.println("Cigarro armado le quedan infinitos tabacos, " +
//                            bankMatch.matches.size() + " fósforos, " + bankPaper.papers.size() +
//                            " papeles");
                    smoke(cigarrate);
                } else {
                    System.out.println("No pudo armar el cigarro");
                }
                break;
            case "PAPER":
                if (bankTobacco.tobaccos.size() > 0 && bankMatch.matches.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    bankTobacco.popTobacco();
                    bankMatch.popMatch();
                    actionLogger.log("Armando cigarro papeles[infinitos] " +
                            "fósforos[" + bankMatch.getSize() + "] " +
                            "tabacos[" + bankTobacco.getSize() + "]", new SmokerActor());
//                    System.out.println("Cigarro armado le quedan infintos papeles, " +
//                            bankMatch.matches.size() + " fosforos, " + bankTobacco.tobaccos.size() +
//                            " tabacos");
                    smoke(cigarrate);
                } else {
                    System.out.println("No pudo armar el cigarro");
                }
                break;
            case "MATCH":
                if (bankTobacco.tobaccos.size() > 0 && bankPaper.papers.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    bankTobacco.popTobacco();
                    bankPaper.popPaper();
//                    System.out.println("Cigarro armado le quedan infintos fosoforos, " +
//                            bankTobacco.tobaccos.size() + " tabacos, " + bankPaper.papers.size() +
//                            " papeles");
                    actionLogger.log("Armando fósforos[infinitos] " +
                            "tabacos[" + bankTobacco.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());
                    smoke(cigarrate);
                } else {
//                    System.out.println("No pudo armar el cigarro");
                    actionLogger.log("No pudo armar el cigarro", new SmokerActor());
                }
                break;
        }

    }

    private void smoke(Cigarrate cigarrate) throws InterruptedException {
        messageReceived = "WAIT";
        cigarrate.smoke();
        messageReceived = "READY";
    }

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

    public void setInfiniteIngredient() {
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0:
                infiniteIngredient = new Match();
                break;
            case 1:
                infiniteIngredient = new Paper();
                break;
            case 2:
                infiniteIngredient = new Tobacco();
                break;
        }
    }


    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 8080);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }

}
