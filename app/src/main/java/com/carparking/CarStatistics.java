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
        PathModel innerline = vector.getPathModelByName("innerline");
        PathModel outline = vector.getPathModelByName("outline");
        innerline.setStrokeColor(Color.RED);
        outline.setStrokeColor(Color.GREEN);
        vector.update();

        information_brake = findViewById(R.id.information_brake);
        information_battery = findViewById(R.id.information_battery);

        canbusThread = new Thread(() -> {
            try {
                int currentId = 374;
                CanBusLogic.switchCanBusData(currentId, information_battery, vector);
                while(true){
                    if(CanBusLogic.isSwitchReady()){
                        switch(currentId){
                            case 346:
                                currentId = 374;
                                CanBusLogic.switchCanBusData(currentId, information_battery, vector);
                                Thread.sleep(10000);
                                break;
                            case 374:
                                currentId = 346;
                                CanBusLogic.switchCanBusData(currentId, information_brake, vector);
                                Thread.sleep(10000);
                                break;
                            case 999:
                                return;
                        }
                    }
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        canbusThread.start();
    }
}
