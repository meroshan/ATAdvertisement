package cpm.advancetect.atadvertisement;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private DatabaseReference mRef;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRef = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String center = parent.getItemAtPosition(position).toString();
                        Log.d("selected center", center);
                        Intent intent = new Intent(MainActivity.this, AdActivity.class);
                        intent.putExtra("selected_center", center);
                        startActivity(intent);
                    }
                }
        );

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                //list.add("select center");
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                    };
                    Log.d("data list of node ", postSnapshot.getKey());
                    //                 List<String> list1 = postSnapshot.getValue(genericTypeIndicator);
                    //+ list1.toString() + " " + list1.size());
                    String center = postSnapshot.getKey();
                    if (!list.contains(center))
                        list.add(postSnapshot.getKey());
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
                //adapter.notifyDataSetChanged();
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "unable to fetch list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String center = parent.getItemAtPosition(position).toString();
        if (!center.toLowerCase().equals("select center")) {
            Intent intent = new Intent(MainActivity.this, AdActivity.class);
            intent.putExtra("selected_center", center);
            startActivity(intent);
            //Toast.makeText(this, "" + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
        }
    }

    //@Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "nothing selected", Toast.LENGTH_SHORT).show();
    }
}
