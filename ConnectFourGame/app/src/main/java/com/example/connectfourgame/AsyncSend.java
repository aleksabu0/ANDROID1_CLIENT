package com.example.connectfourgame;


import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AsyncSend implements Runnable  {
    private static SecretKeySpec receivedSecretKey;
    private static Cipher cipher;
    private static IvParameterSpec ivParameterSpec;

    public static BufferedReader br;
    public static PrintWriter pw;

    public static BufferedReader getBr() {
        return br;
    }

    public static PrintWriter getPw() {
        return pw;
    }
    public static void startCrypto(Cipher cip, SecretKeySpec key, IvParameterSpec iv, BufferedReader buf, PrintWriter wri){
        cipher = cip;
        receivedSecretKey = key;
        ivParameterSpec =iv;
        pw = wri;
        br =buf;
    }

    public static String cryptoRead() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] encryptedMessage = br.readLine().getBytes();             // Decrypt the message
        cipher.init(Cipher.DECRYPT_MODE, receivedSecretKey, ivParameterSpec);
        byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
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

    }
}
