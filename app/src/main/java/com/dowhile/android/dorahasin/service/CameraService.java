package com.dowhile.android.dorahasin.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.dowhile.android.dorahasin.MainApplication;
import com.dowhile.android.dorahasin.util.Util;

import java.io.IOException;
import java.util.List;


public class CameraService extends Service {

    private static MediaSessionCompat mediaSession;
    private static String TAG = "CameraService_dup";
    //Camera variables
    //a surface holder
    private static SurfaceHolder sHolder;
    //a variable to control the camera
    private static Camera mCamera;
    //the camera parameters
    private static Camera.Parameters parameters;
    private static SurfaceView sv;
    private static SurfaceView vg;
    public static boolean isRecording = false;

    public static boolean isPhotoEnabled = false;
    public static boolean isVideoEnabled = false;
    private static VolumeProviderCompat myVolumeProvider;

    public CameraService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        vg = new SurfaceView(this) {
            @Override
            protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
            }
        };
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1,
                1,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        params.setTitle("Any Title");

        wm.addView(vg, params);

    }

    public static void setMediaSession() {
        mediaSession = new MediaSessionCompat(MainApplication.getAppContext(), "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                /*
                -1 -- volume down
                1 -- volume up
                0 -- volume button released
                 */


                        if (direction == -1 && MainApplication.getPreferenceManager().getBoolean("PREF_PHOTO", false)) {
                            doPhotoProcess();
                        } else if (direction == 1 && MainApplication.getPreferenceManager().getBoolean("PREF_VIDEO", false)) {
                            if (!isRecording)
                                doVideoProcess();
                            else
                                stopRecording();
                        }

                    }

                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
    }

    public static void resetMediaSession() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }

    private static SurfaceView mSurfaceView;
    private static SurfaceHolder mSurfaceHolder;
    private static Camera mServiceCamera;
    private static boolean mRecordingStatus;
    private static MediaRecorder mMediaRecorder;

    public static void doVideoProcess() {
        try {
            isRecording = true;

            releaseCameraAndPreview();
            mServiceCamera = Camera.open();

            mSurfaceView = vg;
            mSurfaceHolder = vg.getHolder();

            mSurfaceHolder.addCallback(mSurfCallBack);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            Camera.Parameters p = mServiceCamera.getParameters();
            // p = Util.getCameraParamWithRotation(getResources(), p);

            final List<Camera.Size> listSize = p.getSupportedVideoSizes();
            Camera.Size mPreviewSize = listSize.get(0);
            p.setPreviewFormat(PixelFormat.YCbCr_422_SP);
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);

            mServiceCamera.setParameters(p);

            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            mServiceCamera.unlock();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOutputFile(Util.getOutputFileStr());
            mMediaRecorder.setVideoEncodingBitRate(2147483647);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecordingStatus = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecording();
                }
            }, 10000 * 60);

        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            isRecording = false;

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            isRecording = false;
        }
    }

    public static void stopRecording() {
        if (mServiceCamera != null) {
            try {
                isRecording = false;
                mServiceCamera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaRecorder.stop();
            mMediaRecorder.reset();

            mServiceCamera.stopPreview();
            mMediaRecorder.release();

            mServiceCamera.release();
            mServiceCamera = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public static void doPhotoProcess() {
        try {
            if (!isRecording) {
                releaseCameraAndPreview();
                mServiceCamera = Camera.open();
                //  SurfaceView sv = new SurfaceView(getApplicationContext());

                mServiceCamera.setPreviewDisplay(vg.getHolder());
                mServiceCamera.startPreview();
                //Get a surface
                sHolder = vg.getHolder();
                //tells Android that this surface will have its data constantly replaced
                sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            parameters = mServiceCamera.getParameters();
            List<Camera.Size> s = parameters.getSupportedPictureSizes();
            //set camera parameters
            parameters.setPictureSize(s.get(0).width, s.get(0).height);
            parameters = Util.getCameraParamWithRotation(MainApplication.getAppContext().getResources(), parameters);
            //  parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            mServiceCamera.setParameters(parameters);
            mServiceCamera.takePicture(null, null, mCall);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static SurfaceHolder.Callback mSurfCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    static Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap
            Util.writeImgFilesToDir(data, MainApplication.getAppContext());
        }
    };

    private static void releaseCameraAndPreview() {
        if (mServiceCamera != null) {
            mServiceCamera.stopPreview();
            mServiceCamera.release();
            mServiceCamera = null;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
