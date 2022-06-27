package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Seller {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    private String username;

    private String receivedMessage = "inicial";


    public Seller(Socket socket, String username){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();


            while(socket.isConnected()){
                if(receivedMessage.equals("REFILL")){
                    bufferedWriter.write("INSERT");
                    bufferedWriter.flush();
                    receivedMessage = "";
                    bufferedWriter.newLine();
                }
                TimeUnit.SECONDS.sleep(3);
            }
        } catch (IOException  e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromGroupChat;
            while(socket.isConnected()){
                try {
                    msgFromGroupChat = bufferedReader.readLine();
                    receivedMessage = msgFromGroupChat;
                    System.out.println(msgFromGroupChat);
                }catch (IOException e){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }

            if(bufferedWriter != null){
                bufferedWriter.close();
            }

            if(socket != null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args ) throws IOException {
        String username = "SELLER";
        Socket socket = new Socket("192.168.1.6", 8080);
        Seller seller = new Seller(socket, username);
        seller.listenForMessage();
        seller.sendMessage();
    }

}
