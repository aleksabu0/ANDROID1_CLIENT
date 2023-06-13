package com.example.connectfourgame;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import static javax.crypto.Cipher.SECRET_KEY;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class ConnectTester implements Runnable  {
    MainActivity parent;
    private Socket socket;
    private String ip;
    private String port;
    public static BufferedReader br;
    public static PrintWriter pw;
    private static ReceiveMessageFromServer rmfs;
    private static final String SECRET_KEY = "abcdefghijklmnop";
    //private static SecretKeySpec secretKeySpec;
    private static SecretKeySpec receivedSecretKey;
    private static Cipher cipher;
    private static IvParameterSpec ivParameterSpec;
    private static String username ="";
    private static String playerTwo="";
    public static int anotherGame=2;
    public static void setUsername(String username1) {
        username = username1;
    }

    public static void setPlayerTwo(String playerTwo) {
        ConnectTester.playerTwo = playerTwo;
    }

    public Socket getSocket() {
        return socket;
    }

    public static SecretKeySpec getReceivedSecretKey() {
        return receivedSecretKey;
    }

    public static Cipher getCipher() {
        return cipher;
    }

    public static IvParameterSpec getIvParameterSpec() {
        return ivParameterSpec;
    }

    public static BufferedReader getBr() {
        return br;
    }

    public static PrintWriter getPw() {
        return pw;
    }

    public ConnectTester(MainActivity parent, String ip, String port) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException {
        //System.out.println("Creating");
        this.parent = parent;
        this.port=port;
        this.ip=ip;

    }

    public static String cryptoRead() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        byte[] encryptedMessage = br.readLine().getBytes();             // Decrypt the message
        cipher.init(Cipher.DECRYPT_MODE, receivedSecretKey, ivParameterSpec);
        byte[] decryptedMessage = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        }
        String message = new String(decryptedMessage);
        return message;
    }

    public static void cryptoWrite(String message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException{
        System.out.println(receivedSecretKey);
        System.out.println(cipher.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, receivedSecretKey, ivParameterSpec);
        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
        // Send the encrypted message to the server
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pw.println(Base64.getEncoder().encodeToString(encryptedMessage));
        }
    }

    @Override
    public void run() {
        System.out.println("Running");
        try {
            socket = new Socket(ip, Integer.parseInt(port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Created Socket");
        System.out.println(this.socket.getLocalAddress());

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);

            String encodedSecretKey = br.readLine();
            byte[] secretKeyBytes = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                secretKeyBytes = Base64.getDecoder().decode(encodedSecretKey);
            }
            receivedSecretKey = new SecretKeySpec(secretKeyBytes, "AES");

            String encodedIV = br.readLine();
            byte[] ivBytes = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ivBytes = Base64.getDecoder().decode(encodedIV);
            }
            ivParameterSpec = new IvParameterSpec(ivBytes);

            System.out.println(receivedSecretKey);
            System.out.println(ivParameterSpec);
            // Create cipher for encryption and decryption
            //Cipher cipher = Cipher.getInstance("AES");
            AsyncSend.startCrypto(cipher, receivedSecretKey, ivParameterSpec, br, pw);
            System.out.println("End of ConnectTester setup");

            while (username.equals("")) {
            }
            cryptoWrite(username);
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(ConnectTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        /*rmfs = new ReceiveMessageFromServer(this);
        Thread thr2=new Thread(rmfs);
        thr2.start();*/

        while (true) {
            while (MainActivity.gameStart == 0) {
                //System.out.println("Other thread");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (!playerTwo.equals("")) {
                    try {
                        System.out.println("Trazi drugog igraca " + playerTwo);
                        cryptoWrite("Zahtev za drugog igraca");
                        MainActivity.waitingForPlayer = 1;
                        cryptoWrite(playerTwo);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalBlockSizeException e) {
                        throw new RuntimeException(e);
                    } catch (BadPaddingException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchPaddingException e) {
                        throw new RuntimeException(e);
                    } catch (InvalidAlgorithmParameterException e) {
                        throw new RuntimeException(e);
                    }
                    playerTwo = "";
                }
            }
            System.out.println("Zavrsen prvi deo");
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    String line = cryptoRead();
                    System.out.println("Ocitana linija " + line);
                    if (line.equals("Your turn")) {
                        GameActivity.potez = 1;
                        GameActivity.drawTurn=1;
                        cryptoWrite("move");
                        while (GameActivity.chosenColumn == (-1)) {
                        }
                        cryptoWrite(String.valueOf(GameActivity.chosenColumn));
                        GameActivity.chosenColumn = -1;
                        GameActivity.potez = 0;
                        GameActivity.drawTurn=1;
                    }
                    if (line.equals("Turn ends")) {
                        String kojiIgrac;
                        kojiIgrac = cryptoRead();
                        if (kojiIgrac.equals("A")) {
                            GameActivity.boja = 0;
                        } else {
                            GameActivity.boja = 1;
                        }
                        String rowServer = cryptoRead();
                        String columnServer = cryptoRead();
                        GameActivity.rowServer = Integer.parseInt(rowServer);
                        GameActivity.collumnServer = Integer.parseInt(columnServer);
                        GameActivity.update = 1;
                        System.out.println("INFO ABOUT TURN SENT");
                    }
                    if(line.equals("Game over")){
                        GameActivity.over=1;
                        line=cryptoRead();
                        System.out.println("Win or lose? "+line);
                        if(line.equals("Winner")){
                            GameActivity.win="win!";
                        }
                        else{
                            GameActivity.win="lose!";
                        }
                        int cnt=0;
                        InputStream is =socket.getInputStream();
                        byte[] inputData = new byte[1024];

                        while(anotherGame==2){
                            if(is.available()!=0){
                                line=cryptoRead();
                                System.out.println(line);
                                break;
                            };
                        }
                        System.out.println("Exited loop 1");
                        if(anotherGame==1){
                            cryptoWrite("ACC");
                        }
                        else if (anotherGame==0){
                            cryptoWrite("REJ");
                        }
                        System.out.println("Waiting for server");
                        while(true){
                            if(line.equals("Cont") || line.equals("Stop")){
                                break;
                            }
                            line=cryptoRead();
                        }
                        System.out.println("Server answers");
                        if(line.equals("Stop")){
                            GameActivity.reset=2;
                            MainActivity.gameStart=0;
                            MainActivity.switching=1;
                            anotherGame=2;
                            break;
                        }
                        else{
                            anotherGame=2;
                            GameActivity.reset=1;
                        }
                    }
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                } catch (IllegalBlockSizeException e) {
                    throw new RuntimeException(e);
                } catch (BadPaddingException e) {
                    throw new RuntimeException(e);
                } catch (InvalidAlgorithmParameterException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchPaddingException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
