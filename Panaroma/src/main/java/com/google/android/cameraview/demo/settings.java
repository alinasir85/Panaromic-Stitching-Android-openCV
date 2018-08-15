package com.google.android.cameraview.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class settings extends Activity {

    EditText et;
    CheckBox autoBrightness;
    boolean checked;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Button btn= (Button) findViewById(R.id.settings_btn_done);
        et= (EditText) findViewById(R.id.imagecount);
        autoBrightness=(CheckBox)findViewById(R.id.brightness) ;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String imageCount=et.getText().toString();
                editor.putString("ImageCount",imageCount);

                if(autoBrightness.isChecked())
                {
                    checked=true;

                }
                else
                {
                    checked=false;
                }
                editor.putBoolean("AutoBright",checked);

                editor.commit();

                Intent i= new Intent(settings.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });


    }
}
