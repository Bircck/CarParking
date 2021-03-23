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
    TextView information_battery, information_brake, information_gear;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_statistics);

        vector = findViewById(R.id.vectorBattery);
        PathModel innerline = vector.getPathModelByName("innerline");
        PathModel outline = vector.getPathModelByName("outline");
        innerline.setStrokeColor(Color.RED);
        outline.setStrokeColor(Color.GREEN);
        vector.update();

        information_battery = findViewById(R.id.information_battery);
        information_brake = findViewById(R.id.information_brake);
        information_gear = findViewById(R.id.information_gear);

        CanBusLogic.addTextview("battery", information_battery);
        CanBusLogic.addTextview("brake", information_brake);
        CanBusLogic.addTextview("gear", information_gear);
        CanBusLogic.vector = vector;

        canbusThread = new Thread(() -> {
            try {
                int currentId = 418;
                CanBusLogic.switchCanBusData(currentId);
                while(true){
                    if(CanBusLogic.isSwitchReady()){
                        switch(currentId){
                            case 346:
                                currentId = 374;
                                CanBusLogic.switchCanBusData(currentId);
                                Thread.sleep(200);
                                break;
                            case 418:
                                currentId = 346;
                                CanBusLogic.switchCanBusData(currentId);
                                Thread.sleep(200);
                                break;
                            case 374:
                                currentId = 418;
                                CanBusLogic.switchCanBusData(currentId);
                                Thread.sleep(200);
                                break;
                            case 999:
                                return;
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        canbusThread.start();
    }
}
