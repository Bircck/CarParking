package com.carparking;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

public class CarStatistics extends AppCompatActivity implements View.OnClickListener{

    /*
    videos on audio if/when needed:
    https://www.youtube.com/watch?v=C_Ka7cKwXW0&list=PLrnPJCHvNZuCTyi1Gesn5TTbKVuhAjf8q&ab_channel=CodinginFlow

    github bibliotekt til battery cirklen/vektor ting
    https://github.com/harjot-oberai/VectorMaster

    mulight vektor alternativ
    https://github.com/tarek360/RichPath

    generelt android vector fis
    https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable
     */

    Button change_battery_button;

    VectorMasterView heartVector;

    // find the correct path using name
    PathModel outline;
    PathModel innerline;

    Float battery = 0.65f;

    TextView information_brake;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_statistics);

        heartVector = (VectorMasterView) findViewById(R.id.circle_vector_1);

        information_brake = findViewById(R.id.information_brake);

        CanBusLogic canBusLogic = new CanBusLogic();
        try {
            canBusLogic.startBT();
            canBusLogic.listenCanBusData("346", information_brake);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // find the correct path using name
        outline = heartVector.getPathModelByName("outline");
        innerline = heartVector.getPathModelByName("innerline");

        // set the stroke color
        outline.setStrokeColor(Color.parseColor("#ED4337"));
        innerline.setStrokeColor(Color.parseColor("#000000"));

        // set trim path start (values are given in fraction of length)
        //outline.setTrimPathStart(0.1f);

        // set trim path end (values are given in fraction of length)
        //outline.setTrimPathEnd(0.65f);

        // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)

        //GradientDrawable gradientDrawable = new GradientDrawable(
        //GradientDrawable.Orientation.TOP_BOTTOM, colors);

        heartVector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // initialise valueAnimator and pass start and end float values
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.1f, 0.4f);
                valueAnimator.setDuration(0);

                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                        // set trim end value and update view
                        outline.setTrimPathEnd((Float) valueAnimator.getAnimatedValue());
                        heartVector.update();
                    }
                });
                valueAnimator.start();
            }
        });

    }

    public void onClick(View v) {
        if (v == change_battery_button) {

            battery += 0.05f;

            if(battery > 0.9){
                battery = 0.05f;
            }

            outline.setTrimPathEnd(battery);
        }
    }
}
