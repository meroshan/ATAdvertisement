package cpm.advancetect.atadvertisement;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by rahul on 23-Jun-17.
 */

public class LayoutSelector extends AppCompatActivity implements View.OnClickListener {
    Button button1;
    Button button2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_selector);

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.button1:
                id = 1;
                break;
            case R.id.button2:
                id = 2;
                break;
        }
        Intent intent = new Intent();
        intent.putExtra("id", "one");
        Toast.makeText(this, "id", Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
