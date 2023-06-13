package com.example.connectfourgame;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Arrays;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import javax.swing.JComboBox;
//import javax.swing.JOptionPane;
/**
 * Ova klasa se koristi za prijem poruka od strane servera jer ce one stizati
 * asinhrono (ne znamo u kom trenutku ce se novi korisnik ukljuciti u Chat room,
 * kao ni kada ce nam poslati poruku)
 *
 */
public class ReceiveMessageFromServer implements Runnable {

    ConnectTester parent;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static final String SECRET_KEY = "abcdefghijklmnop";
    //private static SecretKeySpec secretKeySpec;
    private static SecretKeySpec receivedSecretKey;
    private static Cipher cipher;
    private static IvParameterSpec ivParameterSpec;
    public static int accepted=2;
    public ReceiveMessageFromServer(ConnectTester parent) {
        //parent ce nam trebati da bismo mogli iz ovog thread-a da menjamo sadrzaj
        //komponenti u osnovnom GUI prozoru (npr da popunjavamo Combo Box sa listom
        //korisnika
        this.parent = parent;
        //BufferedReader koristimo za prijem poruka od servera, posto su sve
        //poruke u formi Stringa i linija teksta, BufferedReader je zgodniji nego
        //da citamo poruke iz InputStream objekta
        this.br = parent.getBr();
        this.pw = parent.getPw();
        this.cipher=parent.getCipher();
        this.ivParameterSpec = parent.getIvParameterSpec();
        this.receivedSecretKey=parent.getReceivedSecretKey();
    }
    public static String cryptoRead() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
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
        System.out.println("Entered thread");
        //Beskonacna petlja
        while(true) {
            while(MainActivity.gameStart != 1) {
               try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Third thread");
                String line;
                try {
                    System.out.println("Entered reading");
                    line = cryptoRead();
                    System.out.println(line);
                    if (line.equals("Drugi igrac salje zahtev")) {
                        System.out.println("Reached this point");
                        String user1 = cryptoRead();
                        cryptoWrite("Odgovor na zahtev");
                        MainActivity.otherPlayerName = user1;
                        MainActivity.offer = 1;
                        while (accepted == 2) {
                        }
                        if (accepted == 1) {
                            cryptoWrite("Prihvacen");
                            cryptoWrite(user1);
                        } else {
                            cryptoWrite("Odbijen");
                            cryptoWrite(user1);
                        }
                        accepted = 2;
                    }
                    if (line.equals("Pocetak igre")) {
                        cryptoWrite("Nije dostupan");
                        Thread.sleep(300);
                        MainActivity.gameStart = 1;
                        System.out.println("Ulaz u igru za igraca");
                    }
                    if (line.equals("Odbijen")) {
                        System.out.println("Reached odbijen");
                        MainActivity.waitingForPlayer = 0;
                    }
                    if (line.equals("Spisak igraca")) {
                        Arrays.fill(MainActivity.players, "");
                        int i = 0;
                        line = cryptoRead();
                        while (!line.equals("Done")) {
                            MainActivity.players[i] = line;
                            i++;
                            line = cryptoRead();
                        }
                        MainActivity.update = 1;
                        //MainActivity.update();
                    }


                } catch (IOException ex) {
                    Logger.getLogger(ReceiveMessageFromServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidAlgorithmParameterException e) {
                    throw new RuntimeException(e);
                } catch (IllegalBlockSizeException e) {
                    throw new RuntimeException(e);
                } catch (BadPaddingException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeyException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchPaddingException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

