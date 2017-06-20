package cpm.advancetect.atadvertisement;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static cpm.advancetect.atadvertisement.AdActivity.ACTION_RESP;
import static cpm.advancetect.atadvertisement.AdActivity.IMAGES_DIRECTORY;
import static cpm.advancetect.atadvertisement.AdActivity.VIDEOS_DIRECTORY;

/**
 * Created by rahul on 05-Jun-17.
 */

public class DownloadService extends IntentService {

    String directory;

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(this.getClass().getName(), "starting service");

        assert intent != null;
        String url = intent.getStringExtra("url");
        String fileName = intent.getStringExtra("fileName");
        directory = intent.getStringExtra("dir");

        File mfile = createFolder(new File(Environment.getExternalStorageDirectory(), "Digital Signage"));

        if (mfile == null) {
            Log.d(this.getClass().getName(), "Cannot create folder, possibly storage permission is npt granted!");
            return;
        } else {
            Log.d(this.getClass().getName(), mfile.getAbsolutePath() + " folder successfully created");
        }

        //File file = new File(mfile, fileName);
        File videoFileDir = createFolder(new File(mfile, VIDEOS_DIRECTORY));
        File imageFileDir = createFolder(new File(mfile, IMAGES_DIRECTORY));

        if (TextUtils.isEmpty(url)) {
            //Toast.makeText(this, "" + url, Toast.LENGTH_SHORT).show();
            Log.d(this.getClass().getName(), "Empty URL " + url);
        } else {
            Log.d(this.getClass().getName(), "URL is not empty");
        }

        //SystemClock.sleep(2000);

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

    public void downloadFileFromURl(String url1, File file) throws MalformedURLException {
        URL url = new URL(url1);
        int count;
        try {
            URLConnection connection = url.openConnection();

            //connection.setRequestProperty();
            connection.connect();

            //int fileLength = connection.getContentLength();

            InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] data = new byte[1024];
            //long total = 0;
            while ((count = inputStream.read(data)) != -1) {
                //total += count;
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
