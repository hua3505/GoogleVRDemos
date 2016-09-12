package com.gmail.huashadow.videoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_LOCAL_VIDEO = 1;
    private static final String STATE_PROGRESS_TIME = "STATE_PROGRESS_TIME";

    private VrVideoView mVrVideoView;
    private VideoLoadTask mVideoLoadTask;
    private VrVideoView.Options mOptions = new VrVideoView.Options();
    private boolean mHasLoadedVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Button buttonPlayLocalVideo = (Button) findViewById(R.id.btn_play_local_video);
        buttonPlayLocalVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseLocalVideo();
            }
        });
        mOptions.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;
        mOptions.inputType = VrVideoView.Options.TYPE_MONO;
        mVrVideoView = (VrVideoView) findViewById(R.id.vr_video_view);
        mVrVideoView.setEventListener(new MyVrVideoEventListener());
    }

    private void chooseLocalVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_LOCAL_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_LOCAL_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Log.v(TAG, "Choosed video is " + uri);
                asyncLoadVideo(uri);
            } else {
                Log.w(TAG, "No choosed video. resultCode: " + requestCode);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void asyncLoadVideo(Uri uri) {
        if (mVideoLoadTask != null) {
            mVideoLoadTask.cancel(true);
        }
        mVideoLoadTask = new VideoLoadTask();
        //noinspection unchecked
        mVideoLoadTask.execute(Pair.create(uri, mOptions));
    }

    @Override
    protected void onDestroy() {
        mVrVideoView.pauseRendering();
        mVrVideoView.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasLoadedVideo) {
            mVrVideoView.resumeRendering();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHasLoadedVideo) {
            mVrVideoView.pauseRendering();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, mVrVideoView.getCurrentPosition());
//        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        mVrVideoView.seekTo(progressTime);
    }

    private class VideoLoadTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(Pair<Uri, VrVideoView.Options>... pairs) {
            if (pairs == null || pairs.length < 1
                    || pairs[0] == null || pairs[0].first == null) {
                return false;
            }
            Uri uri = pairs[0].first;
            VrVideoView.Options options = pairs[0].second;
            try {
                //noinspection WrongThread
                mVrVideoView.loadVideo(uri, options);
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
            return true;
        }
    }

    private class MyVrVideoEventListener extends VrVideoEventListener {
        public void onLoadSuccess() {
            mHasLoadedVideo = true;
            Log.v(TAG, "Video has been loaded successfully.");
        }

        public void onLoadError(String errorMessage) {
            Log.v(TAG, "Error occurs while loading video: " + errorMessage);
        }

        public void onClick() {
            Log.v(TAG, "video onClick");
        }

        public void onDisplayModeChanged(int newDisplayMode) {
            Log.v(TAG, "video onDisplayModeChanged to: " + newDisplayMode);
        }

        public void onCompletion() {
            Log.v(TAG, "video onCompletion");
        }

        public void onNewFrame() {
            Log.v(TAG, "video onNewFrame");
        }
    }
}
