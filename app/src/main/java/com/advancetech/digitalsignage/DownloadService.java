package com.advancetech.digitalsignage;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import io.fabric.sdk.android.Fabric;

import static com.advancetech.digitalsignage.MyApplication.ACTION_RESP;
import static com.advancetech.digitalsignage.MyApplication.IMAGES_DIRECTORY;
import static com.advancetech.digitalsignage.MyApplication.VIDEOS_DIRECTORY;

/**
 * Created by rahul on 05-Jun-17.
 * <p>
 * Background service for downloading files one by one.
 */

public class DownloadService extends IntentService {

    String directory;

    /**
     * Constructor
     */
    public DownloadService() {
        super(DownloadService.class.getName());
    }

    /**
     * This method is called when this service is started.
     * <p>
     * First, fetch the file name, directory and its URL link.
     * Add suffix ".rr" to the file so that it can be differentiated from previously
     * downloaded files and after successful download remove this suffix.
     * And then start file downloading.
     *
     * @param intent intent passed while starting this service. This intent contains the file URL and file name.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Fabric.with(this, new Crashlytics());
        Log.d(this.getClass().getName(), "starting service");

        assert intent != null;
        String url = intent.getStringExtra("url");

        // add suffix ".rr" to file so it can be differentiated from downloaded files and remove this suffix after successful download.
        String fileName = intent.getStringExtra("fileName") + ".rr";
        directory = intent.getStringExtra("dir");

        File mfile = createFolder(new File(Environment.getExternalStorageDirectory(), "Digital Signage"));

        if (mfile == null) {
            Log.d(this.getClass().getName(), "Cannot create folder, possibly storage permission is not granted!");
            return;
        } else {
            Log.d(this.getClass().getName(), mfile.getAbsolutePath() + " folder successfully created");
        }

        //File file = new File(mfile, fileName);
        File videoFileDir = createFolder(new File(mfile, VIDEOS_DIRECTORY));
        File imageFileDir = createFolder(new File(mfile, IMAGES_DIRECTORY));

        if (TextUtils.isEmpty(url)) {
            Log.d(this.getClass().getName(), "Empty URL " + url);
        } else {
            Log.d(this.getClass().getName(), "URL is not empty");
        }

        try {
            Log.d("startdownload", fileName);
            if (directory.equals("Images"))
                downloadFileFromURl(url, new File(imageFileDir, fileName));
            else
                downloadFileFromURl(url, new File(videoFileDir, fileName));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(this.getClass().getName(), "malformed url, can't download!");
        }
    }

    /**
     * Create a new directory
     *
     * @param file directory name to create.
     * @return file reference
     */
    public File createFolder(File file) {
        //File file = new File(Environment.getExternalStorageDirectory(), "Digital Signage");
        //file = new File(file.getAbsolutePath());
        if (!file.exists()) {
            if (!file.mkdir()) {
                Log.d(this.getClass().getName(), file.getAbsolutePath() + " Unable to create folder");
                return null;
            } else {
                Log.d(this.getClass().getName(), file.getAbsolutePath() + "Folder successfully created");
            }
        } else {
            Log.d(this.getClass().getName(), file.getAbsolutePath() + "Folder already exists");
        }
        return file;
    }

    /**
     * Downloads files from URL
     *
     * @param url1 file url to download
     * @param file file location reference
     * @throws MalformedURLException
     */
    public void downloadFileFromURl(String url1, File file) throws MalformedURLException {
        URL url = new URL(url1);
        int count;
        try {
            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] data = new byte[1024];
            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            sendBroadcast(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * After successful download this send broadcast to other activities with file details
     *
     * @param fileURI file location
     */
    public void sendBroadcast(String fileURI) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        //broadcastIntent.putExtra("filename", filename);
        broadcastIntent.putExtra("dir", directory);
        broadcastIntent.putExtra("fileURI", fileURI);
        sendBroadcast(broadcastIntent);
    }
}
