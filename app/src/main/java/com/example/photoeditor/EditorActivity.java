package com.example.photoeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    int red = 0;
    int green = 0;
    int blue = 255;


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
    boolean isContrastSeekBarHidden = true;
    boolean isDrawingEnabled = false;
    ImageView image;
    Bitmap tempBitmap;
    Canvas tempCanvas;
    Paint myRectPaint;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        verifyStoragePermissions(EditorActivity.this);
        image = findViewById(R.id.ivEditedPhoto);

        try {
            int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            bitmap = BitmapFactory.decodeStream(openFileInput("myImage"));
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            if (bitmap.getWidth() > 1000 || bitmap.getHeight() > 1000) {
                int x;
                if (bitmap.getWidth() >= bitmap.getHeight()) {
                    x = 2 + (bitmap.getWidth() / 1000);
                } else {
                    x = 2 + (bitmap.getHeight() / 1000);
                }
                if((bitmap.getWidth() / x) > screenWidth) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / x, bitmap.getHeight() / x, true);
                } else {
                    x = bitmap.getWidth() / screenWidth;
                    bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / x, bitmap.getHeight() / x, true);
                }
            }

            originalBitmap = bitmap;
            history.add(bitmap);
            image.setImageBitmap(bitmap);
            closeMenu();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
                bitmap = CropEditor.rotate(bitmap, 90);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipVertical = findViewById(R.id.btFlipVertical);
        btFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = CropEditor.flip(bitmap, false, true);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btFlipHorizontal = findViewById(R.id.btFlipHorizontal);
        btFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = CropEditor.flip(bitmap, true, false);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        final View btCrop = findViewById(R.id.btCrop);
        btCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.color_picker, viewGroup, false);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
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
                bitmap = ImageFilters.brightness(bitmap, 15);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btDark = findViewById(R.id.btDark);
        btDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.brightness(bitmap, -25);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btGrey = findViewById(R.id.btGrey);
        btGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.grey(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btSepia = findViewById(R.id.btSepia);
        btSepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.sepia(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btSketch = findViewById(R.id.btSketch);
        btSketch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.sketch(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btNegative = findViewById(R.id.btNegative);
        btNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.negative(bitmap);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btGreenFilter = findViewById(R.id.btGreenFilter);
        btGreenFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.colorRGB(bitmap, false, true, false);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btRedFilter = findViewById(R.id.btRedFilter);
        btRedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.colorRGB(bitmap, true, false, false);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btBlueFilter = findViewById(R.id.btBlueFilter);
        btBlueFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = ImageFilters.colorRGB(bitmap, true, false, true);
                history.add(bitmap);
                image.setImageBitmap(bitmap);
            }
        });

        View btConfirmBrush = findViewById(R.id.btConfirmBrush);
        btConfirmBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(tempBitmap);
                closeMenu();
            }
        });

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(isDrawingEnabled) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downx = event.getX();
                            downy = event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            upx = event.getX();
                            upy = event.getY();
                            tempCanvas.drawLine(downx, downy, upx, upy, myRectPaint);
                            image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                            downx = upx;
                            downy = upy;
                            break;
                        case MotionEvent.ACTION_UP:
                            upx = event.getX();
                            upy = event.getY();
                            Log.v("------", downx + "x" + downy + " - " + upx + "x" + upy);
                            tempCanvas.drawLine(downx, downy, upx, upy, myRectPaint);
                            image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                            downx = upx;
                            downy = upy;
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });

        View btDraw = findViewById(R.id.btDraw);
        btDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View clAdjustBrush = findViewById(R.id.clAdjustBrush);
                if (isDrawingEnabled) {
                    isDrawingEnabled = false;
                    clAdjustBrush.setTranslationY(0);
                } else {
                    closeMenu();
                    isDrawingEnabled = true;
                    clAdjustBrush.setTranslationY((-2) * clAdjustBrush.getHeight());
                    View seekBar = findViewById(R.id.sbBrushSize);
                    seekBar.refreshDrawableState();
                    myRectPaint = new Paint();
                    myRectPaint.setStyle(Paint.Style.STROKE);
                    red = 0; green = 0; blue = 255;
                    myRectPaint.setColor(Color.rgb(0,0,255));
                    myRectPaint.setStrokeWidth(8);
                    myRectPaint.setStrokeCap(Paint.Cap.ROUND);
                    tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                    tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(bitmap, 0, 0, null);
                }
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

        View btContrast = findViewById(R.id.btContrast);
        btContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View adjustContrast = findViewById(R.id.adjustContrast);
                if (isContrastSeekBarHidden) {
                    isContrastSeekBarHidden = false;
                    adjustContrast.setVisibility(View.VISIBLE);
                } else {
                    isContrastSeekBarHidden = true;
                    adjustContrast.setVisibility(View.INVISIBLE);
                }
            }
        });

        View btConfirmContrast = findViewById(R.id.btConfirmContrast);
        btConfirmContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                closeMenu();
            }
        });

        SeekBar sbContrast = (SeekBar) findViewById(R.id.sbContrast);
        sbContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = ImageFilters.changeBitmapContrast(history.get(history.size() - 1), progress);
                image.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        SeekBar sbBrightness = (SeekBar) findViewById(R.id.sb_brghtness);
        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = ImageFilters.brightness(history.get(history.size() - 1), progress);
                image.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        SeekBar sbBrushSize = (SeekBar) findViewById(R.id.sbBrushSize);
        sbBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                myRectPaint.setStrokeWidth(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final View btColorPicker = findViewById(R.id.btColorPicker);
        btColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.color_picker, viewGroup, false);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                View btConfirmColor = alertDialog.findViewById(R.id.btConfirmColor);
                btConfirmColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myRectPaint.setColor(Color.rgb(red,green,blue));
                        alertDialog.dismiss();
                    }
                });
                final View ivColorPreview = alertDialog.findViewById(R.id.ivColorPreview);
                SeekBar sbRed = (SeekBar) alertDialog.findViewById(R.id.sbRed);
                sbRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        red = progress;
                        ivColorPreview.setBackgroundColor(Color.rgb(red, green, blue));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                SeekBar sbGreen = (SeekBar) alertDialog.findViewById(R.id.sbGreen);
                sbGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        green = progress;
                        ivColorPreview.setBackgroundColor(Color.rgb(red, green, blue));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                SeekBar sbBlue = (SeekBar) alertDialog.findViewById(R.id.sbBlue);
                sbBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        blue = progress;
                        ivColorPreview.setBackgroundColor(Color.rgb(red, green, blue));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

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
        View adjustContrast = findViewById(R.id.adjustContrast);
        View clAdjustBrush = findViewById(R.id.clAdjustBrush);
        isHsvAdjustHidden = true;
        isDrawingEnabled = false;
        isHsvFilterHidden = true;
        isHsvCropHidden = true;
        isBrightnessSeekBarHidden = true;
        isContrastSeekBarHidden = true;
        adjustContrast.setVisibility(View.INVISIBLE);
        adjustBrightness.setVisibility(View.INVISIBLE);
        hsvCrop.setTranslationY(0);
        hsvAdjust.setTranslationY(0);
        hsvFilter.setTranslationY(0);
        clAdjustBrush.setTranslationY(0);
    }

}




