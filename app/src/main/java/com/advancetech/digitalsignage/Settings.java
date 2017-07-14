package com.advancetech.digitalsignage;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.colorpicker.ColorPickerDialog;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

import static com.advancetech.digitalsignage.MyApplication.FOOTER_TEXT;
import static com.advancetech.digitalsignage.MyApplication.FOOTER_TEXT_SIZE;
import static com.advancetech.digitalsignage.MyApplication.HEADER_TEXT;
import static com.advancetech.digitalsignage.MyApplication.HEADER_TEXT_SIZE;
import static com.advancetech.digitalsignage.MyApplication.sharedPreferences;

/**
 * Created by rahul on 28-Jun-17.
 * <p>
 * Setting options for changing header & footer text and size.
 */

public class Settings extends AppCompatActivity {
    EditText headerText, footerText, headerTextSize, footerTextSize;
    Button save, color_picker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_text_editor);

        headerText = (EditText) findViewById(R.id.header_text);
        headerTextSize = (EditText) findViewById(R.id.header_text_size);
        footerText = (EditText) findViewById(R.id.footer_text);
        footerTextSize = (EditText) findViewById(R.id.footer_text_size);
        save = (Button) findViewById(R.id.save);
        color_picker = (Button) findViewById(R.id.color_picker_header);

        headerText.setText(sharedPreferences.getString(HEADER_TEXT, ""));
        headerTextSize.setText(sharedPreferences.getString(HEADER_TEXT_SIZE, "24"));
        footerText.setText(sharedPreferences.getString(FOOTER_TEXT, ""));
        footerTextSize.setText(sharedPreferences.getString(FOOTER_TEXT_SIZE, "24"));

        final int colors[] = this.getResources().getIntArray(R.array.color_list);
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();

//        color_picker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                colorPickerDialog.initialize(R.string.color_picker_title, colors, colors[0], 3, colors.length);
//                colorPickerDialog.show(getFragmentManager(), "colorPicker");
//            }
//        });

//        colorPickerDialog.onColorSelected(colors[0]);

        // store header and footer texts from editText into sharedPreferences
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String header = headerText.getText().toString();
                String headerSize = headerTextSize.getText().toString();
                String footer = footerText.getText().toString();
                String footerSize = footerTextSize.getText().toString();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (!header.matches("\\s*")) {
                    editor.putString(HEADER_TEXT, header);
                }
                if (!headerSize.matches("\\s*") && headerSize.matches("\\d*")) {
                    editor.putString(HEADER_TEXT_SIZE, headerSize);
                }
                if (!footer.matches("\\s*")) {
                    editor.putString(FOOTER_TEXT, footer);
                }
                if (!footerSize.matches("\\s*") && footerSize.matches("\\d*")) {
                    editor.putString(FOOTER_TEXT_SIZE, footerSize);
                }
                if (editor.commit()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(Settings.this, "Unable to save settings", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
