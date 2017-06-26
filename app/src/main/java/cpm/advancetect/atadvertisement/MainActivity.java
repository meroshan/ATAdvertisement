package cpm.advancetect.atadvertisement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static cpm.advancetect.atadvertisement.MyApplication.MY_PERMISSIONS_EXTERNAL_STORAGE;
import static cpm.advancetect.atadvertisement.MyApplication.mDatabaseRef;
import static cpm.advancetect.atadvertisement.MyApplication.sharedPreferences;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    String TAG = "main_activity";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //startActivity(new Intent(this, MainActivity.class));
        Log.d(TAG, "onCreate called");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.listView);

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_EXTERNAL_STORAGE);
            return;
        }

        if (!sharedPreferences.getString("current_center", "NULL").equals("NULL"))
            startActivity(new Intent(MainActivity.this, AdActivity.class));

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted");
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Please allow storage permission");
                Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
