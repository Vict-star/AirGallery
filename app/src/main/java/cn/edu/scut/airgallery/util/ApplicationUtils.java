package cn.edu.scut.airgallery.util;

import android.content.Context;

import androidx.annotation.NonNull;

import cn.edu.scut.airgallery.BuildConfig;

/**
 * Data class for holding Application-related data.
 */
public class ApplicationUtils {

    private static String PACKAGE_NAME;

    public static void init(@NonNull Context context) {
        PACKAGE_NAME = context.getPackageName();
    }

    /**
     * Get the Application's package name specified in Manifest
     */
    @NonNull
    public static String getPackageName() {
        return PACKAGE_NAME;
    }

    @NonNull
    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}