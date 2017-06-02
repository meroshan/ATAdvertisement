package cpm.advancetect.atadvertisement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 02-Jun-17.
 */

public class AdActivity extends AppCompatActivity {
    TextView text;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private String center = "";
    private List<String> items = new ArrayList<>();
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        //FireBase initialization
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();

        text = (TextView) findViewById(R.id.text);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (getIntent().getExtras() != null) {
            center = getIntent().getExtras().getString("selected_center");
            text.setText(center);
            mDatabaseRef = mDatabaseRef.child(center);
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    items.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                        };
                        //List<String> list1 = postSnapshot.getValue(genericTypeIndicator);
                        String temp = postSnapshot.getValue().toString();
                        Log.d("data list of node ", temp);
                        //if (!list.contains(center))
                        //list.add(postSnapshot.getKey());
                        center = center.concat("\n" + temp);
//                        StorageReference storageReference = mDatabaseRef.get

                        if (!temp.toLowerCase().equals(null)) {
                            items.add(temp);
                        }
                    }
                    text.setText(center);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast.makeText(this, "" + mDatabaseRef.getKey(), Toast.LENGTH_SHORT).show();
        }


    }
}
