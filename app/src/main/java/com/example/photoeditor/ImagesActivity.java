package com.example.photoeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImagesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;
    private boolean onlyUsersImages = true;
    private Button btUsersImages;
    private Button btAllImages;
    private ImageButton likeIcon;
    private TextView tvLikesNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        mRecyclerView = findViewById(R.id.recycler_view);
        btUsersImages = findViewById(R.id.btUsersImages);
        btAllImages = findViewById(R.id.btAllImages);
        mRecyclerView = findViewById(R.id.recycler_view);
/*        likeIcon = findViewById(R.id.btLike);
        tvLikesNumber = findViewById(R.id.likesNumber);*/
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();
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

/*        likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    likeIcon.setBackgroundResource(R.drawable.liked);
                    int likesNumber = Integer.parseInt(tvLikesNumber.getText().toString()) + 1;
                    tvLikesNumber.setText(likesNumber);
                }
            }
        });*/

    }

    private void create() {
        mUploads.clear();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
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
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }
}