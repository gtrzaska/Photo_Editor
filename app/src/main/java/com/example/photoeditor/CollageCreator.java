package com.example.photoeditor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class CollageCreator extends AppCompatActivity {

    private ArrayList<HashMap<String, Integer>> hashMap = new ArrayList<HashMap<String, Integer>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int screenwidth = this.getResources().getDisplayMetrics().widthPixels;
        final int screenheight = this.getResources().getDisplayMetrics().heightPixels - 100;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage_creator);
        Bundle bundle = getIntent().getExtras();
        int pickedCollage = bundle.getInt("pickedCollage", 1);
        hashMap = CollageMapCreator.CollageHashMap(pickedCollage, screenwidth, screenheight);

        Log.v("-------", String.valueOf(hashMap));
        FrameLayout _linear = (FrameLayout) findViewById(R.id.collageLinear);
        ImageView imageView;
        for (int i = 0; i < hashMap.size(); i++) {
            imageView = new ImageView(CollageCreator.this);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(hashMap.get(i).get("width"), hashMap.get(i).get("height"));
            imageView.setLayoutParams(layoutParams);
            // imageView.setId(i);
            imageView.setX(hashMap.get(i).get("x"));
            imageView.setY(hashMap.get(i).get("y"));
            imageView.setBackgroundColor(Color.rgb(255, 255, 255));

            _linear.addView(imageView);

        }
    }
}
