
package com.google.android.cameraview.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener {
    static {
        System.loadLibrary("MyLibs");
    }
    //Bitmap panoramaBitmap;

    private static final String TAG = "CameraActivity";
    private ImageView previewImage;
    int Pics=2;
    int count=0;
    int flag=0;
    //SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {

                        count++;

                        if(count<Pics)
                        {

                            mCameraView.takePicture();

                        }

                        else {

                            Toast.makeText(v.getContext(),"Limit reached",Toast.LENGTH_LONG).show();
                          //  stitchImages();
                            (new showDialog()).execute();
                        }

                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewImage = (ImageView) findViewById(R.id.overlay1);
        mCameraView = (CameraView) findViewById(R.id.camera);
        count=0;
        SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
       String name = preferences.getString("ImageCount", "");
        if(!name.equalsIgnoreCase(""))
        {
            Pics= Integer.parseInt(name);
        }
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.take_picture);
        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
        }
        Toolbar toolbar = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
        }
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted, Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aspect_ratio:
               FragmentManager fragmentManager = getFragmentManager();
                if (mCameraView != null
                        && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                    final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                    final AspectRatio currentRatio = mCameraView.getAspectRatio();
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                            .show(fragmentManager, FRAGMENT_DIALOG);
                }
                return true;
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), "Picture Taken: "+count, Toast.LENGTH_SHORT)
                    .show();
            final Random ran= new Random();
            final String[] path = new String[1];
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "picture"+ran.nextInt()+".png");

                    path[0] =file.getAbsolutePath();
                    SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("camera_img",path[0]);
                    editor.putString("image"+count,path[0]);
                    editor.commit();

                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Cannot write to " + file, e);
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }

                  new PicAsync().execute();

                }
            });
        }

    };

    @SuppressLint("NewApi")
    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }


    public class PicAsync extends AsyncTask<Void,Bitmap,Bitmap>
    {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
            String imageFilePath = preferences.getString("camera_img", "");
            final Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            Matrix matrix = new Matrix();

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                // Portrait Mode
                matrix.postRotate(90);
            } else {
                // Landscape Mode
            }

            final Bitmap rbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            Matrix matrix2 = new Matrix();
           // matrix2.postScale(0.5f, 0.5f);
            Bitmap croppedBitmap = Bitmap.createBitmap(rbitmap, (rbitmap.getWidth()/2)+500, 0,(rbitmap.getWidth()/2)-500, rbitmap.getHeight(), matrix2, true);

            return croppedBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap croppedBitmap) {
            super.onPostExecute(croppedBitmap);
            previewImage.setImageBitmap(croppedBitmap);
        }
    }

    public class showDialog extends AsyncTask<Void,Void,Bitmap>
    {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p=new ProgressDialog(CameraActivity.this);
            p.setMessage("Stitching...");
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
             return  stitchImages();
        }

        @Override
        protected void onPostExecute(Bitmap panorama) {
            super.onPostExecute(panorama);
            if(p.isShowing())
                p.dismiss();
            if(panorama==null) {
                Toast.makeText(getApplicationContext(),"ERROR IN STITCHING",Toast.LENGTH_SHORT).show();
            }
            else
            {

                SaveImage(panorama);
           Intent i= new Intent(CameraActivity.this,PanaromaViewActivity.class);
            startActivity(i);

            }
        }
    }

    private void SaveImage(Bitmap finalBitmap) {

        final Random ran= new Random();

        //String root = Environment.getExternalStoragePublicDirectory(
          //      Environment.DIRECTORY_PICTURES).toString();
        //File myDir = new File(root + "/saved_images");
        //myDir.mkdirs();
        Random generator = new Random();

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-Test"+ n +".png";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "Test"+ran.nextInt()+".png");

        String [] path = new String[1];
        path[0] =file.getAbsolutePath();
        SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Finalimage",path[0]);
        editor.commit();

        if (file.exists ()) file.delete ();
        try {
            OutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
// Tell the media scanner about the new file so that it is
// immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    public Bitmap stitchImages()
    {
        //return type will indicate the status about stitching
        /*SharedPreferences preferences = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        String imageFilePath = preferences.getString("image1", "");
        if(imageFilePath!="") {
//            Toast.makeText(getApplicationContext(),"PIC Found",Toast.LENGTH_SHORT).show();
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
     //       return bitmap;
        }
       // Toast.makeText(getApplicationContext(),"PIC NOT Found",Toast.LENGTH_SHORT).show();
*/

        List<Mat> imagesList = new ArrayList<>();
        //default
        int noOfImages=Pics;
        SharedPreferences p = getSharedPreferences("MyPrefsFile",MODE_PRIVATE);
        String name = p.getString("ImageCount", "");
        if(!name.equalsIgnoreCase(""))
        {
            noOfImages = Integer.parseInt(name);
        }
        Bitmap imgBitmap=null;
        Bitmap rbitmap=null;
        for(int i=0;i<noOfImages;i++) {
            String imageFilePath = p.getString("image"+(i+1), "");
            if(imageFilePath!="") {
               imgBitmap = BitmapFactory.decodeFile(imageFilePath);
                Matrix matrix = new Matrix();
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    // Portrait Mode
                    matrix.postRotate(90);
                } else {
                    // Landscape Mode
                }


                rbitmap = Bitmap.createBitmap(imgBitmap, 0, 0, imgBitmap.getWidth(), imgBitmap.getHeight(), matrix, true);


///               SaveImage(imgBitmap);
            }

            Mat imgMat = new Mat();
            Utils.bitmapToMat(rbitmap, imgMat);

            imagesList.add(imgMat);
        }
     long[] matAdrsForJNI = new long[noOfImages];
        for (int i=0;i<noOfImages;i++){
            matAdrsForJNI[i]=  imagesList.get(i).getNativeObjAddr();
        }
        Mat panoramaMat= new Mat();
        int ret = OpenCVStitchImages.stichImages(matAdrsForJNI, panoramaMat.getNativeObjAddr());
       if(ret==0)
       {
           return null;
       }
        Bitmap panoramaBitmap=Bitmap.createBitmap(panoramaMat.cols(),  panoramaMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(panoramaMat, panoramaBitmap);
        return panoramaBitmap;
    }
}
