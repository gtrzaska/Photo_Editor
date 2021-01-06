package com.example.photoeditor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class EditorActivity extends AppCompatActivity implements View.OnClickListener {
    ProgressBar progressBar;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    int red = 0;
    int green = 0;
    int blue = 255;
    int opacity = 255;
    int textSize = 56;
    String textToAdd;
    Bitmap bitmap, originalBitmap;
    ArrayList<Bitmap> history = new ArrayList<Bitmap>();
    boolean isHsvCropHidden = true;
    boolean isHsvAdjustHidden = true;
    boolean isHsvFilterHidden = true;
    boolean isBrightnessSeekBarHidden = true;
    boolean isSaturationSeekBarHidden = true;
    boolean isContrastSeekBarHidden = true;
    boolean isDrawingEnabled = false;
    boolean isTextAddEnabled = false;
    View hsvFilter, hsvCrop, hsvAdjust, adjustBrightness, adjustSaturation, adjustContrast, clAdjustBrush;
    ImageView image;
    Bitmap tempBitmap, tempBitmap2;
    Canvas tempCanvas;
    Paint myRectPaint;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        image = findViewById(R.id.ivEditedPhoto);

        hsvFilter = findViewById(R.id.hsvFilter);
        hsvCrop = findViewById(R.id.hsvCrop);
        hsvAdjust = findViewById(R.id.hsvAdjust);
        adjustBrightness = findViewById(R.id.adjustBrightness);
        adjustSaturation = findViewById(R.id.adjustSaturation);
        adjustContrast = findViewById(R.id.adjustContrast);
        clAdjustBrush = findViewById(R.id.clAdjustBrush);
        progressBar = findViewById(R.id.editProgressBar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        try {
            bitmap = BitmapFactory.decodeStream(openFileInput("myImage"));
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            adjustSizeOfBitmapToScreen();

            originalBitmap = bitmap;
            history.add(bitmap);
            image.setImageBitmap(bitmap);
            Log.d("rotation", String.valueOf(image.getRotation()));
            //image.setRotation(90);
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

        View btCompare = findViewById(R.id.btCompare);
        btCompare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    image.setImageBitmap(bitmap);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    image.setImageBitmap(originalBitmap);
                }

                return false;
            }
        });

        View btUndo = findViewById(R.id.btUndo);
        btUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (history.size() > 1) {
                    undo();
                }
            }
        });

        View btSave = findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final File isSaved = SaveImage.saveBitmap(bitmap);

                AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                builder.setTitle("Udostępnić zdjęcie?");
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                                BuildConfig.APPLICATION_ID + ".provider",
                                isSaved);
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);

                        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);  //optional//use this when you want to send an image
                        shareIntent.setType("image/jpeg");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "send"));
                    }
                });
                builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();

                alert.show();
                if (isSaved != null) {
                    Toast.makeText(EditorActivity.this, R.string.Saved, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EditorActivity.this, "Nie udało się zapisać", Toast.LENGTH_LONG).show();
                }

            }
        });

        View btUpload = findViewById(R.id.btUpload);
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isOnline()) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        if (mUploadTask != null && mUploadTask.isInProgress()) {
                            Toast.makeText(EditorActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            uploadFile();
                        }
                    } else {
                        Toast.makeText(EditorActivity.this, R.string.noLogged, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditorActivity.this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
                }
            }
        });


        final View btCropMenu = findViewById(R.id.btCropMenu);
        btCropMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        LinearLayout llCrop = (LinearLayout) findViewById(R.id.llCrop);
        for (int i = 0; i < llCrop.getChildCount(); i++) {
            llCrop.getChildAt(i).setOnClickListener(this);
        }

        final View btFilterMenu = findViewById(R.id.btFilter);
        btFilterMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View hsvMenu = findViewById(R.id.hsvMenu);
                if (isHsvFilterHidden) {
                    closeMenu();
                    isHsvFilterHidden = false;
                    hsvFilter.setTranslationY((-1) * (hsvMenu.getHeight() + hsvFilter.getHeight() + 16));
                } else {
                    isHsvFilterHidden = true;
                    hsvFilter.setTranslationY(0);
                }
            }
        });

        LinearLayout llFilters = (LinearLayout) findViewById(R.id.llFilters);
        for (int i = 0; i < llFilters.getChildCount(); i++) {
            llFilters.getChildAt(i).setOnClickListener(this);
        }

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

                if (isDrawingEnabled) {
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
                } else if (isTextAddEnabled) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downx = event.getX();
                            downy = event.getY();
                            tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            upx = event.getX();
                            upy = event.getY();
                            tempCanvas = new Canvas(tempBitmap);
                            tempCanvas.drawBitmap(bitmap, 0, 0, null);
                            tempCanvas.drawText(textToAdd, upx, upy, myRectPaint);
                            image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
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
                    red = 0;
                    green = 0;
                    blue = 255;
                    myRectPaint.setColor(Color.rgb(0, 0, 255));
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
                if (isHsvAdjustHidden) {
                    closeMenu();
                    isHsvAdjustHidden = false;
                    hsvAdjust.setTranslationY((-2) * hsvAdjust.getHeight() - 4);
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
                if (isContrastSeekBarHidden) {
                    closeMenu();
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
                bitmap = ImageFilters.changeBitmapContrastAndBrightness(history.get(history.size() - 1), progress, 0);
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
                if (isBrightnessSeekBarHidden) {
                    closeMenu();
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
                bitmap = ImageFilters.changeBitmapContrastAndBrightness(history.get(history.size() - 1), 10, progress);
                image.setImageBitmap(bitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        View btSaturation = findViewById(R.id.btSaturation);
        btSaturation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSaturationSeekBarHidden) {
                    closeMenu();
                    isSaturationSeekBarHidden = false;
                    adjustSaturation.setVisibility(View.VISIBLE);
                } else {
                    isSaturationSeekBarHidden = true;
                    adjustSaturation.setVisibility(View.INVISIBLE);
                }
            }
        });

        View btConfirmSaturation = findViewById(R.id.btConfirmSaturation);
        btConfirmSaturation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.add(bitmap);
                closeMenu();
            }
        });

        SeekBar sbSaturation = (SeekBar) findViewById(R.id.sbSaturation);
        sbSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = ImageFilters.saturation(history.get(history.size() - 1), progress / 10f);
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
                if (isTextAddEnabled) {
                    tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                    tempCanvas = new Canvas(tempBitmap);
                    tempCanvas.drawBitmap(bitmap, 0, 0, null);
                    myRectPaint.setTextSize(progress * 7);
                    tempCanvas.drawText(textToAdd, upx, upy, myRectPaint);
                    image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                } else {
                    myRectPaint.setStrokeWidth(progress);
                }
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
                red = 0;
                green = 0;
                blue = 255;
                opacity = 255;
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
                        myRectPaint.setColor(Color.argb(opacity, red, green, blue));
                        if (isTextAddEnabled) {
                            tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                            tempCanvas = new Canvas(tempBitmap);
                            tempCanvas.drawBitmap(bitmap, 0, 0, null);
                            tempCanvas.drawText(textToAdd, upx, upy, myRectPaint);
                            image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                        }
                        alertDialog.dismiss();
                    }
                });
                final View ivColorPreview = alertDialog.findViewById(R.id.ivColorPreview);
                SeekBar sbRed = (SeekBar) alertDialog.findViewById(R.id.sbRed);
                sbRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        red = progress;
                        ivColorPreview.setBackgroundColor(Color.argb(opacity, red, green, blue));
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
                        ivColorPreview.setBackgroundColor(Color.argb(opacity, red, green, blue));
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
                        ivColorPreview.setBackgroundColor(Color.argb(opacity, red, green, blue));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                SeekBar sbOpacity = (SeekBar) alertDialog.findViewById(R.id.sbOpacity);
                sbOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        opacity = progress;
                        ivColorPreview.setBackgroundColor(Color.argb(opacity, red, green, blue));
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

        final View btText = findViewById(R.id.btText);
        btText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTextAddEnabled) {
                    isTextAddEnabled = false;
                    clAdjustBrush.setTranslationY(0);
                } else {
                    closeMenu();
                    isTextAddEnabled = true;
                    clAdjustBrush.setTranslationY((-2) * clAdjustBrush.getHeight());
                    View seekBar = findViewById(R.id.sbBrushSize);
                    seekBar.refreshDrawableState();
                    textToAdd = String.valueOf(R.string.text);
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                    ViewGroup viewGroup = findViewById(android.R.id.content);
                    final View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add_text, viewGroup, false);
                    builder.setView(dialogView);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    final EditText editText = alertDialog.findViewById(R.id.etAddText);
                    View btConfirm = alertDialog.findViewById(R.id.btConfirm);
                    View btClose = alertDialog.findViewById(R.id.btClose);
                    btConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            textToAdd = editText.getText().toString();
                            alertDialog.dismiss();
                            myRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                            myRectPaint.reset();            // czyszczenie
                            myRectPaint.setAntiAlias(true);    // wygładzanie
                            textSize = 72;
                            myRectPaint.setTextSize(textSize);
                            red = 0;
                            green = 0;
                            blue = 255;
                            upx = image.getWidth() / 2;
                            upy = image.getHeight() / 2;
                            myRectPaint.setColor(Color.rgb(0, 0, 255));
                            tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                            tempBitmap2 = tempBitmap;
                            tempCanvas = new Canvas(tempBitmap);
                            tempCanvas.drawBitmap(bitmap, 0, 0, null);
                            tempCanvas.drawText(textToAdd, upx, upy, myRectPaint);
                            image.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                        }
                    });
                    btClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });

    }

    public void undo() {
        bitmap = history.get(history.size() - 2);
        history.remove(history.size() - 1);
        history.trimToSize();
        image.setImageBitmap(bitmap);
    }

    public void closeMenu() {
        bitmap = history.get(history.size() - 1);
        image.setImageBitmap(bitmap);
        isHsvAdjustHidden = true;
        isDrawingEnabled = false;
        isTextAddEnabled = false;
        isHsvFilterHidden = true;
        isHsvCropHidden = true;
        isBrightnessSeekBarHidden = true;
        isSaturationSeekBarHidden = true;
        isContrastSeekBarHidden = true;
        adjustContrast.setVisibility(View.INVISIBLE);
        adjustBrightness.setVisibility(View.INVISIBLE);
        adjustSaturation.setVisibility(View.INVISIBLE);
        hsvCrop.setTranslationY(0);
        hsvAdjust.setTranslationY(0);
        hsvFilter.setTranslationY(0);
        clAdjustBrush.setTranslationY(0);
    }

    public void adjustSizeOfBitmapToScreen() {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        double x = ((screenWidth * 1.0) / (bitmap.getWidth() * 1.0));
        if (bitmap.getHeight() * x > screenHeight) {
            x = ((screenHeight * 1.0) / (bitmap.getHeight() * 1.0));
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * x), (int) (bitmap.getHeight() * x), true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btBright:
                bitmap = ImageFilters.changeBitmapContrastAndBrightness(bitmap, 10, 24);
                break;
            case R.id.btDark:
                bitmap = ImageFilters.changeBitmapContrastAndBrightness(bitmap, 10, -32);
                break;
            case R.id.btGrey:
                // bitmap = ImageFilters.saturation(bitmap, 0);
                bitmap = ImageFilters.gr(bitmap);
                break;
            case R.id.btSepia:
                bitmap = ImageFilters.sepia(bitmap);
                break;
            case R.id.btSketch:
                bitmap = ImageFilters.sketch(bitmap);
                break;
            case R.id.btNegative:
                bitmap = ImageFilters.negative(bitmap);
                break;
            case R.id.btBlueFilter:
                bitmap = ImageFilters.colorRGB(bitmap, false, false, true);
                break;
            case R.id.btRedFilter:
                bitmap = ImageFilters.colorRGB(bitmap, true, false, false);
                break;
            case R.id.btGreenFilter:
                bitmap = ImageFilters.colorRGB(bitmap, false, true, false);
                break;
            case R.id.btVignetteFilter:
                bitmap = ImageFilters.vignette(bitmap);
                break;
            case R.id.btBinary:
                bitmap = ImageFilters.binary(bitmap);
                break;
            case R.id.btBinaryBW:
                bitmap = ImageFilters.binaryBlackWhite(bitmap);
                break;
            case R.id.btSwapping:
                bitmap = ImageFilters.swappingColor(bitmap);
                break;
            case R.id.btRGB2YUV:
                bitmap = ImageFilters.yuv(bitmap);
                break;
            case R.id.btBlur:
                bitmap = ImageFilters.fastblur(bitmap, 10);
                break;
            case R.id.btRotate:
                bitmap = CropEditor.rotate(bitmap, 90);
                adjustSizeOfBitmapToScreen();
                break;
            case R.id.btFlipVertical:
                bitmap = CropEditor.flip(bitmap, false, true);
                break;
            case R.id.btFlipHorizontal:
                bitmap = CropEditor.flip(bitmap, true, false);
                break;
        }
        history.add(bitmap);
        image.setImageBitmap(bitmap);
    }

    boolean isPublic = false;
    private void uploadFile() {

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bOut);
        byte[] data = bOut.toByteArray();
        // StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".png");
        // UploadTask uploadTask = fileReference.putBytes(data);

        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setTitle("Ustawić jako publiczne?");
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                isPublic = true;
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isPublic = false;
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();

        alert.show();

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".png");
        UploadTask uploadTask = fileReference.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                Upload upload = new Upload(FirebaseAuth.getInstance().
                                        getCurrentUser().getEmail(), imageUrl, isPublic, 0);
                                String uploadId = mDatabaseRef.push().getKey();
                                mDatabaseRef.child(uploadId).setValue(upload);
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(EditorActivity.this,
                                        R.string.uploadSuccessful, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

}