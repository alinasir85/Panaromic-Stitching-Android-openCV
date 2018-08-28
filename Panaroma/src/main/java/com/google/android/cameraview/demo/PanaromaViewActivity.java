package com.google.android.cameraview.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class PanaromaViewActivity extends AppCompatActivity{
    String imageFilePath;
    String resultPath;
    Bitmap bitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panaroma_view);
       // Intent intent = getIntent();
        //Bitmap panoramaBitmap = (Bitmap) intent.getParcelableExtra("panorama");
   //   ((ImageView)findViewById(R.id.panaromaView)).setImageBitmap(panoramaBitmap);

        SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        imageFilePath = preferences.getString("Finalimage", "");
        bitmap = BitmapFactory.decodeFile(imageFilePath);
        ImageView previewImage = (ImageView) findViewById(R.id.panaromaView);
        if (bitmap != null){
            previewImage.setImageBitmap(bitmap);

        }

        Button btn= (Button) findViewById(R.id.resultSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Panaroma";
                File myDir = new File(root);
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File file = new File(myDir, fname);
                System.out.println(file.getAbsolutePath());
               // if (file.exists()) file.delete();
                Log.i("LOAD", root + fname);

                try {

                    OutputStream out = new FileOutputStream(file);

                   bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(v.getContext(),"SAVED",Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(),"NOT SAVED",Toast.LENGTH_LONG).show();
                }

                MediaScannerConnection.scanFile(v.getContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);

            }
        });
    }


}
