package com.example.photoeditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;
    private boolean onlyUsersImages = true;
    private Button btUsersImages;
    private Button btAllImages;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        mRecyclerView = findViewById(R.id.recycler_view);
        btUsersImages = findViewById(R.id.btUsersImages);
        btAllImages = findViewById(R.id.btAllImages);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        create();

        btAllImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    onlyUsersImages = false;
                    create();
                }
            }
        });

        btUsersImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    onlyUsersImages = true;
                    create();
                }
            }
        });

    }

    private void create() {
        mUploads.clear();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    assert upload != null;
                    if (onlyUsersImages) {
                        if (Objects.equals(upload.getUser(), FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            mUploads.add(upload);
                        }
                    } else {
                        if (upload.getIsPublic() || Objects.equals(upload.getUser(), FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                            mUploads.add(upload);
                        }
                    }
                }
                mAdapter = new ImageAdapter(ImagesActivity.this, mUploads);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(ImagesActivity.this);
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Log.d("=======", String.valueOf(position));
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        Log.d("=======1", String.valueOf(selectedKey));
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        Log.d("=======2", String.valueOf(imageRef));
    }

    File localFile;

    @Override
    public void onDownloadClick(int position) {

        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File file = new File(path, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");

        Upload selectedItem = mUploads.get(position);
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ImagesActivity.this, "Pobrano ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(ImagesActivity.this, "Błąd pobierania", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLikeClick(int position) {
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        Log.d("=======", String.valueOf(selectedKey));
        // Log.d("=======" , String.valueOf(mDatabaseRef.child(selectedKey + "/likesNumber").getValue("YourDateHere")));
        Log.d("=======2", String.valueOf(mUploads.get(position).getLikesNumber()));
        mDatabaseRef.child(selectedKey + "/likesNumber").setValue(mUploads.get(position).getLikesNumber() + 1);
        create();
    }

}