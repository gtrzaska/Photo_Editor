package com.example.photoeditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class CollageCreator extends AppCompatActivity {

    private ArrayList<HashMap<String, Integer>> hashMap = new ArrayList<HashMap<String, Integer>>();
    private ImageView iv;
    private static final int CAMERA_REQUEST = 200;
    private static final int PICK_IMAGE = 100;
    Uri photoURI;

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

                                                                         Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                                         // Ensure that there's a camera activity to handle the intent
                                                                         if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                                                             // Create the File where the photo should go
                                                                             File photoFile = null;
                                                                             try {
                                                                                 photoFile = createImageFile();
                                                                             } catch (IOException ex) {
                                                                                 Log.d("----", ex.getMessage());
                                                                             }
                                                                             // Continue only if the File was successfully created
                                                                             if (photoFile != null) {
                                                                                 Log.d("----", "successfully created");
                                                                                 photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                                                                                         BuildConfig.APPLICATION_ID + ".provider",
                                                                                         photoFile);


                                                                                 cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                                                                 startActivityForResult(cameraIntent, CAMERA_REQUEST);

                                                                             }
                                                                         }



/*
                                                                         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                         //jesli jest dostepna zewnetrzny aparat
                                                                         if (intent.resolveActivity(getPackageManager()) != null) {
                                                                             startActivityForResult(intent, CAMERA_REQUEST);
                                                                         }*/
                                                                     }
                                                                 }
                                                             });
                                                             alert.show();


                                                         }
                                                     }

            );
        }

    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("----", resultCode + "  " + requestCode + "   " + photoURI);
        Bitmap bitmap = null;
        super.onActivityResult(requestCode, resultCode, data);
        //  if (data != null) {
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
                iv.setScaleType(ImageView.ScaleType.CENTER);
            } else if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {
                Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
                Log.d("----+", photoURI.toString());
                //  Bundle extras = data.getExtras();
                //Bitmap b = (Bitmap) extras.get("data");
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                    Log.d("----+", "hhj");
                } catch (IOException e) {
                    Log.d("----+", e.getMessage());
                }
                iv.setImageBitmap(bitmap);
                //  iv.setImageBitmap(b);
                iv.setScaleType(ImageView.ScaleType.CENTER);
            }


        //}
    }
}
