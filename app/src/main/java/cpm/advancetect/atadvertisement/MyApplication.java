package cpm.advancetect.atadvertisement;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by rahul on 03-Jun-17.
 */

public class MyApplication extends Application {
    public static MyApplication instance = null;
    public static DatabaseReference mDatabaseRef;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static SharedPreferences sharedPreferences;
    public static ArrayList<String> videoListLocal = new ArrayList<>();
    public static ArrayList<String> imageListLocal = new ArrayList<>();
    public static String marqueeText;

    public static Context getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseStorage = FirebaseStorage.getInstance("gs://advtech-e98fc.appspot.com");
        mStorageReference = mFirebaseStorage.getReference();
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        //marqueeText = "";
        Log.d(this.getClass().getName(), "onCreate called");
    }
}
