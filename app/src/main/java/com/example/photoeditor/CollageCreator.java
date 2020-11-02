package com.example.photoeditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class CollageCreator extends AppCompatActivity {

    private ArrayList<HashMap<String, Integer>> hashMap = new ArrayList<HashMap<String, Integer>>();
    private ImageView iv;
    private static final int CAMERA_REQUEST = 200;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int screenwidth = this.getResources().getDisplayMetrics().widthPixels;
        final int screenheight = this.getResources().getDisplayMetrics().heightPixels - 100;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage_creator);
        Bundle bundle = getIntent().getExtras();
        int pickedCollage = bundle.getInt("pickedCollage", 1);
        hashMap = CollageMapCreator.CollageHashMap(pickedCollage, screenwidth, screenheight);
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

        final String[] imageSource = {"Galeria", "Kamera"};
        for (int j = 0; j < _linear.getChildCount(); j++) {

            _linear.getChildAt(j).setOnClickListener(new View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View view) {
                                                             iv = (ImageView) view;
                                                             AlertDialog.Builder alert = new AlertDialog.Builder(CollageCreator.this);
                                                             alert.setItems(imageSource, new DialogInterface.OnClickListener() {
                                                                 public void onClick(DialogInterface dialog, int ii) {
                                                                     if (ii == 0) {
                                                                         Intent intent = new Intent(Intent.ACTION_PICK);
                                                                         intent.setType("image/*");
                                                                         startActivityForResult(intent, PICK_IMAGE);
                                                                     } else if (ii == 1) {
                                                                         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                         //jesli jest dostepna zewnetrzny aparat
                                                                         if (intent.resolveActivity(getPackageManager()) != null) {
                                                                             startActivityForResult(intent, CAMERA_REQUEST);
                                                                         }
                                                                     }
                                                                 }
                                                             });
                                                             alert.show();


                                                         }
                                                     }

            );
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {

                Uri imgData = data.getData();
                InputStream stream = null;
                try {
                    stream = getContentResolver().openInputStream(imgData);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap b = BitmapFactory.decodeStream(stream);
                iv.setImageBitmap(b);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
            } else if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

                Bundle extras = data.getExtras();
                Bitmap b = (Bitmap) extras.get("data");
                iv.setImageBitmap(b);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
            }


        }
    }
}
