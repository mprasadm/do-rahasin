package com.dowhile.android.dorahasin.receiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.dowhile.android.dorahasin.MainApplication;
import com.dowhile.android.dorahasin.service.CameraService;

import io.flic.lib.FlicButton;

public class FlicBroadcastReceiver extends io.flic.lib.FlicBroadcastReceiver {

    @Override
    protected void onRequestAppCredentials(Context context) {
        // Set app credentials by calling FlicManager.setAppCredentials here
    }

    @Override
    public void onButtonUpOrDown(Context context, FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
        if (isUp) {
            // Code for button up event here
        } else {
            // Code for button down event here
        }
    }

    @Override
    public void onButtonSingleOrDoubleClickOrHold(Context context, FlicButton button, boolean wasQueued, int timeDiff, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
        super.onButtonSingleOrDoubleClickOrHold(context, button, wasQueued, timeDiff, isSingleClick, isDoubleClick, isHold);
        if (MainApplication.getPreferenceManager().getBoolean("FLIC_REGISTERED", false)) {
            if (isSingleClick && MainApplication.getPreferenceManager().getBoolean("PREF_PHOTO", false)) {
                Toast.makeText(context, "Single", Toast.LENGTH_SHORT).show();
                CameraService.doPhotoProcess();
            } else if (isDoubleClick && MainApplication.getPreferenceManager().getBoolean("PREF_VIDEO", false)) {
                if (!CameraService.isRecording)
                    CameraService.doVideoProcess();
                else
                    CameraService.stopRecording();
                Toast.makeText(context, "Double", Toast.LENGTH_SHORT).show();
            } else if (isHold) {
                Toast.makeText(context, "Hold", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onButtonRemoved(Context context, FlicButton button) {
        SharedPreferences preference = MainApplication.getPreferenceManager();
        preference.edit().putBoolean("FLIC_REGISTERED", false).commit();
    }
}
