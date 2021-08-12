package io.hankers.trtc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * TRTC视频通话的主页面
 *
 * 包含如下简单功能：
 * - 进入视频通话房间{@link VideoCallingActivity#enterRoom()}
 * - 退出视频通话房间{@link VideoCallingActivity#exitRoom()}
 * - 切换前置/后置摄像头{@link VideoCallingActivity#switchCamera()}
 * - 打开/关闭摄像头{@link VideoCallingActivity#muteVideo()}
 * - 打开/关闭麦克风{@link VideoCallingActivity#muteAudio()}
 * - 显示房间内其他用户的视频画面（当前示例最多可显示6个其他用户的视频画面）{@link TRTCCloudImplListener#refreshRemoteVideoViews()}
 *
 * - 详见接入文档{https://cloud.tencent.com/document/product/647/42045}
 */

/**
 * Video Call
 *
 * Features:
 * - Enter a video call room: {@link VideoCallingActivity#enterRoom()}
 * - Exit a video call room: {@link VideoCallingActivity#exitRoom()}
 * - Switch between the front and rear cameras: {@link VideoCallingActivity#switchCamera()}
 * - Turn on/off the camera: {@link VideoCallingActivity#muteVideo()}
 * - Turn on/off the mic: {@link VideoCallingActivity#muteAudio()}
 * - Display the video of other users (max. 6) in the room: {@link TRTCCloudImplListener#refreshRemoteVideoViews()}
 *
 * - For more information, please see the integration document {https://cloud.tencent.com/document/product/647/42045}.
 */
public class VideoCallingActivity extends TRTCBaseActivity implements View.OnClickListener {

    private static final String             TAG = "VideoCallingActivity";
    private static final int                OVERLAY_PERMISSION_REQ_CODE = 1234;

    private TextView                        mTextTitle;
    private TXCloudVideoView                mTXCVVLocalPreviewView;
    private ImageView                       mImageBack;
    private ImageButton                     mButtonMuteVideo;
    private ImageButton                     mButtonMuteAudio;
    private ImageButton                     mButtonSwitchCamera;
    private ImageButton                     mButtonMuteSpeaker;
    private GridLayout                      mGridLayout;
    private ImageButton                     mBtnExit;

    private TRTCCloud                       mTRTCCloud;
    private TXDeviceManager                 mTXDeviceManager;
    private boolean                         mIsFrontCamera = true;
    private List<String>                    mRemoteUidList;
    private List<TXCloudVideoView>          mRemoteViewList;
    private int                             mUserCount = 0;
    private String                          mRoomId;
    private String                          mUserId;
    private String                          mUserSig;
    private boolean                         mAudioRouteFlag = true;
    private int                             mScreenWidth = 0;
    private int                             mScreenHeight = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;

        setContentView(TRTC.getResourceId("trtc_videocall_activity", "layout"));
//        getSupportActionBar().hide();
        handleIntent();

        if (checkPermission()) {
            initView();
            enterRoom();
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.hasExtra(Constant.USER_ID)) {
                mUserId = intent.getStringExtra(Constant.USER_ID);
            }
            if (intent.hasExtra(Constant.ROOM_ID)) {
                mRoomId = intent.getStringExtra(Constant.ROOM_ID);
            }
            if (intent.hasExtra(Constant.USER_SIG)) {
                mUserSig = intent.getStringExtra(Constant.USER_SIG);
            }
        }
    }

    private void initView() {
        // mTextTitle = findViewById(R.id.tv_room_number);
        // mImageBack = findViewById(R.id.iv_back);
        //mTXCVVLocalPreviewView = findViewById(TRTC.getResourceId("txcvv_main", "id"));
        mButtonMuteVideo = findViewById(TRTC.getResourceId("btn_mute_video", "id"));
        mButtonMuteAudio = findViewById(TRTC.getResourceId("btn_mute_audio", "id"));
        mButtonSwitchCamera = findViewById(TRTC.getResourceId("btn_switch_camera", "id"));
        mButtonMuteSpeaker = findViewById(TRTC.getResourceId("btn_mute_speaker", "id"));
        mBtnExit = findViewById(TRTC.getResourceId("btn_exit", "id"));

//        if (!TextUtils.isEmpty(mRoomId)) {
//            mTextTitle.setText(getString(TRTC.getResourceId("videocall_roomid", "string")) + mRoomId);
//        }
//        mImageBack.setOnClickListener(this);
        mButtonMuteVideo.setOnClickListener(this);
        mButtonMuteAudio.setOnClickListener(this);
        mButtonSwitchCamera.setOnClickListener(this);
        mButtonMuteSpeaker.setOnClickListener(this);
        mBtnExit.setOnClickListener(this);

        mRemoteUidList = new ArrayList<>();
        mRemoteViewList = new ArrayList<>();
        mTXCVVLocalPreviewView = findViewById(TRTC.getResourceId("trtc_view_1", "id"));
        mRemoteViewList = new LinkedList<>(Arrays.asList(
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_2", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_3", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_4", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_5", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_6", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_7", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_8", "id")),
                (TXCloudVideoView) findViewById(TRTC.getResourceId("trtc_view_9", "id"))
        ));

        mGridLayout = (GridLayout) findViewById(TRTC.getResourceId("grid", "id"));
        switch4Person();

//        mFloatingView = new FloatingView(getApplicationContext(), R.layout.videocall_view_floating_default);
//        mFloatingView.setPopupWindow(R.layout.videocall_popup_layout);
//        mFloatingView.setOnPopupItemClickListener(this);
    }

    private void enterRoom() {
        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.setListener(new TRTCCloudImplListener(VideoCallingActivity.this));
        mTXDeviceManager = mTRTCCloud.getDeviceManager();

        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = TRTC._sdkappId;
        trtcParams.userId = mUserId;
        trtcParams.roomId = Integer.parseInt(mRoomId);
        trtcParams.userSig = mUserSig;

        mTRTCCloud.startLocalPreview(mIsFrontCamera, mTXCVVLocalPreviewView);
        mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        mTRTCCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestDrawOverLays();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mFloatingView != null && mFloatingView.isShown()) {
//            mFloatingView.dismiss();
//        }
        exitRoom();
    }

    private void exitRoom() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopLocalAudio();
            mTRTCCloud.stopLocalPreview();
            mTRTCCloud.exitRoom();
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mFloatingView != null && mFloatingView.isShown()) {
//            mFloatingView.dismiss();
//        }
    }

    @Override
    protected void onPermissionGranted() {
        initView();
        enterRoom();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == TRTC.getResourceId("iv_back", "id") ||
                id == TRTC.getResourceId("btn_exit", "id")) {
            finish();
        } else if (id == TRTC.getResourceId("btn_mute_video", "id")) {
            muteVideo();
        } else if (id == TRTC.getResourceId("btn_mute_audio", "id")) {
            muteAudio();
        } else if (id == TRTC.getResourceId("btn_mute_speaker", "id")) {
            muteSpeaker();
        } else if (id == TRTC.getResourceId("btn_switch_camera", "id")) {
            switchCamera();
        } else if (id == TRTC.getResourceId("btn_audio_route", "id")) {
            audioRoute();
        } else if (id == TRTC.getResourceId("iv_return", "id")){
            floatViewClick();
        }
    }

    private void floatViewClick() {
        Intent intent = new Intent(this, VideoCallingActivity.class);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void muteVideo() {
        boolean isSelected = mButtonMuteVideo.isSelected();
        if (!isSelected) {
            mTRTCCloud.stopLocalPreview();
            mButtonMuteVideo.setImageResource(TRTC.getResourceId("video_close", "mipmap"));
        } else {
            mTRTCCloud.startLocalPreview(mIsFrontCamera, mTXCVVLocalPreviewView);
            mButtonMuteVideo.setImageResource(TRTC.getResourceId("video_open", "mipmap"));
        }
        mButtonMuteVideo.setSelected(!isSelected);
    }

    private void muteAudio() {
        boolean isSelected = mButtonMuteAudio.isSelected();
        if (!isSelected) {
            mTRTCCloud.muteLocalAudio(true);
            mButtonMuteAudio.setImageResource(TRTC.getResourceId("microphone_disable", "mipmap"));
        } else {
            mTRTCCloud.muteLocalAudio(false);
            mButtonMuteAudio.setImageResource(TRTC.getResourceId("microphone", "mipmap"));
        }
        mButtonMuteAudio.setSelected(!isSelected);
    }

    private void muteSpeaker() {
        boolean isSelected = mButtonMuteSpeaker.isSelected();
        if (!isSelected) {
            mTRTCCloud.muteAllRemoteAudio(true);
            mButtonMuteSpeaker.setImageResource(TRTC.getResourceId("loudspeaker_disable", "mipmap"));
        } else {
            mTRTCCloud.muteAllRemoteAudio(false);
            mButtonMuteSpeaker.setImageResource(TRTC.getResourceId("loudspeaker", "mipmap"));
        }
        mButtonMuteSpeaker.setSelected(!isSelected);
    }

    private void switchCamera() {
        mIsFrontCamera = !mIsFrontCamera;
        mTXDeviceManager.switchCamera(mIsFrontCamera);
        if(mIsFrontCamera){
            mButtonSwitchCamera.setImageResource(TRTC.getResourceId("camera_switch_front", "mipmap"));
        }else{
            mButtonSwitchCamera.setImageResource(TRTC.getResourceId("camera_switch_end", "mipmap"));
        }
    }

    private void audioRoute() {
//        if(mAudioRouteFlag){
//            mAudioRouteFlag = false;
//            mTXDeviceManager.setAudioRoute(TXDeviceManager.TXAudioRoute.TXAudioRouteEarpiece);
//            mButtonAudioRoute.setText(getString(TRTC.getResourceId("videocall_use_speaker", "string")));
//        }else{
//            mAudioRouteFlag = true;
//            mTXDeviceManager.setAudioRoute(TXDeviceManager.TXAudioRoute.TXAudioRouteSpeakerphone);
//            mButtonAudioRoute.setText(getString(TRTC.getResourceId("videocall_use_receiver", "string")));
//        }
    }

    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<VideoCallingActivity> mContext;

        public TRTCCloudImplListener(VideoCallingActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            Log.d(TAG, "onUserVideoAvailable userId " + userId + ", mUserCount " + mUserCount + ",available " + available);
            int index = mRemoteUidList.indexOf(userId);
            if (available) {
                if (index != -1) {
                    return;
                }
                mRemoteUidList.add(userId);
                refreshRemoteVideoViews();
            } else {
                if (index == -1) {
                    return;
                }
                mTRTCCloud.stopRemoteView(userId);
                mRemoteUidList.remove(index);
                refreshRemoteVideoViews();
            }
            if (mRemoteUidList.size() > 3) {
                switch9Person();
            } else {
                switch4Person();
            }
        }

        private void refreshRemoteVideoViews() {
            for (int i = 0; i < mRemoteViewList.size(); i++) {
                if (i < mRemoteUidList.size()) {
                    String remoteUid = mRemoteUidList.get(i);
                    mRemoteViewList.get(i).setVisibility(View.VISIBLE);
                    mTRTCCloud.startRemoteView(remoteUid,
                            TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SMALL,
                            mRemoteViewList.get(i));
                } else {
                    mRemoteViewList.get(i).setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onError");
            VideoCallingActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode+ "]" , Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }

    public void requestDrawOverLays() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N && !Settings.canDrawOverlays(VideoCallingActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + VideoCallingActivity.this.getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            showFloatingView();
        }
    }

    private void showFloatingView() {
//        if (mFloatingView != null && !mFloatingView.isShown()) {
//            if ((null != mTRTCCloud)) {
//                mFloatingView.show();
//                mFloatingView.setOnPopupItemClickListener(this);
//            }
//        }
    }

    private void switch4Person() {
        VideoCallingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mGridLayout.getRowCount() == 2 && mGridLayout.getColumnCount() == 2) return;
                mGridLayout.removeAllViewsInLayout();
                mGridLayout.setRowCount(2);
                mGridLayout.setColumnCount(2);

                mGridLayout.addView(mTXCVVLocalPreviewView, 0);

                for(int i=0; i<3; i++) {
                    TXCloudVideoView v = (TXCloudVideoView)mRemoteViewList.get(i);
                    mGridLayout.addView(v, i+1);
                }
            }
        });
    }

    private void switch9Person() {
        VideoCallingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mGridLayout.getRowCount() == 3 && mGridLayout.getColumnCount() == 3) return;
                mGridLayout.removeAllViewsInLayout();
                mGridLayout.setRowCount(3);
                mGridLayout.setColumnCount(3);

                mGridLayout.addView(mTXCVVLocalPreviewView, 0);

                for(int i=0; i<mRemoteViewList.size(); i++) {
                    TXCloudVideoView v = (TXCloudVideoView)mRemoteViewList.get(i);
                    mGridLayout.addView(v, i+1);
                }
            }
        });
    }
}
