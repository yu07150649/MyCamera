package com.example.yu.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private ImageView mImageView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView shutter;
    private Camera mCamera = null;
    private boolean mPreviewRunning;
    private static final int MENU_STRAT=1;
    private static final int MENU_SENSOR=2;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera);
        mImageView = (ImageView) findViewById(R.id.shutter);
        shutter = (ImageView) findViewById(R.id.shutter);
        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onClick(View view) {
        if(mPreviewRunning){
            shutter.setEnabled(false);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    mCamera.takePicture(mShutterCallback,null,mPictureCallback);
                }
            });
        }
    }
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if(bytes!=null){
                saveAndShow(bytes);
            }
        }
    };
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){

        @Override
        public void onShutter() {
            System.out.println("快照回调函数");
        }
    };
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setCameraParams();
    }
    public void setCameraParams(){
        if(mCamera!=null){
            return;
        }
        mCamera = Camera.open();
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setPreviewFrameRate(3);
        params.setPictureFormat(PixelFormat.YCbCr_422_SP);
        params.set("jpeg-quality",85);
        List<Camera.Size> list = params.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        int w = size.width;
        int h = size.height;
        params.setPictureSize(w,h);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0,MENU_STRAT,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==MENU_STRAT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if(item.getItemId()==MENU_SENSOR){
            Intent intent = new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void saveAndShow(byte[] data){
        try{
            String imageId = System.currentTimeMillis()+"";
            String pathName = android.os.Environment.getExternalStorageDirectory().getPath()+"/mycamera";
            File  file = new File(pathName);
            if(!file.exists()){
                file.mkdir();
            }
            pathName +="/"+imageId+".jpeg";
            file = new File(pathName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album = new AlbumActivity();
            bitmap = album.loadImage(pathName);
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            if(mPreviewRunning){
                mCamera.stopPreview();
                mPreviewRunning=false;
            }
            shutter.setEnabled(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
            if(mPreviewRunning){
                mCamera.stopPreview();
            }
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mPreviewRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera!=null){
            mCamera.stopPreview();
            mPreviewRunning=false;
            mCamera.release();
            mCamera=null;
        }
    }
}
