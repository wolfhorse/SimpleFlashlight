package com.wolfhorse.simpleflashlight;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import java.io.IOException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ToggleButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * @author Tim Sexton
 * July 21, 2014
 * @version 2.0
 *
 */
public class FlashlightActivity extends ActionBarActivity  implements Callback {

    private final Context mContext = this;
    private PackageManager mPackageManager = null;
    private Camera mCamera = null;
    private ToggleButton mButton;
    private SurfaceView mPreview = null;
    private SurfaceHolder mHolder = null;
    private Animation mAnimScale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flashlight);
        mPackageManager = mContext.getPackageManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Verify our required hardware exists on this device.
        isCameraSupported();
        isFlashSupported();

        // Setup button animation to zoom out and in when clicked
        // to give the user more visual button click feedback.
        mAnimScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnimScale = null;
        mPackageManager = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mPreview == null)
            mPreview = (SurfaceView) findViewById(R.id.preview1);

        if (mPreview != null)
            mHolder = mPreview.getHolder();

        if (mHolder != null)
            mHolder.addCallback(this);

        if (mButton == null)
            mButton = (ToggleButton) findViewById(R.id.toggleButton1);

        if (mButton != null)
        {
            mButton.bringToFront();
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Turn the flashlight on/off
                    view.startAnimation(mAnimScale);
                    toggleFlash(mButton.isChecked());
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mHolder != null)
            mHolder.removeCallback(this);

        mHolder = null;
        mPreview = null;
    }

    @Override
    protected void onResume()
    {
        OpenCamera();

        // Default the light to the ToggleButton's check state. (ON by default).
        // This will reset to the default if the user closes the app with the back button, but not when switching apps.
        if (mButton != null)
            toggleFlash(mButton.isChecked());

        super.onResume();
    }

    @Override
    protected void onPause()
    {
        CloseCamera();
        super.onPause();
    }

    private void CloseCamera(){
        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    private void OpenCamera(){
        CloseCamera();
        mCamera = Camera.open();
    }

    private boolean isFlashSupported(){
        // if device support camera flash?
        if (mPackageManager == null)
            return false;

        if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            return true;
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("No Flash Detected");
            alertDialog.setMessage("This device's camera doesn't support flash.");
            alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    Log.e("err", "This device's camera doesn't support flash.");
                }
            });
            alertDialog.show();
        }
        return false;
    }

    private boolean isCameraSupported(){
        // if device support camera?
        if (mPackageManager == null)
            return false;

        if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("No Camera Detected");
            alertDialog.setMessage("This device doesn't support a camera.");
            alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    Log.e("err", "This device doesn't support a camera.");
                }
            });
            alertDialog.show();
        }
        return false;
    }

    private void toggleFlash(boolean turnLightOn)
    {
        if (mCamera != null)
        {
            final Parameters parameters;
            parameters = mCamera.getParameters();
            if(turnLightOn)
            {
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                Log.i("info", "Flash is ON...");
            }
            else
            {
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                Log.i("info", "Flash is OFF...");
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null && mHolder != null)
        {
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                // Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Auto-generated method stub
    }
}
