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
    PathModel distance1;
    PathModel distance2;
    PathModel distance3;
    PathModel distance4;
    int mode = 0;

    //Thread animationThread;

    // initialize valueAnimator and pass start and end color values
    ValueAnimator valueAnimator;
    Resources res;

    int color_parked;
    int color_reverse;
    int color_notActive;
    int parked_active = 0;
    int reverse_active = 0;

    MediaPlayer quackMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sound
        quackMediaPlayer = MediaPlayer.create(this, R.raw.quack);

        //park/revers symbols
        parking_symbol = findViewById(R.id.parking_symbol);
        parking_symbol.setOnClickListener(this);
        reverse_symbol = findViewById(R.id.reverse_symbol);
        reverse_symbol.setOnClickListener(this);
        declareColors();

        distance_vector = (VectorMasterView) findViewById(R.id.distance_vector);
        distance_vector.setOnClickListener(this);

        // find the correct path using name
        distance1 = distance_vector.getPathModelByName("distance1");
        distance2 = distance_vector.getPathModelByName("distance2");
        distance3 = distance_vector.getPathModelByName("distance3");
        distance4 = distance_vector.getPathModelByName("distance4");

        //other stuff
        car_statistics_screen_button = findViewById(R.id.car_statistics_screen_button);
        car_statistics_screen_button.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == car_statistics_screen_button) {
            Intent i = new Intent(this, CarStatistics.class);
            startActivity(i);
        }
        if (v == distance_vector) {
            distanceVectorUpdater();
//            quackMediaPlayer.setLooping(true);
//            quackMediaPlayer.start();
        }
        if (v == parking_symbol){
            updateRevParkSymbols(v);
            //quackMediaPlayer.pause();
        }
        if (v == reverse_symbol){
            updateRevParkSymbols(v);
        }
    }

    public void declareColors(){
        //colors on buttons
        final Context context=this;
        res = context.getResources();
        color_parked = res.getColor(R.color.red_900);
        color_reverse = getResources().getColor(R.color.white);
        color_notActive = getResources().getColor(R.color.main_lightgrey);
        parking_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
        reverse_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
    }

    public void updateRevParkSymbols(View clicked_symbol){
        //parked
        if(clicked_symbol == parking_symbol){
            if(parked_active == 0){
                parked_active++;
                parking_symbol.setColorFilter(color_parked, PorterDuff.Mode.SRC_ATOP);
            } else if(parked_active >= 1){
                parked_active = 0;
                parking_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
            }
        }
        //reverse
        if(clicked_symbol == reverse_symbol){
            if(reverse_active == 0){
                reverse_active++;
                reverse_symbol.setColorFilter(color_reverse, PorterDuff.Mode.SRC_ATOP);
            } else if(reverse_active >= 1){
                reverse_active = 0;
                reverse_symbol.setColorFilter(color_notActive, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void distanceVectorUpdater(){
        mode++;
        if(mode == 1){
            //repeating animation
            valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#111111"), Color.parseColor("#555555"));
            valueAnimator.setDuration(2000);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
            System.out.println(valueAnimator.getRepeatCount());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // set fill color and update view
                    distance1.setFillColor((Integer) valueAnimator.getAnimatedValue());
                    distance2.setFillColor((Integer) valueAnimator.getAnimatedValue());
                    distance3.setFillColor((Integer) valueAnimator.getAnimatedValue());
                    distance4.setFillColor((Integer) valueAnimator.getAnimatedValue());

                    distance_vector.update();
                }
            });
            valueAnimator.start();
        }
        else if(mode == 2){
            valueAnimator.cancel();

            // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)
            distance1.setFillColor(Color.parseColor("#555555"));
            distance2.setFillColor(Color.parseColor("#555555"));
            distance3.setFillColor(Color.parseColor("#555555"));
            distance4.setFillColor(Color.parseColor("#555555"));

            distance_vector.update();
        }
        else if(mode == 3){
            // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)
            distance4.setFillAlpha(0);
            distance3.setFillColor(Color.parseColor("#4f3d3d"));
            distance2.setFillColor(Color.parseColor("#4f3d3d"));
            distance1.setFillColor(Color.parseColor("#4f3d3d"));
            distance_vector.update();
        }
        else if(mode == 4){
            // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)
            distance4.setFillAlpha(0);
            distance3.setFillAlpha(0);
            distance2.setFillColor(Color.parseColor("#6e2424"));
            distance1.setFillColor(Color.parseColor("#6e2424"));
            distance_vector.update();
        }
        else if(mode == 5){
            // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)
            distance4.setFillAlpha(0);
            distance3.setFillAlpha(0);
            distance2.setFillAlpha(0);
            distance1.setFillColor(Color.parseColor("#961515"));
            distance_vector.update();
        }


        else if(mode == 6){
            // set the fill color (if fill color is not set or is TRANSPARENT, then no fill is drawn)
            distance4.setFillColor(Color.parseColor("#555555"));
            distance3.setFillColor(Color.parseColor("#555555"));
            distance2.setFillColor(Color.parseColor("#555555"));
            distance1.setFillColor(Color.parseColor("#555555"));
            distance4.setFillAlpha(1);
            distance3.setFillAlpha(1);
            distance2.setFillAlpha(1);
            distance_vector.update();
            mode = 0;
        }
    }
}
