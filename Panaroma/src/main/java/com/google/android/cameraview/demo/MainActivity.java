package com.google.android.cameraview.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    int camPerm=0;
    int ReadPerm=0;
    int WritePerm=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }



        Button photoButton = (Button) findViewById(R.id.button_image);
        final Button settings = (Button) findViewById(R.id.settings_btn);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,settings.class);
                startActivity(i);
                finish();
            }
        });

        photoButton.setOnClickListener(new  View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               Intent i= new Intent(v.getContext(),CameraActivity.class);
               startActivity(i);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            deleteAppData();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    private void deleteAppData() {
        try {
            // clearing app data
            String packageName = getApplicationContext().getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear "+packageName);

        } catch (Exception e) {
            e.printStackTrace();
        } }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){

                    camPerm=1;
                    // permission was granted
                } else {
                    // permission denied
                    //Disable the functionality
                    //that depends on this permission.
                }



                return;
            }

            case 2: {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){
                    ReadPerm=1;
                    // permission was granted
                } else {
                    // permission denied
                    //Disable the functionality
                    //that depends on this permission.
                }

                return;
            }

            case 3: {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED){

                    WritePerm=1;
                    // permission was granted
                } else {
                    // permission denied
                    //Disable the functionality
                    //that depends on this permission.
                }

                return;
            }

        }
    }
}
