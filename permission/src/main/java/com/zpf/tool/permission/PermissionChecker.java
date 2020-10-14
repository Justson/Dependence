package com.zpf.tool.permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.SparseArray;

import com.zpf.tool.config.SpInstance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZPF on 2018/8/22.
 */
public abstract class PermissionChecker<T> {
    private List<PermissionInfo> permissionList;
    public static final int REQ_PERMISSION_CODE = 10001;
    protected final SparseArray<PermissionResultListener> permissionCallBack = new SparseArray<>();

    @SuppressLint("InlinedApi")
    private void initPermissionList() {
        permissionList = new ArrayList<>(24);
        permissionList.add(new PermissionInfo(Manifest.permission.WRITE_CONTACTS, "写入联系人", Manifest.permission_group.CONTACTS));
        permissionList.add(new PermissionInfo(Manifest.permission.READ_CONTACTS, "读取联系人", Manifest.permission_group.CONTACTS));
        permissionList.add(new PermissionInfo(Manifest.permission.GET_ACCOUNTS, "访问账户列表", Manifest.permission_group.CONTACTS));

        permissionList.add(new PermissionInfo(Manifest.permission.READ_CALL_LOG, "读取通话记录", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.READ_PHONE_STATE, "获取本机识别码", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.CALL_PHONE, "拨打电话", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.WRITE_CALL_LOG, "写入通话记录", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.USE_SIP, "使用SIP视频", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.PROCESS_OUTGOING_CALLS, "处理拨出电话", Manifest.permission_group.PHONE));
        permissionList.add(new PermissionInfo(Manifest.permission.ADD_VOICEMAIL, "添加语音邮件", Manifest.permission_group.PHONE));

        permissionList.add(new PermissionInfo(Manifest.permission.READ_CALENDAR, "读取日程信息", Manifest.permission_group.CALENDAR));
        permissionList.add(new PermissionInfo(Manifest.permission.WRITE_CALENDAR, "写入日程信息", Manifest.permission_group.CALENDAR));

        permissionList.add(new PermissionInfo(Manifest.permission.CAMERA, "访问摄像头", Manifest.permission_group.CAMERA));

        permissionList.add(new PermissionInfo(Manifest.permission.BODY_SENSORS, "读取生命体征相关的传感器数据", Manifest.permission_group.SENSORS));

        permissionList.add(new PermissionInfo(Manifest.permission.ACCESS_FINE_LOCATION, "获取精确位置", Manifest.permission_group.LOCATION));
        permissionList.add(new PermissionInfo(Manifest.permission.ACCESS_COARSE_LOCATION, "获取粗略位置", Manifest.permission_group.LOCATION));

        permissionList.add(new PermissionInfo(Manifest.permission.READ_EXTERNAL_STORAGE, "读取手机存储", Manifest.permission_group.STORAGE));
        permissionList.add(new PermissionInfo(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入手机存储", Manifest.permission_group.STORAGE));

        permissionList.add(new PermissionInfo(Manifest.permission.RECORD_AUDIO, "录音", Manifest.permission_group.MICROPHONE));

        permissionList.add(new PermissionInfo(Manifest.permission.READ_SMS, "读取短信内容", Manifest.permission_group.SMS));
        permissionList.add(new PermissionInfo(Manifest.permission.RECEIVE_WAP_PUSH, "接收Wap Push", Manifest.permission_group.SMS));
        permissionList.add(new PermissionInfo(Manifest.permission.RECEIVE_MMS, "接收彩信", Manifest.permission_group.SMS));
        permissionList.add(new PermissionInfo(Manifest.permission.RECEIVE_SMS, "接收短信", Manifest.permission_group.SMS));
        permissionList.add(new PermissionInfo(Manifest.permission.SEND_SMS, "发送短信", Manifest.permission_group.SMS));
    }

    public boolean checkPermissions(T target, String... permissions) {
        return checkPermissions(target, REQ_PERMISSION_CODE, getDefListener(), permissions);
    }

    public boolean checkPermissions(T target, int requestCode, String... permissions) {
        return checkPermissions(target, requestCode, getDefListener(), permissions);
    }

    public boolean checkPermissions(T target, int requestCode, PermissionResultListener listener, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkEffective(target)) {
                return false;
            }
            List<String> missPermissionList = new ArrayList<>();
            SharedPreferences.Editor editor = null;
            boolean showCustomRationale = false;
            for (String per : permissions) {
                if (!hasPermission(target, per)) {
                    if (!SpInstance.get().getBoolean(per, false)) {
                        if (editor == null) {
                            editor = SpInstance.get().edit();
                        }
                        editor.putBoolean(per, true);
                    } else if (!shouldShowRequestPermissionRationale(target, per)) {
                        showCustomRationale = true;
                    }
                    missPermissionList.add(per);
                }
            }
            if (editor != null) {
                editor.commit();
            }
            if (showCustomRationale) {
                if (listener != null) {
                    listener.onPermissionCheck(false, getMissInfo(missPermissionList));
                }
                return false;
            } else if (missPermissionList.size() > 0) {
                if (listener != null) {
                    permissionCallBack.put(requestCode, listener);
                }
                requestPermissions(target, missPermissionList, requestCode);
                return false;
            } else {
                if (listener != null) {
                    listener.onPermissionCheck(false, null);
                }
                return true;
            }
        } else {
            if (listener != null) {
                listener.onPermissionCheck(false, null);
            }
            return true;
        }
    }

    public void requestPermissions(T target, List<String> missPermissionList, int code) {
        if (missPermissionList != null && missPermissionList.size() > 0) {
            int size = missPermissionList.size();
            realRequestPermissions(target, missPermissionList.toArray(new String[size]), code);
        }
    }

    protected PermissionResultListener getDefListener() {
        return null;
    }

    //检查是否拥有权限
    public abstract boolean hasPermission(T target, String p);

    //检查传入对象是否为有效
    protected abstract boolean checkEffective(T target);

    //是否可以使用系统弹窗请求权限
    protected abstract boolean shouldShowRequestPermissionRationale(T target, String p);

    //请求权限
    protected abstract void realRequestPermissions(T target, String[] p, int code);

    //获取所有缺失权限的详细描述
    public List<PermissionInfo> getMissInfo(List<String> list) {
        List<PermissionInfo> PermissionInfoList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            if (permissionList == null) {
                initPermissionList();
            }
            if (permissionList != null) {
                boolean hasDesc;
                for (String name : list) {
                    hasDesc = false;
                    for (PermissionInfo permissionInfo : permissionList) {
                        if (TextUtils.equals(permissionInfo.getPermissionName(), name)) {
                            PermissionInfoList.add(permissionInfo);
                            hasDesc = true;
                            break;
                        }
                    }
                    if (!hasDesc) {
                        PermissionInfoList.add(new PermissionInfo(name, name, null));
                    }
                }
            }
        }
        return PermissionInfoList;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final PermissionResultListener resultListener = permissionCallBack.get(requestCode);
        if (resultListener == null) {
            return;
        }
        permissionCallBack.clear();
        List<String> missPermissionList = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (permissions.length > i) {
                    missPermissionList.add(permissions[i]);
                }
            }
        }
        if (missPermissionList.size() == 0) {
            resultListener.onPermissionCheck(true, null);
        } else {
            resultListener.onPermissionCheck(true, getMissInfo(missPermissionList));
        }
    }

    public boolean checkWriteSetting(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return false;
            }
        }
        return true;
    }

    public boolean checkDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                Intent serviceIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivity(serviceIntent);
                return false;
            }
        }
        return true;
    }

    public boolean checkNoticeEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= 24) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            return mNotificationManager != null && mNotificationManager.areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT >= 19) {
            AppOpsManager appOps =
                    (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE,
                        Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
                int value = (int) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg)
                        == AppOpsManager.MODE_ALLOWED);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException | RuntimeException e) {
                return true;
            }
        } else {
            return true;
        }
    }

    public void clearCallBack() {
        permissionCallBack.clear();
    }

}
