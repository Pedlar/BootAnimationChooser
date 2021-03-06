
package com.pedlar.bootanimation;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    public Handler mHandler = new Handler();

    private static LinearLayout mMainLayout;

    private static LinearLayout mInstallLayout;
    private static LinearLayout mInstallLayoutMain;

    private static LinearLayout mUninstallLayout;
    private static LinearLayout mUninstallLayoutMain;

    private static LinearLayout mChooseLayout;

    private static LinearLayout mResetLayout;

    private static LinearLayout mPreviewLayout;

    private static LinearLayout mPreviewLayoutCurrent;

    private static RadioGroup mRadioGroupLocation;
    private static RadioGroup mRadioGroupMethod;
    
    private static RadioButton mRadioButtonData;
    private static RadioButton mRadioButtonSystem;
    
    private static RadioButton mRadioButtonBinary;
    private static RadioButton mRadioButtonAlternate;

    private static TextView choseText;

    private static final String BOOT_MD5 = "5b7703b6d55fdb638f586086bbb961c0";

    private static final String BOOT_RESET = "com.pedlar.bootanim.RESET_DEFAULT";
    public static final String BOOT_MOVE = "com.pedlar.bootanim.MOVE_BOOTANIMATION";

    private static boolean mBootPreviewRunning;
    private static final String BOOT_PREVIEW_FILE = "preview_bootanim";

    private static final int BINARY = 1;

    private static final int ALTERNATE = 2;

    private static final int SYSTEM_MEDIA = 3;

    private static final int DATA_LOCAL = 4;

    private static int prevOrientation;

    private ViewFlow viewFlow;
    private ListView listView;
    private TextView textView;

    private File currentDir;

    private FileArrayAdapter adapter;

    private static String choseFile;

    private static Context mContext;

    private static String installMethod;

    private static SharedPreferences prefSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setTitle(R.string.main_head);
        setContentView(R.layout.main);
        mContext = getApplicationContext();

        mvBootAnim();

        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        ViewAdapter viewAdapter = new ViewAdapter(this);
        viewFlow.setAdapter(viewAdapter);
        TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
        indicator.setTitleProvider(viewAdapter);
        viewFlow.setFlowIndicator(indicator);
        viewFlow.setSelection(1);

        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
        textView = (TextView) findViewById(R.id.textView1);
        currentDir = new File("/sdcard/");
        fill(currentDir);

        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mMainLayout.setOnClickListener(this);

        mInstallLayoutMain = (LinearLayout) findViewById(R.id.installayout_main);
        mInstallLayoutMain.setVisibility(View.GONE);

        mUninstallLayoutMain = (LinearLayout) findViewById(R.id.uninstallayout_main);
        mUninstallLayoutMain.setVisibility(View.GONE);

        mInstallLayout = (LinearLayout) findViewById(R.id.installayout);
        mInstallLayout.setOnClickListener(this);

        mUninstallLayout = (LinearLayout) findViewById(R.id.uninstallayout);
        mUninstallLayout.setOnClickListener(this);

        mChooseLayout = (LinearLayout) findViewById(R.id.chooselayout);
        mChooseLayout.setOnClickListener(this);

        mPreviewLayout = (LinearLayout) findViewById(R.id.previewlayout);
        mPreviewLayout.setOnClickListener(this);

        mPreviewLayoutCurrent = (LinearLayout) findViewById(R.id.previewlayout_current);
        mPreviewLayoutCurrent.setOnClickListener(this);

        mResetLayout = (LinearLayout) findViewById(R.id.resetlayout);
        mResetLayout.setOnClickListener(this);

        mRadioGroupLocation = (RadioGroup) findViewById(R.id.radio_location);
        mRadioGroupLocation.setOnCheckedChangeListener(this);

/*
 * Removing Binary Installer for Now.
        mRadioGroupMethod = (RadioGroup) findViewById(R.id.radio_method);
        mRadioGroupMethod.setOnCheckedChangeListener(this);
*/
        choseText = (TextView) findViewById(R.id.chose_file);

        prefSet = getSharedPreferences("main", Context.MODE_PRIVATE);
        if (prefSet.contains("installMethod")) {
            installMethod = prefSet.getString("installMethod", null);
            setupInstallMethod(installMethod);
            setupRadioButtons();
        } else {
            StartDialog myDialog = new StartDialog(this, new OnChoiceListener());
            myDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private int getInstallLocation() {
        String installLocation = prefSet.getString("installLocation", "data_local");
        if (installLocation.equals("data_local"))
            return DATA_LOCAL;
        else
            return SYSTEM_MEDIA;
    }

    public int getInstallMethod() {
        if (installMethod.equals("binary"))
            return BINARY;
        else
            return ALTERNATE;
    }
    
    private void setupRadioButtons() {
        mRadioButtonData = (RadioButton) findViewById(R.id.data_local_radio);
        mRadioButtonSystem = (RadioButton) findViewById(R.id.sys_media_radio);
        /*
         * Removing Binary Installer for Now.
        mRadioButtonBinary = (RadioButton) findViewById(R.id.binary_radio);
        mRadioButtonAlternate = (RadioButton) findViewById(R.id.alternate_radio);
         */
        switch(getInstallLocation()) {
            case DATA_LOCAL:
                mRadioButtonData.setChecked(true);
                mRadioButtonSystem.setChecked(false);
                break;
            case SYSTEM_MEDIA:
                mRadioButtonData.setChecked(false);
                mRadioButtonSystem.setChecked(true);
                break;
        }
        /*
         * Removing Binary Installer for Now.
        switch(getInstallMethod()) {
            case ALTERNATE:
                mRadioButtonAlternate.setChecked(true);
                mRadioButtonBinary.setChecked(false);
                break;
            case BINARY:
                mRadioButtonAlternate.setChecked(false);
                mRadioButtonBinary.setChecked(true);
                break;
        }
         */
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        //Toast.makeText(mContext, v.getId() + " " + v.toString(), Toast.LENGTH_SHORT).show();
        Option o = adapter.getItem(position);
        if (o.getData().equalsIgnoreCase("folder")
                || o.getData().equalsIgnoreCase("parent directory")) {
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            // onFileClick(o);
            Toast.makeText(mContext, "Picked: " + o.getName(), Toast.LENGTH_SHORT).show();
            choseFile = o.getPath();
            choseText.setText("Chosen File: \n" + o.getPath());
        }
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        textView.setText("Current Dir: " + f.getName());
        List<Option> dir = new ArrayList<Option>();
        List<Option> fls = new ArrayList<Option>();
        try {
            for (File ff : dirs) {
                if (ff.isDirectory())
                    dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
                else {
                    if (ff.getName().endsWith(".zip")) {
                        fls.add(new Option(ff.getName(), "File Size: " + ff.length(), ff
                                .getAbsolutePath()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Option("..", "Parent Directory", f.getParent()));

        adapter = new FileArrayAdapter(MainActivity.this, R.layout.file_list, dir);
        listView.setAdapter(adapter);
    }

    private void mvBootAnim() {
        try {
            InputStream is = getAssets().open("bootanimation");
            int size = is.available();
            FileOutputStream outfile = openFileOutput("bootanimation", Context.MODE_WORLD_READABLE);
            byte[] buf = new byte[size];
            int i = 0;
            while ((i = is.read(buf)) != -1) {
                outfile.write(buf, 0, i);
            }
            if (is != null)
                is.close();
            if (outfile != null)
                outfile.close();
        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
    }

    private void runBootAnim() {
        final ProgressDialog pbarDialog =
                ProgressDialog.show(this, "Loading...",
                                "Preparing Boot Animation...", true, false);

        mHandler.postDelayed(new Runnable() {
            public void run() {
                pbarDialog.dismiss();
                runRoot("setprop ctl.start bootanim");
            }
        }, 5000);

    }

    private void startPreview(boolean current) {
        if (getInstallMethod() == BINARY) {
            if (!current) {
                String filePath = choseFile;
                if (filePath != null) {
                    try {
                        FileOutputStream outfile = mContext.openFileOutput(BOOT_PREVIEW_FILE,
                                Context.MODE_WORLD_READABLE);
                        outfile.write(filePath.getBytes());
                        outfile.close();
                    } catch (Exception e) {
                    }
                    mBootPreviewRunning = true;
                    prevOrientation = getRequestedOrientation();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    runBootAnim();
                } else {
                    Toast.makeText(mContext, "Please choose a file.", Toast.LENGTH_SHORT).show();
                }
            } else {
                mBootPreviewRunning = true;
                prevOrientation = getRequestedOrientation();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                runBootAnim();
            }
        } else if (getInstallMethod() == ALTERNATE) {
            if (!current) {
                if (choseFile != null) {
                    if (getInstallLocation() == DATA_LOCAL) {
                        runRoot("mv /data/local/bootanimation.zip /data/local/bootanimation.preview_bk ; cp \""
                                + choseFile + "\" /data/local/bootanimation.zip");
                    } else if (getInstallLocation() == SYSTEM_MEDIA) {
                        runRoot("mount -o remount,rw /system ; mv /system/media/bootanimation.zip /system/media/bootanimation.preview_bk ; cp \""
                                + choseFile
                                + "\" /system/media/bootanimation.zip ; mount -o remount,rw /system");
                    }
                } else {
                    Toast.makeText(mContext, "Please choose a file.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            mBootPreviewRunning = true;
            prevOrientation = getRequestedOrientation();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            runBootAnim();
        }
    }

    private void stopPreview() {
        runRoot("setprop ctl.stop bootanim");
        File rmFile = new File(mContext.getFilesDir(), BOOT_PREVIEW_FILE);
        if (rmFile.exists()) {
            try {
                rmFile.delete();
            } catch (Exception e) {
            }
        }

        if (getInstallMethod() == ALTERNATE) {
            if (getInstallLocation() == DATA_LOCAL) {
                runRoot("mv /data/local/bootanimation.preview_bk /data/local/bootanimation.zip");
            } else if (getInstallLocation() == SYSTEM_MEDIA) {
                runRoot("mount -o remount,rw /system ; mv /system/media/bootanimation.preview_bk /system/media/bootanimation.zip ; mount -o remount,ro /system ;");
            }
        }
        setRequestedOrientation(prevOrientation);
        mBootPreviewRunning = false;
    }

    private void saveFile() {
        String filePath = choseFile;
        if (filePath != null) {
            if (getInstallMethod() == BINARY) {
                Intent mvBootIntent = new Intent();
                mvBootIntent.setAction(BOOT_MOVE);
                mvBootIntent.putExtra("fileName", filePath);
                sendBroadcast(mvBootIntent);
            } else if (getInstallMethod() == ALTERNATE) {
                if (getInstallLocation() == DATA_LOCAL) {
                    runRoot("mv /data/local/bootanimation.zip /data/local/bootanimation.install_bk ; cp \""
                            + filePath + "\" /data/local/bootanimation.zip");
                } else if (getInstallLocation() == SYSTEM_MEDIA) {
                    runRoot("mount -o remount,rw /system ; mv /system/media/bootanimation.zip /system/media/bootanimation.install_bk ; cp \""
                            + filePath
                            + "\" /system/media/bootanimation.zip ; mount -o remount,ro /system ;");
                }
            }
        } else {
            Toast.makeText(mContext, "Please choose a file.", Toast.LENGTH_SHORT).show();
        }
    }

    private InputStream runRoot(String command) {
        try {
            InputStream returnStream;
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(command + "\n");
            returnStream = p.getInputStream();
            os.writeBytes("exit\n");
            os.flush();
            return returnStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkInstall() {
        String currentBoot = "";
        try {
            InputStream is = runRoot("md5sum /system/bin/bootanimation | cut -d' ' -f1");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            currentBoot = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!currentBoot.equals(BOOT_MD5)) {
            mInstallLayoutMain.setVisibility(View.VISIBLE);
        } else {
            mUninstallLayoutMain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBootPreviewRunning) {
            stopPreview();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBootPreviewRunning) {
            stopPreview();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mBootPreviewRunning) {
            stopPreview();
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if (!mBootPreviewRunning) {
            switch (v.getId()) {
                case R.id.installayout:
                    runRoot("mount -o remount,rw /system ; cp /system/bin/bootanimation /system/bin/bootanimation.bk ; cp /data/data/com.pedlar.bootanimation/files/bootanimation /system/bin/bootanimation ; chmod 755 /system/bin/bootanimation ; mount -o remount,ro /system");
                    mInstallLayoutMain.setVisibility(View.GONE);
                    mUninstallLayoutMain.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, "Installed", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.uninstallayout:
                    runRoot("mount -o remount,rw /system ; mv /system/bin/bootanimation.bk /system/bin/bootanimation ; chmod 755 /system/bin/bootanimation ; mount -o remount,ro /system");
                    mUninstallLayoutMain.setVisibility(View.GONE);
                    mInstallLayoutMain.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, "Uninstalled", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.chooselayout:
                    saveFile();
                    break;
                case R.id.previewlayout:
                    startPreview(false);
                    break;
                case R.id.previewlayout_current:
                    startPreview(true);
                    break;
                case R.id.resetlayout:
                    if (getInstallMethod() == BINARY) {
                        Intent intent = new Intent(BOOT_RESET);
                        sendBroadcast(intent);
                    } else if (getInstallMethod() == ALTERNATE) {
                        if (getInstallLocation() == DATA_LOCAL) {
                            runRoot("cp /data/local/bootanimation.install_bak /data/local/bootanimation.zip");
                        } else if (getInstallLocation() == SYSTEM_MEDIA) {
                            runRoot("mount -o remount,rw /system ; cp /system/media/bootanimation.install_bak /system/media/bootanimation.zip ; mount -o remount,ro /system");
                        }
                        Toast.makeText(MainActivity.this, "Reset to default.", Toast.LENGTH_SHORT)
                                .show();
                    }
                    break;
            }
        } else {
            stopPreview();
        }
    }

    private void setupInstallMethod(String type) {
        if (type.equals("binary")) {
            checkInstall();
        } else {
            mInstallLayoutMain.setVisibility(View.GONE);
            mUninstallLayoutMain.setVisibility(View.GONE);
        }
    }

    public void onCheckedChanged(RadioGroup group, int position) {
        int id = group.getCheckedRadioButtonId();
        Log.v("BootAnim", "ID: " + id);
        switch (id) {
            case R.id.sys_media_radio:
                prefSet.edit().putString("installLocation", "system_local").commit();
                runRoot("rm -f /data/local/bootanimation.zip");
                break;
            case R.id.data_local_radio:
                prefSet.edit().putString("installLocation", "data_local").commit();
                runRoot("mount -o remount,rw /system ; rm -f /system/media/bootanimation.zip ; mount -o remount,ro /system");
                break;
/*
 * Removing Binary Installer for now.
            case R.id.binary_radio:
                prefSet.edit().putString("installMethod", "binary").commit();
                installMethod = "binary";
                setupInstallMethod("binary");
                break;
            case R.id.alternate_radio:
                prefSet.edit().putString("installMethod", "alternate").commit();
                installMethod = "alternate";
                setupInstallMethod("alternate");
                break;
*/
        }
    }

    private class OnChoiceListener implements StartDialog.ChoiceListener {
        @Override
        public void choice(String choice) {
            prefSet.edit().putString("installMethod", choice).commit();
            installMethod = choice;
            setupInstallMethod(choice);
            setupRadioButtons();
            /*
             * Toast.makeText(MainActivity.this, choice,
             * Toast.LENGTH_LONG).show();
             */
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.data_local:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                prefSet.edit().putString("installLocation", "data_local").commit();
                runRoot("mount -o remount,rw /system ; rm -f /system/media/bootanimation.zip ; mount -o remount,ro /system");
                return true;
            case R.id.system_menu:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                prefSet.edit().putString("installLocation", "system_local").commit();
                runRoot("rm -f /data/local/bootanimation.zip");
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
