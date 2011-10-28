package com.pedlar.bootanimation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import java.io.File;

public class rmuiReceiver extends BroadcastReceiver {

    public static final String rmUi = "com.pedlar.bootanim.RESET_DEFAULT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(rmUi)) {
            String fileName = "bootanimation.zip";
            File rmFile = new File(context.getFilesDir(), fileName);
            if (rmFile.exists()) {
                try {
                    rmFile.delete();
                } catch (Exception e) { }
                Toast.makeText(context, R.string.remove_ui, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
