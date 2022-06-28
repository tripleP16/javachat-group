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

    /**
     * Instancia del cliente fumador.
     * Inicializa buffer de lectura y escritura.
     * Se le asigna nombre y un ingrediente infinito al cliente, también se inicializan
     * el buffer de entrada y salida de datos.
     *
     * @param socket   Socket que usa el cliente para la conexión con el servidor
     * @param username Nombre del usuario con el que el servidor lo identificará
     */
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

    /**
     * Se encarga de enviar un mensaje al servidor.
     * Si el último mensaje recibido es distinto a "WAIT"
     * chequea si ya es su segundo intento de pedir el ingrediente que necesita, si se
     * cumple este caso se manda el mensaje "REFILL" al servidor. En otro caso se envia
     * "NEED" pidiendo el ingrediente que necesite para armar el cigarro.
     */
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

    /**
     * Escucha los mensajes del servidor en un hilo aparte del principal.
     * <p>
     * Mientras el haya conexión con el servidor, escucha los mensajes que vienen del
     * servidor.
     * <p>
     * "Mensaje recibido", descripción
     * <p>
     * "WAIT", se está reponiendo un ingrediente y el servidor no admite más pedidos mientras
     * repone.
     * <p>
     * "READY", el servidor ha terminado de reponer el ingrediente que necesita el cliente.
     * <p>
     * "-1", no recibió ningún ingrediente
     * <p>
     * "0", recibió fósforo
     * <p>
     * "1", recibió papel
     * <p>
     * "2", recibió tabaco
     */
    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while (socket.isConnected()) {
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    messageReceived = msgFromGroupChat;
                    switch (msgFromGroupChat) {
                        case "WAIT" -> {
                            check = true;
//                            System.out.println("El vendedor esta reponiendo");
                            actionLogger.log("esta reponiendo", new SellerActor());
                        }
                        case "READY" -> {
                            check = false;
//                            System.out.println("El vendedor ya repuso");
                            actionLogger.log("ya repuso", new SellerActor());
                        }
                        case "-1" -> {
                            tries++;
//                            System.out.println("No recibe nada");
                            actionLogger.log("no recibe nada", new SmokerActor());
                        }
                        case "0" -> {
                            bankMatch.pushMatch(new Match());
//                            System.out.println("Recibe fosforos");
                            actionLogger.log("recibe fosforos", new SmokerActor());
                            tries = 0;
                        }
                        case "1" -> {
                            bankPaper.pushPaper(new Paper());
//                            System.out.println("Recibe papel");
                            actionLogger.log("recibe papel", new SmokerActor());
                            tries = 0;
                        }
                        case "2" -> {
                            bankTobacco.pushTobacco(new Tobacco());
//                            System.out.println("Recibe tabaco");
                            actionLogger.log("recibe tabaco", new SmokerActor());
                            tries = 0;
                        }
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

    /**
     * Arma el cigarro si el cliente tiene los ingredientes necesarios.
     * <p>
     * Revisa de que tipo de ingrediente infinito tiene el fumador y chequea los otros dos
     * ingredientes que necesita para armar el cigarro.
     * <p>
     * Si el fumador tiene los ingredientes necesarios procede a armarlo y fumarlo
     *
     * @throws InterruptedException se puede interrumpir la ejecución de fumar.
     */
    public void buildCigarette() throws InterruptedException {
        switch (infiniteIngredient.id) {
            case "TOBACCO":
                if (bankPaper.papers.size() > 0 && bankMatch.matches.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    actionLogger.log("Armando cigarro con tabacos[infinitos] " +
                            "fosforos[" + bankMatch.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());
                    bankMatch.popMatch();
                    bankPaper.popPaper();
                    actionLogger.log("Le quedan tabacos[infinitos] " +
                            "fosforos[" + bankMatch.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());

//                    System.out.println("Cigarro armado le quedan infinitos tabacos, " +
//                            bankMatch.matches.size() + " fósforos, " + bankPaper.papers.size() +
//                            " papeles");
                    smoke(cigarrate);
                } else {
//                    System.out.println("No pudo armar el cigarro");
                    actionLogger.log("No pudo armar el cigarro", new SmokerActor());
                }
                break;
            case "PAPER":
                if (bankTobacco.tobaccos.size() > 0 && bankMatch.matches.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    actionLogger.log("Armando cigarro con papeles[infinitos] " +
                            "fosforos[" + bankMatch.getSize() + "] " +
                            "tabacos[" + bankTobacco.getSize() + "]", new SmokerActor());
                    bankTobacco.popTobacco();
                    bankMatch.popMatch();
                    actionLogger.log("Le quedan papeles[infinitos] " +
                            "fosforos[" + bankMatch.getSize() + "] " +
                            "tabacos[" + bankTobacco.getSize() + "]", new SmokerActor());
//                    System.out.println("Cigarro armado le quedan infintos papeles, " +
//                            bankMatch.matches.size() + " fosforos, " + bankTobacco.tobaccos.size() +
//                            " tabacos");
                    smoke(cigarrate);
                } else {
//                    System.out.println("No pudo armar el cigarro");
                    actionLogger.log("No pudo armar el cigarro", new SmokerActor());
                }
                break;
            case "MATCH":
                if (bankTobacco.tobaccos.size() > 0 && bankPaper.papers.size() > 0) {
                    Cigarrate cigarrate = new Cigarrate(new Tobacco(), new Match(), new Paper());
                    actionLogger.log("Armando cigarro con fosforos[infinitos] " +
                            "tabacos[" + bankTobacco.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());
                    bankTobacco.popTobacco();
                    bankPaper.popPaper();
                    actionLogger.log("Le quedan fosforos[infinitos] " +
                            "tabacos[" + bankTobacco.getSize() + "] " +
                            "papeles[" + bankPaper.getSize() + "]", new SmokerActor());
//                    System.out.println("Cigarro armado le quedan infintos fosoforos, " +
//                            bankTobacco.tobaccos.size() + " tabacos, " + bankPaper.papers.size() +
//                            " papeles");
                    smoke(cigarrate);
                } else {
//                    System.out.println("No pudo armar el cigarro");
                    actionLogger.log("No pudo armar el cigarro", new SmokerActor());
                }
                break;
        }

    }

    /**
     * Fumar el cigarro.
     *
     * @param cigarrete Cigarro armado para fumar.
     * @throws InterruptedException se puede interrumpir la ejecución de fumar.
     */
    private void smoke(Cigarrate cigarrete) throws InterruptedException {
        messageReceived = "WAIT";
        cigarrete.smoke();
        messageReceived = "READY";
    }

    /**
     * Cierra todos los recursos utilizados.
     *
     * @param socket         socket utiliza para la conexión del cliente con el servidor.
     * @param bufferedReader flujo de lectura utilizado para leer los mensajes del cliente.
     * @param bufferedWriter flujo de escritura utilizado para escribir los mensajes al cliente.
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
     * Establece aleatoriamente el ingrediente infinito del fumador
     */
    public void setInfiniteIngredient() {
        int randomNumber = RandomGenerator.generateNumber();
        switch (randomNumber) {
            case 0 -> infiniteIngredient = new Match();
            case 1 -> infiniteIngredient = new Paper();
            case 2 -> infiniteIngredient = new Tobacco();
        }
    }

    /**
     * Punto de entrada del programa del fumador
     *
     * @param args parámetros del programan NO SE USAN
     * @throws IOException en caso de que no se pueda establecer la conexión con el servidor.
     */
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce un nombre de usuario para el fumador: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("192.168.1.11", 8080);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }

}
