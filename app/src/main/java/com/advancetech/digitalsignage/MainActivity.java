package com.advancetech.digitalsignage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

import static com.advancetech.digitalsignage.MyApplication.mDatabaseRef;
import static com.advancetech.digitalsignage.MyApplication.setFabricUserIdentifier;
import static com.advancetech.digitalsignage.MyApplication.sharedPreferences;

/**
 * Fetch Centre list from the fireBase and showing in a listView.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * arrayList of centers
     */
    ArrayList<String> list = new ArrayList<>();

    /**
     * list adapter for list
     */
    ArrayAdapter<String> adapter;

    /**
     * Tag for logging purpose
     */
    String TAG = "main_activity";

    ProgressDialog dialog;

    /**
     * listView to show Centres
     */
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new Answers());
        setContentView(R.layout.activity_main);

        // initializing FireBase reference & listView
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.listView);

        // A progress dialog while centre list is downloading from fireBase
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Centre list is loading, Please wait!");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        /*
         * this checks if the centre name is stored in the sharedPreference.
         * If center name is saved then start AdActivity and finish this activity
         * so that user can't come back to this activity.
         */
        if (!sharedPreferences.getString("current_center", "NULL").equals("NULL")) {
            dialog.cancel();
            startActivity(new Intent(MainActivity.this, AdActivity.class));
            finish();
        }

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        // when user selects a centre, it is saved in sharePreferences for further use in other activities
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String center = parent.getItemAtPosition(position).toString();
                        Log.d("selected center", center);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (editor.putString("current_center", center).commit()) {
                            setFabricUserIdentifier();
                            Intent intent = new Intent(MainActivity.this, AdActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d(this.getClass().getName(), "Unable to save center name in sharedPreferences");
                            Toast.makeText(MainActivity.this, "Unable to save center name in sharedPreferences", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // addValueListener on centre list
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                dialog.cancel();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
//                    };
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
