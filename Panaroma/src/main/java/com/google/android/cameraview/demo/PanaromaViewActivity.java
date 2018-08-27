package com.google.android.cameraview.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class PanaromaViewActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panaroma_view);
        Intent intent = getIntent();
        Bitmap panoramaBitmap = (Bitmap) intent.getParcelableExtra("panorama");
        ((ImageView)findViewById(R.id.panaromaView)).setImageBitmap(panoramaBitmap);
    }
}
