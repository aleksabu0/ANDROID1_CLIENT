package com.example.connectfourgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends AppCompatActivity {
    public static int chosenColumn=-1;
    public static int potez=0;
    public static int collumnServer;
    public static int rowServer;

    public static int update=0;
    public static int boja=-1;
    public static int over=0;
    public static int reset=0;
    public static String win;
    TextView txpop;
    TextView txPlayer;
    TextView txNotYourTurn;
    Button butAcc;
    Button butRej;
    ConstraintLayout con1;

    private int imageIdsArray [];
    private int[] imageIds = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private int warning=-1;
    public static int drawTurn=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        butAcc=findViewById(R.id.buttonAccept);
        butRej=findViewById(R.id.buttonReject);
        con1=findViewById(R.id.popup);
        txpop=findViewById(R.id.textPopup);
        txPlayer=findViewById(R.id.textView3);
        txNotYourTurn=findViewById(R.id.textView);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        int gridSize = (int) Math.sqrt(imageIds.length);
        imageIdsArray = new int[42];
        for (int i = 0; i < 42; i++) {
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image1); // Replace 'image' with your own image resource
            imageView.setImageBitmap(bitmap);

            // Calculate the dimensions for each image view to fit the screen
            int imageWidth = screenWidth/7 ;
            int imageHeight = screenHeight/9;
            imageView.setLayoutParams(new ViewGroup.LayoutParams(imageWidth, imageHeight));

            final int column = i; // Remember the column index for the touch listener
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            System.out.println("Touched Column"+(column%7+1));
                            if(potez==1) {
                                chosenColumn = column % 7;
                            }
                            else{
                                warning =1;
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        default:
                            return false;
                    }
                }

            });
            butAcc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectTester.anotherGame=1;
                }
            });
            butRej.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectTester.anotherGame=0;
                }
            });
            int x=View.generateViewId();
            imageIdsArray[i]=x;
            imageView.setId(x);
            gridLayout.addView(imageView);
        }
        runThread(findViewById(R.id.popup));
    }

    private void runThread(View v) {
        new Thread() {
            public void run() {
                final boolean[] running = {true};
                while(running[0]) {
                    //System.out.println("Entered redrawing");
                    try {
                        Thread.sleep(50);
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                Bitmap bitmap;
                                if (update == 1) {
                                    int num = (5 - rowServer) * 7 + collumnServer;
                                    ImageView im = findViewById(imageIdsArray[num]);
                                    if (boja == 0) {
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image2);
                                    } else {
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image3);
                                    }
                                    im.setImageBitmap(bitmap);
                                    update = 0;
                                }
                                if(over==1){
                                    con1.setVisibility(v.VISIBLE);
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    txpop.setText("Game over! You "+win);
                                    over=0;
                                }
                                if(reset==1){
                                    con1.setVisibility(v.GONE);
                                    for (int i = 0; i < 42; i++) {
                                        ImageView im = findViewById(imageIdsArray[i]);
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image1);
                                        im.setImageBitmap(bitmap);
                                    }
                                    reset=0;
                                }
                                if(reset==2){
                                    con1.setVisibility(v.GONE);
                                    for (int i = 0; i < 42; i++) {
                                        ImageView im = findViewById(imageIdsArray[i]);
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image1);
                                        im.setImageBitmap(bitmap);
                                    }
                                    reset=0;
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    running[0] = false;
                                    finish();
                                }
                                if(warning==0){
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    txNotYourTurn.setVisibility(v.GONE);
                                    warning =-1;
                                }

                                if(warning==1){
                                    txNotYourTurn.setVisibility(v.VISIBLE);
                                    warning =0;
                                }

                                if(drawTurn==1){
                                    if(potez==1){
                                        txPlayer.setText("Your turn");
                                    }
                                    else{
                                        txPlayer.setText("Other player's turn");
                                    }
                                    drawTurn=0;
                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}