package cpm.advancetect.atadvertisement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by rahul on 17-Jun-17.
 */

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d("boot_completed_dss", "boot completed");
//        Toast.makeText(context, "boot completed", Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("boot_completed_ds", "boot completed");
            //Toast.makeText(context, "boot_completed_ds " + "boot completedd", Toast.LENGTH_SHORT).show();
            Intent serviceIntent = new Intent(context, MainActivity.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serviceIntent);
        }
    }
}
