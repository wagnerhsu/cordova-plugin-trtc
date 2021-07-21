package io.hankers.trtc;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL;

public class TRTC extends CordovaPlugin {

    Activity _activity;
    public static int _sdkappId = 0;
    public static String _userInfoUrl = "";
    private static Application _app = null;
    private static String _packageName = null;
    private static Resources _resources = null;

    @Override
    protected void pluginInitialize() {
        _app = cordova.getActivity().getApplication();
        _packageName = _app.getPackageName();
        _resources = _app.getResources();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("enterRoom".equals(action)) {
            String userId = args.getString(0).trim();
            String userSig = args.getString(1).trim();
            String roomId = args.getString(2);

            Context context = this.cordova.getActivity().getApplicationContext();
            _activity = this.cordova.getActivity();

            Intent intent = new Intent(_activity, VideoCallingActivity.class);
            intent.putExtra(Constant.ROOM_ID, roomId);
            intent.putExtra(Constant.USER_ID, userId);
            intent.putExtra(Constant.USER_SIG, userSig);
            _activity.startActivity(intent);
        } else if (action.equals("init")) {
            JSONObject params = args.getJSONObject(0);
            _sdkappId = params.has("sdkappid") ? params.getInt("sdkappid") : 0;
            _userInfoUrl = params.has("user_info_url") ? params.getString("user_info_url") : "";
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

    }

    @Override
    public void onPause(boolean p) {
        super.onPause(p);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static int getResourceId(String name, String type) {
        int ic = _resources.getIdentifier(name, type, _packageName);
        return ic;
    }

    public static String getUserInfoUrl() { return _userInfoUrl; }
}