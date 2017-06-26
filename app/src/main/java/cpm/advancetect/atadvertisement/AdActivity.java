package cpm.advancetect.atadvertisement;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import static cpm.advancetect.atadvertisement.MyApplication.MY_PERMISSIONS_EXTERNAL_STORAGE;
import static cpm.advancetect.atadvertisement.MyApplication.imageListLocal;
import static cpm.advancetect.atadvertisement.MyApplication.mDatabaseRef;
import static cpm.advancetect.atadvertisement.MyApplication.sharedPreferences;
import static cpm.advancetect.atadvertisement.MyApplication.videoListLocal;

/**
 * Created by rahul on 05-Jun-17.
 */

public class AdActivity extends Activity {
    public static final String ACTION_RESP = "com.mamlambo.intent.action.MESSAGE_PROCESSED";
    public static final String MAIN_DIRECTORY = "Digital Signage";
    public static final String VIDEOS_DIRECTORY = "Videos";
    public static final String IMAGES_DIRECTORY = "Images";
    public static final String MARQUEE_TEXT = "marquee_text";
    //final String STORAGE_PATH = Environment.getExternalStorageDirectory().toString() + "/" + "Digital Signage/";

    public ArrayList<DataModel> videoListServer = new ArrayList<>();
    public ArrayList<DataModel> imageListServer = new ArrayList<>();

    protected ArrayList<String> serverFileList = new ArrayList<>();
    protected ArrayList<String> localFileList = new ArrayList<>();
    protected ArrayList<String> urlList = new ArrayList<>();
    int countVideo = 0;
    ImageView imageView;
    File localImageDir;
    File localVideoDir;
    Handler h = new Handler();
    Runnable r;
    TextView marqueeText;
    private String selectedCenter;
    private String tempCenter;
    private DatabaseReference databaseReference;
    private ResponseReceiver receiver;
    private VideoView videoView;

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();
        playVideos();
        //playImages();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final View decor = getWindow().getDecorView();
        decor.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                android.util.Log.d("d", "onSystemUiVisibilityChange");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
                    }
                }, 1500);
            }
        });

        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        setContentView(R.layout.activity_ad);

        videoView = (VideoView) findViewById(R.id.videoView);
        imageView = (ImageView) findViewById(R.id.imageView);
        marqueeText = (TextView) findViewById(R.id.marqueeText);
        marqueeText.setSelected(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            localImageDir = createLocalDirectory(IMAGES_DIRECTORY);
            localVideoDir = createLocalDirectory(VIDEOS_DIRECTORY);

            getLocalFileList(IMAGES_DIRECTORY, 0);
            getLocalFileList(VIDEOS_DIRECTORY, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_EXTERNAL_STORAGE);
            return;
        }

        String marquee_text = sharedPreferences.getString(MARQUEE_TEXT, "");
        if (!marquee_text.equals("")) {
            marqueeText.setVisibility(View.VISIBLE);
            marqueeText.setText(marquee_text + " " + marquee_text);
        } else
            marqueeText.setVisibility(View.GONE);

        IntentFilter filter = new IntentFilter(ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        tempCenter = selectedCenter = sharedPreferences.getString("current_center", "NULL");

        databaseReference = mDatabaseRef.child(selectedCenter);


        playVideos();
        playImages();

        getFileListFromServer();

//        Glide.with(context)
//                .load(new String[]{"anc"})
//                .into(imageView);

        Intent i = new Intent(this, LayoutSelector.class);
        //startActivityForResult(i, 1);

        //videoView.setVideoPath("/storage/emulated/0/Digital Signage/g.MP4");
        //videoView.setMediaController(new MediaController(this));
        //videoView.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                //int id = Integer.parseInt(data.getStringExtra("id"));
                String id = data.getStringExtra("id");
                if (id.equals("one")) {
                    Toast.makeText(getApplicationContext(), "Layout 1 selected", Toast.LENGTH_SHORT).show();
                } else if (id.equals("two")) {
                }
                //Toast.makeText(context, "Layout 2 selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playImages() {
        r = new Runnable() {
            int i = 0;

            //9780286280
            @Override
            public void run() {
                // do stuff then
                // can call h again after work!

                if (imageListLocal.size() > 0) {
                    File f = new File(imageListLocal.get(i));
                    Glide.with(AdActivity.this)
                            .load(f)
                            .into(imageView);
                    i++;
                    if (i > imageListLocal.size() - 1) {
                        i = 0;
                    }
                    Log.d("ImageHandler", "Showing image " + i + " size=" + imageListLocal.size());
                } else Log.d(this.getClass().getName(), "imageList is empty");
                h.postDelayed(this, 4000);
            }
        };

        h.postDelayed(r, 500);
    }

    private void playVideos() {
//        if (videoView == null) {
//            Log.d("ImageHandler", "videoView is null");
//            return;
//        }
        if (videoListLocal.size() <= 0) {
            Log.d(this.getClass().getName(), "video playlist size is zero");
            //return;
        } else {
            videoView.setVideoPath(videoListLocal.get(0));
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
                }
            });
        }
    }

    void getFileListFromServer() {
        DatabaseReference textRef = databaseReference.child("Text");
        textRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String marquee_text = dataSnapshot.getValue(String.class);
                    Log.d("marquee text", marquee_text);
                    sharedPreferences.edit().putString(MARQUEE_TEXT, marquee_text).apply();
                    marqueeText.setVisibility(View.VISIBLE);
                    marqueeText.setText(marquee_text + " " + marquee_text);
                } else marqueeText.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO
            }
        });

        DatabaseReference tempRef = databaseReference.child("Images");
        tempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //serverFileList.clear();
                //urlList.clear();

                if (dataSnapshot.exists()) {
                    imageListServer.clear();
                    for (DataSnapshot t : dataSnapshot.getChildren()) {
                        Log.d(this.getClass().getName(), "t_key = " + t.getKey() + " t_value = " + t.getValue());
                        DataModel model = t.getValue(DataModel.class);
                        //Log.d("dataModel", model.getname() + " " + model.geturl());
                        //Log.d("dataModel", model.name + " " + model.url);
                        //serverFileList.add(model.name);
                        //urlList.add(model.url);
                        if (!imageListServer.contains(model)) {
                            imageListServer.add(model);
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

        tempRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("onChildEventListener", dataSnapshot.getKey() + " " + dataSnapshot.getValue());
                DataModel dataModel = dataSnapshot.getValue(DataModel.class);
                getLocalFileList(IMAGES_DIRECTORY, 0);
                String fileDir = (new File(localImageDir, dataModel.name)).getAbsolutePath();
                Log.d("onChildEventListener", "fileDir:" + fileDir);
                if (imageListLocal.contains(fileDir)) {
                    Log.d("onChildEventListener", "delete " + fileDir);
                    if (deleteFile(fileDir)) {
                        Log.d("onChildEventListener", "successfully deleted");
                        imageListLocal.remove(fileDir);
                    } else {
                        Log.d("onChildEventListener", "Unable to delete file");
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

        DatabaseReference tempRef1 = databaseReference.child("Videos");
        tempRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //serverFileList.clear();
                //urlList.clear();

                for (DataSnapshot t : dataSnapshot.getChildren()) {
                    videoListServer.clear();
                    Log.d(this.getClass().getName(), "t_key = " + t.getKey() + " t_value = " + t.getValue());
                    DataModel model = t.getValue(DataModel.class);
                    //Log.d("dataModel", model.getname() + " " + model.geturl());
                    Log.d("dataModel_video", model.name + " " + model.url);
                    //serverFileList.add(model.name);
                    //urlList.add(model.url);
                    videoListServer.add(model);
                }
                downloadFiles(1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean deleteFile(String fileDir) {
        File file = new File(fileDir);
        return file.delete();
    }

    public File createLocalDirectory(String dir) {
        //file = null;
        //File fileDir = new File(getExternalFilesDir(null), MAIN_DIRECTORY);
        File fileDir = new File(Environment.getExternalStorageDirectory(), MAIN_DIRECTORY);
        fileDir = new File(fileDir, dir);

        if (!fileDir.exists()) {
            if (!fileDir.mkdir()) {
                // Toast.makeText(AdActivity.this, "unable to create folder", Toast.LENGTH_SHORT).show();
                Log.d("create_folder", "unable to create folder " + fileDir.getAbsolutePath());
            } else {
                //Toast.makeText(AdActivity.this, "folder created successfully", Toast.LENGTH_SHORT).show();
                Log.d("create_folder", "folder created successfully " + fileDir.getAbsolutePath());
            }
        } else {
            //Toast.makeText(AdActivity.this, "folder already exists", Toast.LENGTH_SHORT).show();
            Log.d("create_folder", "folder already exists " + fileDir.getAbsolutePath());
        }
        return fileDir;
    }

    public void getLocalFileList(String dir, int subDirId) {
        File home = createLocalDirectory(dir);

        Log.d(this.getClass().getName(), "storage path " + home.getAbsolutePath());

        File[] listFiles = home.listFiles();

        if (listFiles != null && listFiles.length > 0) {
            if (subDirId == 0)
                for (File tempFile : listFiles) {
                    if (!imageListLocal.contains(tempFile.getAbsolutePath()))
                        imageListLocal.add(tempFile.getAbsolutePath());
                }
            else
                for (File tempFile : listFiles) {
                    videoListLocal.add(tempFile.getAbsolutePath());
                }
        }

        //Log.d(this.getClass().getName(), "playList " + videoList);
    }

//    public void getLocalFileList(String center) {
//        if (STORAGE_PATH == null)
//            return;
//        File home = new File(STORAGE_PATH);
//        Log.d(this.getClass().getName(), "storage path " + home.getAbsolutePath());
//        File[] listFiles = home.listFiles();
//        if (listFiles != null && listFiles.length > 0) {
//            for (File file1 : listFiles) {
//                if (file1.isDirectory())
//                    scanDirectory(file1);
//                else
//                    localFileList.add(file1.getAbsolutePath());
//            }
//        }
//        Log.d(this.getClass().getName(), "playList " + localFileList);
//    }


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
                Log.d("continuee", "download files,image " + fileName);
                if (imageListLocal.contains(localImageDir.getAbsolutePath() + "/" + fileName))
                    continue;
            } else {
                fileName = videoListServer.get(i).name;
                url = videoListServer.get(i).url;
                Log.d("continuee", "download files,video " + fileName);
                if (videoListLocal.contains(localVideoDir.getAbsolutePath() + "/" + fileName))
                    continue;
            }

            Log.d(this.getLocalClassName(), "+continuee " + fileName);

            Intent intent = new Intent(AdActivity.this, DownloadService.class);
            intent.putExtra("fileName", fileName);
            intent.putExtra("url", url);
            intent.putExtra("dir", directory);
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        h.removeCallbacks(r);
        Log.d("onDestroy", "unregisterReceiver");
    }

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

