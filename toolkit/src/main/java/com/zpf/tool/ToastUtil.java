package com.zpf.tool;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zpf.tool.config.AppContext;

import java.lang.ref.SoftReference;

/**
 * Created by ZPF on 2018/7/26.
 */
public class ToastUtil {
    private static SoftReference<DefToaster> defToaster;
    private static IToaster realToaster;

    //弹toast
    public static void toast(final CharSequence msg) {
        try {
            getToaster().showToast(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setToaster(@Nullable IToaster toaster) {
        realToaster = toaster;
    }

    public static IToaster getToaster() {
        if (realToaster == null) {
            return getDefToaster();
        } else {
            return realToaster;
        }
    }

    @NonNull
    public static IToaster getDefToaster() {
        if (defToaster.get() == null) {
            defToaster = new SoftReference<>(new DefToaster(AppContext.get()));
        }
        return defToaster.get();
    }
}