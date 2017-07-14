package com.advancetech.digitalsignage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

/**
 * Created by rahul on 03-Jun-17.
 * <p>
 * Global variables and constants.
 */

public class MyApplication extends Application {
    public static final String USER_IDENTIFIER = "Ambika";
    public static final int REQUEST_CODE_SETTINGS = 1;
    public static final String ACTION_RESP = "com.mamlambo.intent.action.MESSAGE_PROCESSED";
    public static final String MAIN_DIRECTORY = "Digital Signage";
    public static final String VIDEOS_DIRECTORY = "Videos";
    public static final String IMAGES_DIRECTORY = "Images";
    public static final int MY_PERMISSIONS_EXTERNAL_STORAGE = 1;
    public static final String HEADER_TEXT = "header_text";
    public static final String HEADER_TEXT_SIZE = "header_text_size";
    public static final String HEADER_TEXT_HINT = "Enter some text here";
    public static final String FOOTER_TEXT = "footer_text";
    public static final String FOOTER_TEXT_SIZE = "footer_text_size";
    public static final String FOOTER_TEXT_HINT = "Enter some text here";
    public static final String HEADER_COLOR = "header_color";
    public static final String FOOTER_COLOR = "footer_color";
    public static MyApplication instance = null;
    public static DatabaseReference mDatabaseRef;
    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;
    public static SharedPreferences sharedPreferences;
    public static ArrayList<String> videoListLocal = new ArrayList<>();
    public static ArrayList<String> imageListLocal = new ArrayList<>();

    public static Context getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }

    /**
     * sets fabric user identifier for tracking from where log is generated.
     */
    public static void setFabricUserIdentifier() {
        String selectedCenter = sharedPreferences.getString("current_center", "NULL");
        if (selectedCenter.endsWith("NULL"))
            Crashlytics.setUserIdentifier(USER_IDENTIFIER);
        else
            Crashlytics.setUserIdentifier(USER_IDENTIFIER + "-" + selectedCenter);
    }

    /**
     * Global variable initialization
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseStorage = FirebaseStorage.getInstance("gs://advtech-e98fc.appspot.com");
        mStorageReference = mFirebaseStorage.getReference();
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        setFabricUserIdentifier();
    }
}
