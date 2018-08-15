package com.google.android.cameraview.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"OpenCv Loaded Successfully",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"OpenCv NOT Loaded",Toast.LENGTH_LONG).show();

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



}
