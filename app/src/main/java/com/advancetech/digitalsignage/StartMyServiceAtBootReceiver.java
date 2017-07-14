package com.advancetech.digitalsignage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by rahul on 17-Jun-17.
 * <p>
 * This receives 'BOOT_COMPLETED' broadcast which is sent by android system when system is rebooted.
 */

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {
    /**
     * After reboot this method is called which opens {@link MainActivity}
     *
     * @param context context
     * @param intent  intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("boot_completed_ds", "boot completed");
            Intent serviceIntent = new Intent(context, MainActivity.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serviceIntent);
        }
    }
}
