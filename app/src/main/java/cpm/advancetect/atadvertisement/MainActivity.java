package cpm.advancetect.atadvertisement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cpm.advancetect.atadvertisement.MyApplication.mDatabaseRef;
import static cpm.advancetect.atadvertisement.MyApplication.sharedPreferences;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    Button folder;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(this.getClass().getName(), "onCreate called");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.listView);

        if (!sharedPreferences.getString("current_center", "NULL").equals("NULL"))
            startActivity(new Intent(MainActivity.this, AdActivity.class));

        folder = (Button) findViewById(R.id.folder);
        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //File file = new File("/storage/emulated/0/ATAdvertisement/");
                File file = new File(getExternalFilesDir(null), "Digital Signage");
                if (!file.exists()) {
                    if (!file.mkdir()) {
                        Toast.makeText(MainActivity.this, "unable to create folder", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "folder successfully created", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "folder already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String center = parent.getItemAtPosition(position).toString();
                        Log.d("selected center", center);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (editor.putString("current_center", center).commit()) {
                            Intent intent = new Intent(MainActivity.this, AdActivity.class);
                            //intent.putExtra("selected_center", center);
                            startActivity(intent);
                        } else {
                            Log.d(this.getClass().getName(), "Unable to save center name in sharedPreferences");
                            Toast.makeText(MainActivity.this, "Unable to save center name in sharedPreferences", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                    };
                    Log.d("Center List ", postSnapshot.getKey());
                    String center = postSnapshot.getKey();
                    if (!list.contains(center)) {
                        list.add(postSnapshot.getKey());
                    }
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "unable to fetch list", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
