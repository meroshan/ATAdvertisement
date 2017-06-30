package cpm.advancetect.atadvertisement;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static cpm.advancetect.atadvertisement.MyApplication.FOOTER_TEXT;
import static cpm.advancetect.atadvertisement.MyApplication.FOOTER_TEXT_SIZE;
import static cpm.advancetect.atadvertisement.MyApplication.HEADER_TEXT;
import static cpm.advancetect.atadvertisement.MyApplication.HEADER_TEXT_SIZE;
import static cpm.advancetect.atadvertisement.MyApplication.sharedPreferences;

/**
 * Created by rahul on 28-Jun-17.
 */

public class TextEditor extends AppCompatActivity {
    EditText headerText, footerText, headerTextSize, footerTextSize;
    Button save;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        headerText = (EditText) findViewById(R.id.header_text);
        headerTextSize = (EditText) findViewById(R.id.header_text_size);
        footerText = (EditText) findViewById(R.id.footer_text);
        footerTextSize = (EditText) findViewById(R.id.footer_text_size);
        save = (Button) findViewById(R.id.save);

        headerText.setText(sharedPreferences.getString(HEADER_TEXT, ""));
        headerTextSize.setText(sharedPreferences.getString(HEADER_TEXT_SIZE, "24"));
        footerText.setText(sharedPreferences.getString(FOOTER_TEXT, ""));
        footerTextSize.setText(sharedPreferences.getString(FOOTER_TEXT_SIZE, "24"));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String header = sharedPreferences.getString(HEADER_TEXT, "");
//                String headerSize = sharedPreferences.getString(HEADER_TEXT_SIZE, "");
//                String footer = sharedPreferences.getString(FOOTER_TEXT, "");
//                String footerSize = sharedPreferences.getString(FOOTER_TEXT_SIZE, "");

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
                    Toast.makeText(TextEditor.this, "Settings saved", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(TextEditor.this, "Unable to save settings", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
