package com.advancetech.digitalsignage;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

import static com.advancetech.digitalsignage.MyApplication.ACTION_RESP;
import static com.advancetech.digitalsignage.MyApplication.FOOTER_TEXT;
import static com.advancetech.digitalsignage.MyApplication.FOOTER_TEXT_HINT;
import static com.advancetech.digitalsignage.MyApplication.FOOTER_TEXT_SIZE;
import static com.advancetech.digitalsignage.MyApplication.HEADER_TEXT;
import static com.advancetech.digitalsignage.MyApplication.HEADER_TEXT_HINT;
import static com.advancetech.digitalsignage.MyApplication.HEADER_TEXT_SIZE;
import static com.advancetech.digitalsignage.MyApplication.IMAGES_DIRECTORY;
import static com.advancetech.digitalsignage.MyApplication.MAIN_DIRECTORY;
import static com.advancetech.digitalsignage.MyApplication.MY_PERMISSIONS_EXTERNAL_STORAGE;
import static com.advancetech.digitalsignage.MyApplication.REQUEST_CODE_SETTINGS;
import static com.advancetech.digitalsignage.MyApplication.VIDEOS_DIRECTORY;
import static com.advancetech.digitalsignage.MyApplication.imageListLocal;
import static com.advancetech.digitalsignage.MyApplication.mDatabaseRef;
import static com.advancetech.digitalsignage.MyApplication.setFabricUserIdentifier;
import static com.advancetech.digitalsignage.MyApplication.sharedPreferences;
import static com.advancetech.digitalsignage.MyApplication.videoListLocal;

/**
 * Created by rahul on 05-Jun-17.
 * <p>
 * This activity is for displaying contents like images, texts & videos.
 */

public class AdActivity extends Activity {

    /**
     * stores video files URL of cloud server
     */
    public ArrayList<DataModel> videoListServer = new ArrayList<>();

    /**
     * stores URL of images to download
     */
    public ArrayList<DataModel> imageListServer = new ArrayList<>();
    public String TAG = "AdActivity";

    /**
     * local files
     */
    protected ArrayList<String> localFileList = new ArrayList<>();

    /**
     * video counter
     */
    int countVideo = 0;

    /**
     * image reference
     */
    ImageView imageView;

    /**
     * local image file directory
     */
    File localImageDir;

    /**
     * local video file directory
     */
    File localVideoDir;

    /**
     * this handler is used to show list of images at some fixed interval
     */
    Handler h = new Handler();
    Runnable r;

    /**
     * header and footer text reference
     */
    TextView header, footer;
    View decor;
    private DatabaseReference databaseReference;
    /**
     * receiver for starting a new background download.
     * <p>
     * Whenever the download of a file is finished from {@link DownloadService},
     * a message is broadcast with file name and its directory,
     * indicating download has been finished.
     */
    private ResponseReceiver receiver;
    private VideoView videoView;

    @Override
    protected void onResume() {
        super.onResume();
        playVideos();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // for hiding status bar and navigation buttons
        decor = getWindow().getDecorView();
        decor.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                android.util.Log.d("d", "onSystemUiVisibilityChange");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        setSystemUIFlag();
                    }
                }, 2000);
            }
        });
        setSystemUIFlag();
        Fabric.with(this, new Crashlytics());

        //current centre which user has selected
        String selectedCenter = sharedPreferences.getString("current_center", "NULL");
        databaseReference = mDatabaseRef.child(selectedCenter);

        setFabricUserIdentifier();
        setContentView(R.layout.activity_ad);

        videoView = (VideoView) findViewById(R.id.videoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        header = (TextView) findViewById(R.id.header_text);
        footer = (TextView) findViewById(R.id.footer_text);

        initializeTexts();

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AdActivity.this, Settings.class), REQUEST_CODE_SETTINGS);
            }
        });

        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AdActivity.this, Settings.class), REQUEST_CODE_SETTINGS);
            }
        });

        //checks storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            intit();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_EXTERNAL_STORAGE);
            //return;
        }

        // register receiver
        IntentFilter filter = new IntentFilter(ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
    }

    // initialization of local images and videos form local storage
    public void intit() {
        localImageDir = createLocalDirectory(IMAGES_DIRECTORY);
        localVideoDir = createLocalDirectory(VIDEOS_DIRECTORY);

        getLocalFileList(IMAGES_DIRECTORY, 0);
        getLocalFileList(VIDEOS_DIRECTORY, 1);

        playVideos();
        playImages();

        getFileListFromServer();
    }

    /**
     * hide status bar and navigation buttons
     */
    public void setSystemUIFlag() {
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * this method is called when storage permission is granted
     * so here we can initialize the important file reference
     * {@link AdActivity#localVideoDir} & {@link AdActivity#localImageDir}
     *
     * @param requestCode  request code
     * @param permissions  required permissions
     * @param grantResults grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                intit();
            } else {
                Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * receive a result from other activities here
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                initializeTexts();
            }
        }
    }

    /**
     * footer and header text initialization
     */
    public void initializeTexts() {
        String headerText = sharedPreferences.getString(HEADER_TEXT, HEADER_TEXT_HINT);
        int headerSize = Integer.valueOf(sharedPreferences.getString(HEADER_TEXT_SIZE, "0"));
        String footerText = sharedPreferences.getString(FOOTER_TEXT, FOOTER_TEXT_HINT);
        int footerSize = Integer.valueOf(sharedPreferences.getString(FOOTER_TEXT_SIZE, "0"));

        header.setText(headerText);
        if (headerSize != 0)
            header.setTextSize(headerSize);

        footer.setText(footerText);
        if (footerSize != 0)
            footer.setTextSize(footerSize);

        footer.setSelected(true);
    }

    /**
     * to display images in imageView.
     * <p>
     * images are changed at some constant time interval (interval).
     * a handler is used for this.
     */
    private void playImages() {
        final int interval = 4000;
        r = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                // do stuff then
                // can call h again after work!

                if (imageListLocal.size() > 0) {
                    File f = new File(imageListLocal.get(i));
                    Glide.with(AdActivity.this)
                            .load(f)
                            .animate(android.R.anim.fade_in)
                            .into(imageView);
                    i++;
                    if (i > imageListLocal.size() - 1) {
                        i = 0;
                    }
                    Log.d("ImageHandler", "Showing image " + i + " size=" + imageListLocal.size());
                } else Log.d(this.getClass().getName(), "imageList is empty");
                h.postDelayed(this, interval);
            }
        };

        h.postDelayed(r, 500);
    }

    /**
     * play videos from local directory {@link AdActivity#localVideoDir}.
     * <p>
     * Next videos is played after completion on a video.
     */
    private void playVideos() {
        if (videoListLocal.size() <= 0) {
            Log.d(this.getClass().getName(), "video playlist size is zero");
        } else {
            Log.d(TAG, "countVideo " + countVideo + " size=" + videoListLocal.size());
            videoView.setVideoPath(videoListLocal.get(countVideo));
            videoView.start();
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(1);
                    if (countVideo == videoListLocal.size() - 1)
                        countVideo = 0;
                    else countVideo++;

                    videoView.setVideoPath(videoListLocal.get(countVideo));
                    videoView.start();
                    Log.d(TAG, "countVideos " + countVideo + " size=" + videoListLocal.size());
                }
            });
        }
    }

    /**
     * fetch file URLs from fireBase dataBase and saves it into a list.
     * <p>
     * These are different FireBase DatabaseReference for texts, images and videos
     * #textRef - Footer Text reference (see fireBase dataBase)
     * #iamgeRef - Images reference
     * #videoRef - Videos reference
     * <p>
     * This method also implements onDataChanges callbacks for 'Text', 'Videos' & 'Images'.
     * Whenever new items are added to the dataBase, this method is called and download list is updated.
     */
    void getFileListFromServer() {
        DatabaseReference textRef = databaseReference.child("Text");
        textRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String marquee_text = dataSnapshot.getValue(String.class);
                    Log.d("footer text", marquee_text);
                    sharedPreferences.edit().putString(FOOTER_TEXT, marquee_text).apply();
                    footer.setText(marquee_text);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO
            }
        });

        DatabaseReference imageRef = databaseReference.child("Images");
        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    imageListServer.clear();
                    for (DataSnapshot t : dataSnapshot.getChildren()) {
                        Log.d(this.getClass().getName(), " image_key = " + t.getKey() + " image_value = " + t.getValue());
                        DataModel model = t.getValue(DataModel.class);
                        // prevent duplicate elements
                        if (!imageListServer.contains(model)) {
                            imageListServer.add(model);
                            assert model != null;
                            Log.d("dataModel_image", model.name + " " + model.url);
                        }
                    }
                    downloadFiles(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        imageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DataModel model = dataSnapshot.getValue(DataModel.class);
                    //imageListServer.add(model);

                    Log.d("dataModel_image_added", dataSnapshot.getValue().toString());
                    assert model != null;
                    Log.d("dataModel_image_added_k", model.name + " " + model.url);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("dataModel_image", dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                DataModel dataModel = dataSnapshot.getValue(DataModel.class);

                //update local files list before deleting
                getLocalFileList(IMAGES_DIRECTORY, 0);
                assert dataModel != null;
                String fileDir = (new File(localImageDir, dataModel.name)).getAbsolutePath();
                Log.d("dataModel_image", "fileDir:" + fileDir);
                if (imageListLocal.contains(fileDir)) {
                    Log.d("dataModel_image", "delete " + fileDir);
                    if (deleteFile(fileDir)) {
                        Log.d("dataModel_image", "successfully deleted");
                        imageListLocal.remove(fileDir);
                    } else {
                        Log.d("dataModel_image", "Unable to delete file");
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference videoRef = databaseReference.child("Videos");
        videoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    videoListServer.clear();
                    for (DataSnapshot t : dataSnapshot.getChildren()) {
                        Log.d(this.getClass().getName(), "t_key = " + t.getKey() + " t_value = " + t.getValue());
                        DataModel model = t.getValue(DataModel.class);
                        assert model != null;
                        Log.d("dataModel_video", model.name + " " + model.url);
                        if (!videoListServer.contains(model))
                            videoListServer.add(model);
                    }
                    downloadFiles(1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Delete a file
     *
     * @param fileDir address of file to delter
     * @return result of file deletion
     */
    public boolean deleteFile(String fileDir) {
        File file = new File(fileDir);
        return file.delete();
    }

    /**
     * Creates a local directory in external storage
     *
     * @param dir directory name
     * @return directory reference
     */
    public File createLocalDirectory(String dir) {
        File fileDir = new File(Environment.getExternalStorageDirectory(), MAIN_DIRECTORY);
        fileDir = new File(fileDir, dir);

        if (!fileDir.exists()) {
            if (!fileDir.mkdir()) {
                Log.d("create_folder", "unable to create folder " + fileDir.getAbsolutePath());
            } else {
                Log.d("create_folder", "folder created successfully " + fileDir.getAbsolutePath());
            }
        } else {
            Log.d("create_folder", "folder already exists " + fileDir.getAbsolutePath());
        }
        return fileDir;
    }

    /**
     * List the local files from 'Digital Signage' directory
     * into {@link AdActivity#localImageDir} & {@link AdActivity#localVideoDir}
     *
     * @param dir      Directory name
     * @param subDirId id of directory
     */
    public void getLocalFileList(String dir, int subDirId) {
        File home = createLocalDirectory(dir);
        File[] listFiles = home.listFiles();

        if (listFiles != null && listFiles.length > 0) {
            if (subDirId == 0)
                for (File tempFile : listFiles) {
                    if (!tempFile.getAbsolutePath().endsWith(".rr") && !imageListLocal.contains(tempFile.getAbsolutePath())) {
                        imageListLocal.add(tempFile.getAbsolutePath());
                        Log.d(TAG, "image added " + tempFile.getAbsolutePath());
                    }
                }
            else
                for (File tempFile : listFiles) {
                    if (!tempFile.getAbsolutePath().endsWith(".rr") && !videoListLocal.contains(tempFile.getAbsolutePath())) {
                        videoListLocal.add(tempFile.getAbsolutePath());
                        Log.d(TAG, "video added " + tempFile.getAbsolutePath());
                    }
                }
        }
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        localFileList.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * starts a background service for downloading files.
     *
     * @param id directory id{0 = image directory, 1 = video directory}
     */
    public void downloadFiles(int id) {
        int size;
        String fileName;
        String url;
        String directory = id == 0 ? "Images" : "Videos";

        if (id == 0) {
            size = imageListServer.size();
        } else {
            size = videoListServer.size();
        }

        for (int i = 0; i < size; i++) {
            //intent.putExtra("fileName", serverFileList.get(i));

            if (id == 0) {
                fileName = imageListServer.get(i).name;
                url = imageListServer.get(i).url;
                if (imageListLocal.contains(localImageDir.getAbsolutePath() + "/" + fileName))
                    continue;
            } else {
                fileName = videoListServer.get(i).name;
                url = videoListServer.get(i).url;
                if (videoListLocal.contains(localVideoDir.getAbsolutePath() + "/" + fileName))
                    continue;
            }

            Log.d(this.getLocalClassName(), "+download this" + fileName);

            Intent intent = new Intent(AdActivity.this, DownloadService.class);
            intent.putExtra("fileName", fileName);
            intent.putExtra("url", url);
            intent.putExtra("dir", directory);
            startService(intent);
        }
    }

    /**
     * when onStop is called, unregister all receivers and handler callbacks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        h.removeCallbacks(r);
        Log.d("onDestroy", "unregisterReceiver");

    }

    /**
     * This receives a broadcast from {@link DownloadService} when a file has been downloaded.
     */
    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //    TextView textView = (TextView) findViewById(R.id.text);
            //    textView.setText(intent.getStringExtra("url"));
            Log.d("ResponseReceiver", imageListLocal.toString() + " " + imageListLocal.size());
            Log.d("ResponseReceiver", intent.getStringExtra("fileURI"));
            String dir = intent.getStringExtra("dir");
            //String filename = intent.getStringExtra("filename");
            String fileURI = intent.getStringExtra("fileURI");

            // rename downloaded file name to original file name
            if (fileURI.endsWith(".rr")) {
                File from = new File(fileURI);
                fileURI = fileURI.substring(0, fileURI.length() - 3);
                File to = new File(fileURI);
                Log.d(TAG, from.getAbsolutePath() + " renamed to " + to.getAbsolutePath());
                if (from.renameTo(to)) {
                    if (dir.equals(VIDEOS_DIRECTORY) && !videoListLocal.contains(fileURI)) {
                        videoListLocal.add(fileURI);
                        playVideos();
                    } else if (!imageListLocal.contains(fileURI)) {
                        imageListLocal.add(fileURI);
                        //playImages();
                        Log.d("ImageHandler", "ResponseReceiver " + imageListLocal.size());
                    }
                }
            }
        }
    }
}

