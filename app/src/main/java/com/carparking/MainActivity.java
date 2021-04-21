package com.carparking;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton car_statistics_screen_button;

    ImageView reverse_symbol, parking_symbol;

    VectorMasterView distance_vector;

    // find the correct path using name

    //Thread animationThread;

    // initialize valueAnimator and pass start and end color values
    Resources res;

    int color_parked;
    int color_reverse;
    int color_notActive;
    int parked_active = 0;
    int reverse_active = 0;

    MediaPlayer quackMediaPlayer;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //sound
        quackMediaPlayer = MediaPlayer.create(this, R.raw.quack);

        //park/revers symbols
        parking_symbol = findViewById(R.id.parking_symbol);
        parking_symbol.setOnClickListener(this);
        //reverse_symbol = findViewById(R.id.reverse_symbol);
        //reverse_symbol.setOnClickListener(this);
        declareColors();

        distance_vector = (VectorMasterView) findViewById(R.id.distance_vector);

        // find the correct path using name

        //other stuff
        //car_statistics_screen_button = findViewById(R.id.car_statistics_screen_button);
        //car_statistics_screen_button.setOnClickListener(this);




        try {
            DistanceLogic.startBT(handler, distance_vector);
        } catch (Exception e) {

            for (int i=0; i < 15; i++)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
        try {
            CanBusLogic.startBT(handler);
            StartCarCommunication();
        } catch (Exception e) {

            for (int i=0; i < 15; i++)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }
    }



    public void declareColors(){
        //colors on buttons
        final Context context=this;
        res = context.getResources();
        color_parked = res.getColor(R.color.red_900,null);
        color_reverse = getResources().getColor(R.color.white,null);
        color_notActive = getResources().getColor(R.color.main_lightgrey,null);
        parking_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
        //reverse_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
    }

    public void onClick(View v) {

    }


    Thread canbusThread;
    VectorMasterView vector;
    TextView information_battery, information_brake, information_gear;
    private void StartCarCommunication(){

        vector = findViewById(R.id.vectorBattery_line);
        PathModel innerline = vector.getPathModelByName("innerline");
        PathModel outline = vector.getPathModelByName("outline");
        innerline.setStrokeColor(Color.RED);
        outline.setStrokeColor(Color.GREEN);

        vector.update();

        information_battery = findViewById(R.id.information_battery);
        //information_brake = findViewById(R.id.information_brake);
        information_gear = findViewById(R.id.information_gear);

        CanBusLogic.addTextview("battery", information_battery);
        CanBusLogic.addTextview("brake", information_brake);
        CanBusLogic.addTextview("gear", information_gear);
        CanBusLogic.addImageview("brake", parking_symbol);
        CanBusLogic.addColors("red", color_parked);
        CanBusLogic.addColors("grey", color_notActive);
        CanBusLogic.vector = vector;

        canbusThread = new Thread(() -> {
            try {
                int currentId = 418;
                CanBusLogic.switchCanBusData(currentId);
                while(true){
                    if(CanBusLogic.isReadyToSwitch()){
                        CanBusLogic.setReadyToSwitch(false);
                        switch(currentId){
                            case 346:
                                currentId = 374;
                                CanBusLogic.switchCanBusData(currentId);
                                //Thread.sleep(50);
                                break;
                            case 418:
                                currentId = 346;
                                CanBusLogic.switchCanBusData(currentId);
                                //Thread.sleep(50);
                                break;
                            case 374:
                                currentId = 418;
                                CanBusLogic.switchCanBusData(currentId);
                                //Thread.sleep(50);
                                break;
                            case 999:
                                return;
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        canbusThread.start();
    }
}
