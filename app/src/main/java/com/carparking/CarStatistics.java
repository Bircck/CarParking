package com.carparking;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

public class CarStatistics extends AppCompatActivity{
    Thread canbusThread;
    VectorMasterView vector;
    TextView information_brake, information_battery;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_statistics);

        vector = findViewById(R.id.vectorBattery);
        information_brake = findViewById(R.id.information_brake);
        information_battery = findViewById(R.id.information_battery);

        canbusThread = new Thread(() -> {
            try {
                CanBusLogic.switchCanBusData(374, information_battery, vector);

                int currentId = 0;
//                while(true){
//                    if(canBusLogic.isSwitchReady()){
//                        switch(currentId){
//                            case 0:
//                            case 346:
//                                canBusLogic.switchCanBusData("374", information_battery);
//                                currentId = 374;
//                                Thread.sleep(10000);
//                                break;
//                            case 374:
//                                canBusLogic.switchCanBusData("346", information_brake);
//                                currentId = 346;
//                                Thread.sleep(10000);
//                                break;
//                            case 999:
//                                return;
//                        }
//                    }+
//                    Thread.sleep(3000);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        canbusThread.start();
    }
}
