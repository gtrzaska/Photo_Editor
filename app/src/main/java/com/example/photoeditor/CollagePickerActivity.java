package com.example.photoeditor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;


public class CollagePickerActivity extends AppCompatActivity {

    int pickedCollage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage_picker);

        View btToMenu = findViewById(R.id.btPreviousScreen);
        btToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollagePickerActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        View collage_1 = findViewById(R.id.collage_1);
        View collage_2 = findViewById(R.id.collage_2);
        View collage_3 = findViewById(R.id.collage_3);
        View collage_4 = findViewById(R.id.collage_4);
        collage_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickedCollage = 1;
                goToCollageCreator();
            }
        });
        collage_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickedCollage = 2;
                goToCollageCreator();
            }
        });
        collage_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickedCollage = 3;
                goToCollageCreator();
            }
        });
        collage_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickedCollage = 4;
                goToCollageCreator();

            }
        });

    }

    private void goToCollageCreator() {
        Intent intent = new Intent(CollagePickerActivity.this, CollageCreator.class);
        intent.putExtra("pickedCollage", pickedCollage);
        startActivity(intent);
    }
}
