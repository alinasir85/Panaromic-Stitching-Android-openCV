package com.google.android.cameraview.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class preview extends Activity {
    private ImageView previewImage;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);


        SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        imageFilePath = preferences.getString("camera_img", "");

        Toast.makeText(this,imageFilePath,Toast.LENGTH_LONG).show();
        previewImage = (ImageView) findViewById(R.id.preview);

        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

//        Bitmap bitmap = BitmapFactory.decodeByteArray(camera, 0, camera.length);
//
        if (bitmap != null){
            previewImage.setImageBitmap(bitmap);
        }
    }
}
