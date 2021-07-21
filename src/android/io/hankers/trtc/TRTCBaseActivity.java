package io.hankers.trtc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import org.apache.cordova.CordovaActivity;

import java.util.ArrayList;
import java.util.List;


public abstract class TRTCBaseActivity extends CordovaActivity {

    protected static final int REQ_PERMISSION_CODE = 0x1000;
    protected int              mGrantedCount       = 0;

    protected abstract void onPermissionGranted();

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(Manifest.permission.INTERNET)) {
                permissions.add(Manifest.permission.INTERNET);
            }
            if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                this.requestPermissions(permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED == ret) {
                        mGrantedCount ++;
                    }
                }
                if (mGrantedCount == permissions.length) {
                    onPermissionGranted();
                } else {
                    Toast.makeText(this, "没有权限，加入通话失败", Toast.LENGTH_SHORT).show();
                }
                mGrantedCount = 0;
                break;
            default:
                break;
        }
    }
}
