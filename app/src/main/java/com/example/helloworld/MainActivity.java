package com.example.helloworld;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import com.example.screenshot.ScreenCapturer;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MediaProjectionManager mProjectionManager;
    private static MediaProjection sMediaProjection;
    private static final int REQUEST_CODE = 100;
    private static final String LOG_TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "[onActivityResult] requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            if (sMediaProjection != null) {
//                Intent i = new Intent(MainActivity.this, ScreenCaptureService.class);
//                i.putExtra(ScreenCaptureService.EXTRA_PATH, "some path");
//                i.putExtra(ScreenCaptureService.EXTRA_MEDIA_PROJECTION, () sMediaProjection);
                Log.d(LOG_TAG, "Start capturing screen...");
                ScreenCapturer capture = new ScreenCapturer(this, sMediaProjection);
                File cacheDir = this.getExternalCacheDir();
                Log.d(LOG_TAG, "cache 1: " + cacheDir);
                final File imageFile = new File(cacheDir, "screenshot.jpg");
                capture.startProjection(imageFile.getAbsolutePath(), new ScreenCapturer.OnScreenCapturedListener() {
                    @Override
                    public void imageCaptured(boolean successful) {
                        Log.d(LOG_TAG, "Image: " + imageFile.getAbsolutePath());
                    }
                });
            }
        }
    }

    public void RequestScreenshot() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }
}