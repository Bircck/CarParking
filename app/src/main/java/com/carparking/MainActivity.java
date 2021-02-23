package com.carparking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button testing_button, car_statistics_screen_button;
    ImageView testing_image;
    ImageView first_bar, second_bar, third_bar;

    int counter;
    int green = Color.parseColor("#058a00");
    int yellow = Color.parseColor("#d4d400");
    int red = Color.parseColor("#9c1000");
    int black = Color.parseColor("#000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        car_statistics_screen_button = findViewById(R.id.car_statistics_screen_button);
        car_statistics_screen_button.setOnClickListener(this);
        testing_button = findViewById(R.id.testing_button);
        testing_image = findViewById(R.id.testing_image);

        first_bar = findViewById(R.id.first_bar);
        second_bar = findViewById(R.id.second_bar);
        third_bar = findViewById(R.id.third_bar);

        counter = 0;

        testing_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                counter++;

                if(counter == 1){
                    first_bar.setColorFilter(green);
                } else if(counter == 2){
                    second_bar.setColorFilter(yellow);
                } else if(counter == 3){
                    third_bar.setColorFilter(red);
                } else{
                    counter = 0;
                    first_bar.setColorFilter(black);
                    second_bar.setColorFilter(black);
                    third_bar.setColorFilter(black);
                }

                System.out.println("asdf");
                //testing_image.setColorFilter(green);
                //testing_image.setColorFilter(getApplicationContext().getResources().getColor(R.color.purple_200));
            }
        });
    }

    public void onClick(View v) {
        if (v == car_statistics_screen_button) {
            Intent i = new Intent(this, CarStatistics.class);
            startActivity(i);
        }
    }
}