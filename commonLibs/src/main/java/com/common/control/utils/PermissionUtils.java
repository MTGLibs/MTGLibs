package com.common.control.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.common.control.R;
import com.common.control.dialog.PermissionNormalDialog;
import com.common.control.dialog.PermissionStorageDialog;
import com.common.control.interfaces.PermissionCallback;


public class PermissionUtils {
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int RQC_REQUEST_PERMISSION_ANDROID_11 = 51233;
    public static final int RQC_REQUEST_PERMISSION_ANDROID_BELOW = 53233;
    private static PermissionUtils instance;
    private PermissionCallback permissionCallback;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void showDialogPermission(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.pl_grant_permission)
                .setMessage(R.string.pl_grant_permission_desc)
                .setPositiveButton("Go Settings", (dialog, which) -> {
                    final Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    context.startActivity(intent);
                    dialog.dismiss();
                }).setNegativeButton(R.string.pl_cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    public void setPermissionCallback(PermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }

    public static PermissionUtils instance() {
        if (instance == null) {
            instance = new PermissionUtils();
        }
        return instance;
    }


    public PermissionCallback getPermissionCallback() {
        return permissionCallback;
    }

    public void requestPermission(Activity activity) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(PERMISSIONS_STORAGE, RQC_REQUEST_PERMISSION_ANDROID_BELOW);
            }
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, RQC_REQUEST_PERMISSION_ANDROID_BELOW);
            return;
        }

        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
            activity.startActivityForResult(intent, RQC_REQUEST_PERMISSION_ANDROID_11);
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            activity.startActivityForResult(intent, RQC_REQUEST_PERMISSION_ANDROID_11);
        }
    }

    public void showPermissionStorageDialog(Activity activity, String content, PermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;

        if (isStoragePermissionGranted(activity)) {
            this.permissionCallback.onPermissionGranted();
            return;
        }
        this.permissionCallback.onPermissionNotGranted();

        PermissionStorageDialog.show(activity, content);
    }

    public void showPermissionNormalDialog(Activity activity, PermissionCallback permissionCallback, String... permissions) {
        this.permissionCallback = permissionCallback;

        if (permissionGranted(activity, permissions)) {
            this.permissionCallback.onPermissionGranted();
            return;
        }
        this.permissionCallback.onPermissionNotGranted();

        PermissionNormalDialog.show(activity, permissions);
    }

    public void requestPermission(Activity activity, String... permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(permission, 1);
        }
    }


    public static boolean permissionGranted(Activity activity, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onActivityResult(Activity activity, int requestCode) {
        if (permissionCallback == null) {
            return;
        }
        if (requestCode == RQC_REQUEST_PERMISSION_ANDROID_11
                || requestCode == RQC_REQUEST_PERMISSION_ANDROID_BELOW) {
            if (isStoragePermissionGranted(activity)) {
                permissionCallback.onPermissionGranted();
            } else {
                permissionCallback.onPermissionDenied();
            }
        }
    }

    public static boolean isStoragePermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
