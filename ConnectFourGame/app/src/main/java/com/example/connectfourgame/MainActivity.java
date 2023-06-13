package com.example.connectfourgame;

import static android.view.View.GONE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import static javax.crypto.Cipher.SECRET_KEY;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {


    ListView list;
    Button but1;
    Button but2;
    TextInputLayout tx1;
    TextInputLayout tx2;
    TextInputLayout tx3;
    TextView txpop;
    TextView txpopBlock;
    Button butAcc;
    Button butRej;
    ConnectTester c1;
    ConstraintLayout con1;
    ConstraintLayout con2;
    ListenToServer listen1;
    public static String players[];
    public static ArrayList<String> playersArray;
    public static String otherPlayerName;
    public static int switching=1;

    public static BufferedReader br;
    public static PrintWriter pw;
    private static ReceiveMessageFromServer rmfs;
    private static final String SECRET_KEY = "abcdefghijklmnop";
    //private static SecretKeySpec secretKeySpec;
    private static SecretKeySpec receivedSecretKey;
    private static Cipher cipher;
    private static IvParameterSpec ivParameterSpec;

    public static BufferedReader getBr() {
        return br;
    }

    public static PrintWriter getPw() {
        return pw;
    }

    public static ArrayAdapter<String> arr;
    public static int update =0;
    public static int gameStart=0;
    public static int offer=0;
    public static int waitingForPlayer=-1;
    /*public static void startCrypto(Cipher cip, SecretKeySpec key, IvParameterSpec iv, BufferedReader buf, PrintWriter wri){
        cipher = cip;
        receivedSecretKey = key;
        ivParameterSpec =iv;
        pw = wri;
        br =buf;
    }

    public static String cryptoRead() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
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
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Start");
        players = new String[20];
        Arrays.fill(players,"");

        list = findViewById(R.id.single_list_view);
        but1 = findViewById(R.id.button);
        but2 = findViewById(R.id.button2);
        butAcc=findViewById(R.id.buttonAccept);
        butRej=findViewById(R.id.buttonReject);
        tx1=findViewById(R.id.tx1);
        tx2=findViewById(R.id.tx2);
        tx3=findViewById(R.id.tx3);
        con1=findViewById(R.id.popup);
        con2=findViewById(R.id.popupBlock);
        txpop=findViewById(R.id.textPopup);
        txpopBlock=findViewById(R.id.textPopupBlock);

        arr
                = new ArrayAdapter<String>(
                this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                players);
        list.setAdapter(arr);

        //String text = list.getItemAtPosition(position).toString();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                String selectedFromList =(String) (list.getItemAtPosition(myItemInt));
                System.out.println(selectedFromList);
                ConnectTester.setPlayerTwo(selectedFromList);
            }
        });
        //konekcija
        but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to be executed when the button is clicked
                String ip = String.valueOf(tx1.getEditText().getText());
                System.out.println(ip);
                String port = String.valueOf(tx2.getEditText().getText());
                System.out.println(port);
                try {
                    c1 = new ConnectTester(MainActivity.this,ip,port);
                    Thread thr = new Thread(c1);
                    thr.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchPaddingException e) {
                    throw new RuntimeException(e);
                }

                //ako je povezan
                tx1.setVisibility(v.GONE);
                tx2.setVisibility(v.GONE);
                but1.setVisibility(v.GONE);

                tx3.setVisibility(v.VISIBLE);
                but2.setVisibility(v.VISIBLE);
            }
        });

        //username
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to be executed when the button is clicked

                //ako je povezan

                //Thread thr2 = new Thread(a1);
                //thr2.start();
                ConnectTester.setUsername(String.valueOf(tx3.getEditText().getText()));
                tx3.setVisibility(v.GONE);
                but2.setVisibility(v.GONE);
                list.setVisibility(v.VISIBLE);
                rmfs = new ReceiveMessageFromServer(c1);
                Thread thr2=new Thread(rmfs);
                thr2.start();
                System.out.println("Made new thread");
                //String s1="igrac";
                //players[0]=s1;
                //switchActivities();
                runThread(v);
                System.out.println("End of first activity");
                //switchActivities();
            }
        });

        butAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmfs.accepted=1;
                con1.setVisibility(v.GONE);
            }
        });

        butRej.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rmfs.accepted=0;
                con1.setVisibility(v.GONE);
            }
        });

        }
    private void switchActivities() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }


    private void runThread(View v) {

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                //System.out.println("Glavni thread"+waitingForPlayer);
                                if(update==1) {
                                    arr.notifyDataSetChanged();
                                    update=0;
                                }
                                if(offer==1){
                                    con1.setVisibility(v.VISIBLE);
                                    txpop.setText(otherPlayerName+" wants to play with you");
                                    offer=0;
                                }
                                if(waitingForPlayer==1){
                                    txpopBlock.setText("Waiting for other player");
                                    con2.setVisibility(v.VISIBLE);
                                }
                                if(waitingForPlayer==0){
                                    txpopBlock.setText("Odbijen !");
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    con2.setVisibility(v.GONE);
                                    waitingForPlayer=-1;
                                }
                                if(waitingForPlayer==-1){
                                    con2.setVisibility(v.GONE);
                                }
                                if(gameStart==1 && switching==1){
                                    waitingForPlayer=-1;
                                    con1.setVisibility(v.GONE);
                                    switchActivities();
                                    switching=0;
                                }
                            }
                        });
                        Thread.sleep(300);

                        /*if(waitingForPlayer==0){
                            waitingForPlayer=-1;
                        }*/
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


}