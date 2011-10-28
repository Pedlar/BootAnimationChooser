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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener {

	private static LinearLayout mMainLayout;
	
	private static LinearLayout mInstallLayout;
	private static LinearLayout mInstallLayoutMain;
	
	private static LinearLayout mUninstallLayout;
	private static LinearLayout mUninstallLayoutMain;
	
	private static LinearLayout mChooseLayout;
	
	private static LinearLayout mResetLayout;

	private static LinearLayout mPreviewLayout;
	
	private static LinearLayout mPreviewLayoutCurrent;
	
	private static TextView choseText;
	
	private static final String BOOT_MD5 = "5b7703b6d55fdb638f586086bbb961c0";
	
	private static final String BOOT_RESET = "com.pedlar.bootanim.RESET_DEFAULT";
	public static final String BOOT_MOVE = "com.pedlar.bootanim.MOVE_BOOTANIMATION";
	
	private static boolean mBootPreviewRunning;
	private static final String BOOT_PREVIEW_FILE = "preview_bootanim";
	private static int prevOrientation;
	
	private ViewFlow viewFlow;
	private ListView listView;
	private TextView textView;
	
	private File currentDir;
	
	private FileArrayAdapter adapter;
	
	private static String choseFile;
	
	private static Context mContext;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setTitle(R.string.main_head);
        setContentView(R.layout.main);
        mContext = getApplicationContext();
        
        mvBootAnim();
        
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        ViewAdapter viewAdapter = new ViewAdapter(this);
        viewFlow.setAdapter(viewAdapter);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(viewAdapter);
		viewFlow.setFlowIndicator(indicator);
		
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		textView = (TextView) findViewById(R.id.textView1);
        currentDir = new File("/sdcard/");
        fill(currentDir);
        
        mInstallLayoutMain = (LinearLayout) findViewById(R.id.installayout_main);
        mInstallLayoutMain.setVisibility(View.GONE);
        mUninstallLayoutMain = (LinearLayout) findViewById(R.id.uninstallayout_main);
        mUninstallLayoutMain.setVisibility(View.GONE);
        checkInstall();
        
        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mMainLayout.setOnClickListener(this);
        
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
        
        choseText = (TextView) findViewById(R.id.chose_file);
    }
    
    @Override
	public void onItemClick(AdapterView arg0, View v, int position, long id) {
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		} else {
			//onFileClick(o);
			Toast.makeText(mContext, "Picked: " + o.getName(), Toast.LENGTH_SHORT).show();
			choseFile = o.getPath();
			choseText.setText("Chosen File: \n" + o.getPath());
		}
	}
    
    private void fill(File f)
    {
         File[]dirs = f.listFiles();
         textView.setText("Current Dir: " + f.getName());
         List<Option>dir = new ArrayList<Option>();
         List<Option>fls = new ArrayList<Option>();
         try{
             for(File ff: dirs)
             {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                	if(ff.getName().endsWith(".zip")) {
                		fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                	}
                }
             }
         }catch(Exception e)
         {
             e.printStackTrace();
         }
         Collections.sort(dir);
         Collections.sort(fls);
         dir.addAll(fls);
         if(!f.getName().equalsIgnoreCase("sdcard"))
             dir.add(0,new Option("..","Parent Directory",f.getParent()));
         
         adapter = new FileArrayAdapter(MainActivity.this,R.layout.file_list,dir);
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
            if (is != null) is.close();
            if (outfile != null) outfile.close();
        } catch (IOException e) { 
            // Should never happen! 
            throw new RuntimeException(e); 
        } 
    }
    
    private void startPreview(boolean current) {
    	if(!current) {
    		String filePath = choseFile;
    		if (filePath != null) {
    			try {
    				FileOutputStream outfile = mContext.openFileOutput(BOOT_PREVIEW_FILE, Context.MODE_WORLD_READABLE);
    				outfile.write(filePath.getBytes());
    				outfile.close();
    			} catch (Exception e) { }
    			mBootPreviewRunning = true;
    			prevOrientation = getRequestedOrientation();
    			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    			runRoot("setprop ctl.start bootanim");
    		} else {
    			Toast.makeText(mContext, "Please choose a file.", Toast.LENGTH_SHORT).show();
    		}
    	} else {
    		mBootPreviewRunning = true;
			prevOrientation = getRequestedOrientation();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			runRoot("setprop ctl.start bootanim");
    	}
    }
    
    private void saveFile() {
    	String filePath = choseFile;
        if (filePath != null) {
        	Intent mvBootIntent = new Intent();
        	mvBootIntent.setAction(BOOT_MOVE);
        	mvBootIntent.putExtra("fileName", filePath);
        	sendBroadcast(mvBootIntent);
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
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
			currentBoot = sb.toString();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	if(!currentBoot.equals(BOOT_MD5)) {
    		mInstallLayoutMain.setVisibility(View.VISIBLE);
    	} else {
    		mUninstallLayoutMain.setVisibility(View.VISIBLE);
    	}
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Context context = getApplicationContext();
        if(mBootPreviewRunning) {
            File rmFile = new File(context.getFilesDir(), BOOT_PREVIEW_FILE);
            if (rmFile.exists()) {
                try {
                    rmFile.delete();
                } catch (Exception e) { }
            }
            setRequestedOrientation(prevOrientation);
            runRoot("setprop ctl.stop bootanim");
            mBootPreviewRunning = false;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        Context context = getApplicationContext();
        if (keyCode == KeyEvent.KEYCODE_BACK && mBootPreviewRunning) {
            File rmFile = new File(context.getFilesDir(), BOOT_PREVIEW_FILE);
            if (rmFile.exists()) {
                try {
                    rmFile.delete();
                } catch (Exception e) { }
            }
            runRoot("setprop ctl.stop bootanim");
            mBootPreviewRunning = false;
            setRequestedOrientation(prevOrientation);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public void onClick(View v) {
		if(!mBootPreviewRunning) {
			switch(v.getId()) {
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
					Intent intent = new Intent(BOOT_RESET);
					sendBroadcast(intent);
					break;
			}
		} else {
	        File rmFile = new File(mContext.getFilesDir(), BOOT_PREVIEW_FILE);
	        if (rmFile.exists()) {
	            try {
	                rmFile.delete();
	            } catch (Exception e) { }
	        }
	        setRequestedOrientation(prevOrientation);
	        runRoot("setprop ctl.stop bootanim");
	        mBootPreviewRunning = false;
		}
	}
}