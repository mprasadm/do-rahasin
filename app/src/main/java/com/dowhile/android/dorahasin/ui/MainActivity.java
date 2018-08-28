package com.dowhile.android.dorahasin.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.dowhile.android.dorahasin.MainApplication;
import com.dowhile.android.dorahasin.R;
import com.dowhile.android.dorahasin.service.CameraService;

import io.flic.lib.FlicAppNotInstalledException;
import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

import static com.dowhile.android.dorahasin.util.PermissionHelper.PERMISSIONS;
import static com.dowhile.android.dorahasin.util.PermissionHelper.PERMISSION_ALL;
import static com.dowhile.android.dorahasin.util.PermissionHelper.hasPermissions;


public class MainActivity extends AppCompatActivity {


    public static SurfaceView mSurfaceView;
    private FlicButton button;
    private boolean isPhotoEnabled;
    private boolean isVideoEnabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.sv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CameraService.isRecording) {
                    Intent intentSetPref = new Intent(getApplicationContext(), PrefActivity.class);
                    startActivityForResult(intentSetPref, 0);
                    //onFlicConnectClicked();
                } else {
                    Toast.makeText(MainApplication.getAppContext(), "Please stop your current recording", Toast.LENGTH_LONG).show();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            checkPermissionOverlay();
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            Intent serviceIntent = new Intent(this, CameraService.class);
            startService(serviceIntent);

        }
    }

    private void onFlicConnectClicked() {
        try {
            FlicManager.getInstance(this, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    manager.initiateGrabButton(MainActivity.this);
                }
            });
        } catch (FlicAppNotInstalledException err) {
            Toast.makeText(this, "Flic App is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {
            @Override
            public void onInitialized(FlicManager manager) {
                button = manager.completeGrabButton(requestCode, resultCode, data);
                if (button != null) {
                    SharedPreferences preference = MainApplication.getPreferenceManager();
                    preference.edit().putBoolean("FLIC_REGISTERED", true).commit();
                    button.registerListenForBroadcast(FlicBroadcastReceiverFlags.CLICK_OR_DOUBLE_CLICK_OR_HOLD | FlicBroadcastReceiverFlags.REMOVED);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preference = MainApplication.getPreferenceManager();
        isPhotoEnabled = preference.getBoolean("PREF_PHOTO", false);
        isVideoEnabled = preference.getBoolean("PREF_VIDEO", false);
        String actionMode = preference.getString("PREF_ACTION", "volume");
        if (actionMode.equalsIgnoreCase("flic")) {
            if (!preference.getBoolean("FLIC_REGISTERED", false)) {
                onFlicConnectClicked();
            }
            CameraService.resetMediaSession();
        } else {
            preference.edit().putBoolean("FLIC_REGISTERED", false).commit();
            if (button != null) {
                button.removeAllFlicButtonCallbacks();
            }
            CameraService.setMediaSession();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do your work....
                    Intent serviceIntent = new Intent(this, CameraService.class);
                    startService(serviceIntent);
                } else {
                    // permission denied
                    // Disable the functionality that depends on this permission.
                }
                return;
            }

            // other 'case' statements for other permssions
        }
    }

    public static int OVERLAY_PERMISSION_REQ_CODE = 1;

    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermissionOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intentSettings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intentSettings, OVERLAY_PERMISSION_REQ_CODE);
        }
    }
}
