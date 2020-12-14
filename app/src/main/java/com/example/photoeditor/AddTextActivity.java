package com.example.photoeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AddTextActivity extends AppCompatActivity {
    int red = 255;
    int green = 255;
    int blue = 255;
    int opacity = 255;
    Bitmap bitmap;
    ArrayList<Bitmap> history = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        Intent intent = getIntent();
        history = intent.getParcelableArrayListExtra("history");
        bitmap = history.get(history.size() - 1);
        final EditText textPlaceholder = (EditText) findViewById(R.id.etText);
        disableEmojiInTitle(textPlaceholder);
        showKeyboard(textPlaceholder, AddTextActivity.this);

        View btPreviousScreen = findViewById(R.id.btPreviousScreen);
        btPreviousScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTextActivity.this.finish();
            }
        });

        View btConfirm = findViewById(R.id.btConfirm);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTextActivity.this, EditorActivity.class);
                intent.putParcelableArrayListExtra("history", history);
                startActivity(intent);
                finish();
            }
        });

        SeekBar sbTextSize = findViewById(R.id.sbTextSize);
        sbTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textPlaceholder.setTextSize(progress);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(AddTextActivity.this);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.color_picker, viewGroup, false);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                SeekBar sbRed = (SeekBar) alertDialog.findViewById(R.id.sbRed);
                SeekBar sbGreen = (SeekBar) alertDialog.findViewById(R.id.sbGreen);
                SeekBar sbBlue = (SeekBar) alertDialog.findViewById(R.id.sbBlue);
                SeekBar sbOpacity = (SeekBar) alertDialog.findViewById(R.id.sbOpacity);
                sbRed.setProgress(red);
                sbGreen.setProgress(green);
                sbBlue.setProgress(blue);
                sbOpacity.setProgress(opacity);
                final View ivColorPreview = alertDialog.findViewById(R.id.ivColorPreview);
                ivColorPreview.setBackgroundColor(Color.rgb(red, green, blue));
                View btConfirmColor = alertDialog.findViewById(R.id.btConfirmColor);
                btConfirmColor.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        textPlaceholder.setTextColor(Color.rgb(red, green, blue));
                        alertDialog.dismiss();
                    }
                });

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

    }

    public static void showKeyboard(EditText mEtSearch, Context context) {

        mEtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void disableEmojiInTitle(EditText editText) {
        InputFilter emojiFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int index = start; index < end - 1; index++) {
                    int type = Character.getType(source.charAt(index));

                    if (type == Character.SURROGATE) {
                        return "";
                    }
                }
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{emojiFilter});
    }
}
