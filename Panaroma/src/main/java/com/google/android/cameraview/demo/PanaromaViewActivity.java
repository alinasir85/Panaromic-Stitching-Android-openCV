package com.google.android.cameraview.demo;

import android.content.ActivityNotFoundException;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
    private Uri uri;
    ImageView previewImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_view);

        LinearLayout myll = (LinearLayout) findViewById(R.id.PanaromaLayout);
        myll.setOrientation(LinearLayout.HORIZONTAL);

        SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        imageFilePath = preferences.getString("Finalimage", "");
        bitmap = BitmapFactory.decodeFile(imageFilePath);
        previewImage = (ImageView) findViewById(R.id.panaromaView);

        if (bitmap != null){
            previewImage.setImageBitmap(bitmap);

        }

        Button btn= (Button) findViewById(R.id.resultSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmap = BitmapFactory.decodeFile(imageFilePath);

                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Panaroma";
                File myDir = new File(root);
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".png";
                File file = new File(myDir, fname);
                System.out.println(file.getAbsolutePath());
               // if (file.exists()) file.delete();
                Log.i("LOAD", root + fname);

                try {

                    OutputStream out = new FileOutputStream(file);

                   bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Toast.makeText(v.getContext(),"SAVED",Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(v.getContext(),"NOT SAVED",Toast.LENGTH_LONG).show();
                }

                MediaScannerConnection.scanFile(v.getContext(), new String[]{file.getPath()}, new String[]{"image/png"}, null);

            }
        });


        Button crop= (Button) findViewById(R.id.crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File f= new File(imageFilePath);
                Uri uri= Uri.fromFile(f);
                CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).start(PanaromaViewActivity.this);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Toast.makeText(this, "Uri = "+result.getUri(), Toast.LENGTH_LONG).show();

            if (resultCode == RESULT_OK)
            {
                previewImage.setImageURI(result.getUri());
                imageFilePath= result.getUri().getPath();
                SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Finalimage",imageFilePath);
                editor.commit();

            Toast.makeText(this, "Cropping successful " , Toast.LENGTH_SHORT).show();
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }


        }
    }

}
