package com.example.photoeditor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class EditorActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Bitmap bitmap, originalBitmap;
    ArrayList<Bitmap> history = new ArrayList<Bitmap>();
    boolean isHsvCropHidden = true;
    boolean isHsvAdjustHidden = true;
    boolean isHsvFilterHidden = true;
    boolean isBrightnessSeekBarHidden = true;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        verifyStoragePermissions(EditorActivity.this);
        image = findViewById(R.id.ivEditedPhoto);

        try {
            bitmap = BitmapFactory.decodeStream(openFileInput("myImage"));
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (bitmap.getWidth() > 1000 || bitmap.getHeight() > 1000) {
                int x;
                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    x = 2 + (bitmap.getWidth() / 1000);
                } else {
                    x = 2 + (bitmap.getHeight() / 1000);
                }

                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / x, bitmap.getHeight() / x, true);

            }

            originalBitmap = bitmap;
            history.add(bitmap);
            image.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        View btCompare = findViewById(R.id.btCompare);
        btCompare.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    image.setImageBitmap(originalBitmap);
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    image.setImageBitmap(bitmap);
                }
                return false;
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
                if (history.size() > 1) {
                    bitmap = history.get(history.size() - 2);
                    history.remove(history.size() - 1);
                    history.trimToSize();
                    image.setImageBitmap(bitmap);
                }
            }
        });

        View btSave = findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Assume block needs to be inside a Try/Catch block.
                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                try {
                    fOut = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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

        final View btCropMenu = findViewById(R.id.btCropMenu);
        btCropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View hsvCrop = findViewById(R.id.hsvCrop);
                if (isHsvCropHidden) {
                    closeMenu();
                    isHsvCropHidden = false;
                    hsvCrop.setTranslationY((-2) * hsvCrop.getHeight());
                } else {
                    isHsvCropHidden = true;
                    hsvCrop.setTranslationY(0);
                }
            }
        });

        View btRotate = findViewById(R.id.btRotate);
        btRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                bitmap = CropEditor.rotate(bitmap, 90);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipVertical = findViewById(R.id.btFlipVertical);
        btFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                bitmap = CropEditor.flip(bitmap, false, true);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipHorizontal = findViewById(R.id.btFlipHorizontal);
        btFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                bitmap = CropEditor.flip(bitmap, true, false);
                image.setImageBitmap(bitmap);
            }
        });

        final View btCrop = findViewById(R.id.btCrop);
        btCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        final View btFilterMenu = findViewById(R.id.btFilter);
        btFilterMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View hsvFilter = findViewById(R.id.hsvFilter);
                View hsvMenu = findViewById(R.id.hsvMenu);
                if (isHsvFilterHidden) {
                    closeMenu();
                    isHsvFilterHidden = false;
                    hsvFilter.setTranslationY((-1) * (hsvMenu.getHeight() + hsvFilter.getHeight() + 2));
                } else {
                    isHsvFilterHidden = true;
                    hsvFilter.setTranslationY(0);
                }
            }
        });

        View btBright = findViewById(R.id.btBright);
        btBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = Filters.brightness(bitmap, 15);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btGrey = findViewById(R.id.btGrey);
        btGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = Filters.grey(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btSepia = findViewById(R.id.btSepia);
        btSepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = Filters.sepia(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        final View btAdjustMenu = findViewById(R.id.btAdjustMenu);
        btAdjustMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View hsvAdjust = findViewById(R.id.hsvAdjust);
                if (isHsvAdjustHidden) {
                    closeMenu();
                    isHsvAdjustHidden = false;
                    hsvAdjust.setTranslationY((-2) * hsvAdjust.getHeight());
                } else {
                    isHsvAdjustHidden = true;
                    hsvAdjust.setTranslationY(0);
                }
            }
        });

        View btBrightness = findViewById(R.id.btBrightness);
        btBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View adjustBrightness = findViewById(R.id.adjustBrightness);
                if (isBrightnessSeekBarHidden) {
                    isBrightnessSeekBarHidden = false;
                    adjustBrightness.setVisibility(View.VISIBLE);
                } else {
                    isBrightnessSeekBarHidden = true;
                    adjustBrightness.setVisibility(View.INVISIBLE);
                }
            }
        });

        View btConfirmBrightness = findViewById(R.id.btConfirmBrightness);
        btConfirmBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                closeMenu();
            }
        });

        SeekBar sbBrightness;
        sbBrightness = (SeekBar) findViewById(R.id.sb_brghtness);
        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = Filters.brightness(history.get(history.size() - 1), progress);
                image.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

    public void closeMenu() {
        bitmap = history.get(history.size() - 1);
        image.setImageBitmap(bitmap);
        View hsvFilter = findViewById(R.id.hsvFilter);
        View hsvCrop = findViewById(R.id.hsvCrop);
        View hsvAdjust = findViewById(R.id.hsvAdjust);
        View adjustBrightness = findViewById(R.id.adjustBrightness);
        isHsvAdjustHidden = true;
        isHsvFilterHidden = true;
        isHsvCropHidden = true;
        isBrightnessSeekBarHidden = true;
        adjustBrightness.setVisibility(View.INVISIBLE);
        hsvCrop.setTranslationY(0);
        hsvAdjust.setTranslationY(0);
        hsvFilter.setTranslationY(0);
    }

}



