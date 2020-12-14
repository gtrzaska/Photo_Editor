package com.example.photoeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 200;
    Uri photoURI;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDirectory();
        View btCamera = findViewById(R.id.btCamera);
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    photoFile);


                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);

                        }
                    }

                }
            }
        });

        View btLibrary = findViewById(R.id.btLibrary);
        btLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    openGallery();
                }
            }
        });

        View btCollage = findViewById(R.id.btCollage);
        btCollage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    Intent intent = new Intent(MainActivity.this, CollagePickerActivity.class);
                    startActivity(intent);
                }
            }
        });

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

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                createImageFromBitmap(bitmap);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            photoURI = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            createImageFromBitmap(bitmap);
            startActivity(intent);
        }
    }


    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private void createDirectory() {
        //File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(getExternalFilesDir(null), "PhotoEditor");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

}

