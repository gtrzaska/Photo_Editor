package com.example.photoeditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditorActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Bitmap bitmap, previousBitmap;
    boolean isHsvCropHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        verifyStoragePermissions(EditorActivity.this);
        final ImageView image = findViewById(R.id.ivEditedPhoto);

        try {
            bitmap = BitmapFactory.decodeStream(openFileInput("myImage"));
            previousBitmap = bitmap;
            image.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        View btCrop = findViewById(R.id.btCrop);
        btCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View hsvCrop = findViewById(R.id.hsvCrop);
                if (isHsvCropHidden) {
                    isHsvCropHidden = false;
                    hsvCrop.setTranslationY(hsvCrop.getTranslationY() + (-2) * hsvCrop.getHeight());
                } else {
                    isHsvCropHidden = true;
                    hsvCrop.setTranslationY(hsvCrop.getTranslationY() + (2) * hsvCrop.getHeight());
                }
            }
        });

        View btToMenu = findViewById(R.id.btPreviousScreen);
        btToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        View btUndo = findViewById(R.id.btUndo);
        btUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = previousBitmap;
                image.setImageBitmap(bitmap);
            }
        });

        View btSave = findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = new File(getExternalFilesDir(null), "PhotoEditor");
                // Assume block needs to be inside a Try/Catch block.
                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                try {
                    fOut = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Log.d("gt-----", path);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                try {
                    fOut.flush();
                    fOut.close();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(EditorActivity.this);
                    builder1.setMessage(R.string.saved);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        View btRotate = findViewById(R.id.btRotate);
        btRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousBitmap = bitmap;
                bitmap = CropEditor.rotate(bitmap, 90);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipVertical = findViewById(R.id.btFlipVertical);
        btFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousBitmap = bitmap;
                bitmap = CropEditor.flip(bitmap, false, true);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipHorizontal = findViewById(R.id.btFlipHorizontal);
        btFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousBitmap = bitmap;
                bitmap = CropEditor.flip(bitmap, true, false);
                image.setImageBitmap(bitmap);
            }
        });
/*
        View btTest = findViewById(R.id.imageButton);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView image = findViewById(R.id.ivEditedPhoto);
                image.setImageBitmap(emboss(bitmap));

            }
        });*/

    }
/*
    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }*/

    public static Bitmap emboss(Bitmap src) {
        double[][] EmbossConfig = new double[][]{
                {-1, 0, -1},
                {0, 4, 0},
                {-1, 0, -1}
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(EmbossConfig);
        convMatrix.Factor = 1;
        convMatrix.Offset = 127;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }


    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

}



