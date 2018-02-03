package com.lody.virtual.os;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;

import java.io.File;

/**
 * @author Lody
 */

public class VEnvironment {

    private static final String TAG = VEnvironment.class.getSimpleName();

    private static final File ROOT;
    private static final File DATA_DIRECTORY;
    private static final File USER_DIRECTORY;
    private static final File DALVIK_CACHE_DIRECTORY;

    static {    //getApplicationInfo().dataDir  <17 data/data/包名  >=17 data/user/0/包名
        File host = new File(getContext().getApplicationInfo().dataDir);  //  /data/user/0/io.virtualapp(多个进程调用这个static块)
        // Point to: /
        ROOT = ensureCreated(new File(host, "virtual"));    //  /data/user/0/io.virtualapp/virtual
        // Point to: /data/
        DATA_DIRECTORY = ensureCreated(new File(ROOT, "data")); //  /data/user/0/io.virtualapp/virtual/data
        // Point to: /data/user/
        USER_DIRECTORY = ensureCreated(new File(DATA_DIRECTORY, "user"));   //  /data/user/0/io.virtualapp/virtual/data/user
        // Point to: /opt/
        DALVIK_CACHE_DIRECTORY = ensureCreated(new File(ROOT, "opt"));  //  /data/user/0/io.virtualapp/virtual/opt
    }

    public static void systemReady() {  //获得该文件的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                FileUtils.chmod(ROOT.getAbsolutePath(), FileUtils.FileMode.MODE_755);   //  /data/user/0/io.virtualapp/virtual
                FileUtils.chmod(DATA_DIRECTORY.getAbsolutePath(), FileUtils.FileMode.MODE_755);// /data/user/0/io.virtualapp/virtual/data
                FileUtils.chmod(getDataAppDirectory().getAbsolutePath(), FileUtils.FileMode.MODE_755);// /data/user/0/io.virtualapp/virtual/data/app
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static Context getContext() {   //虚拟机app的applicationContext
        return VirtualCore.get().getContext();
    }

    private static File ensureCreated(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            VLog.w(TAG, "Unable to create the directory: %s.", folder.getPath());
        }
        return folder;
    }

    public static File getDataUserPackageDirectory(int userId,
                                                   String packageName) {///data/user/0/io.virtualapp/virtual/data/user/123/com.tentcent.qq
        return ensureCreated(new File(getUserSystemDirectory(userId), packageName));
    }

    public static File getPackageResourcePath(String packgeName) {///data/user/0/io.virtualapp/virtual/data/app/com.tentcent.qq/base.apk
        return new File(getDataAppPackageDirectory(packgeName), "base.apk");
    }

    public static File getDataAppDirectory() { ///data/user/0/io.virtualapp/virtual/data/app
        return ensureCreated(new File(getDataDirectory(), "app"));
    }

    public static File getUidListFile() {///data/user/0/io.virtualapp/virtual/data/app/system/uid-list.ini
        return new File(getSystemSecureDirectory(), "uid-list.ini");
    }

    public static File getBakUidListFile() {///data/user/0/io.virtualapp/virtual/data/app/system/uid-list.ini.bak
        return new File(getSystemSecureDirectory(), "uid-list.ini.bak");
    }

    public static File getAccountConfigFile() {///data/user/0/io.virtualapp/virtual/data/app/system/account-list.ini
        return new File(getSystemSecureDirectory(), "account-list.ini");
    }

    public static File getVirtualLocationFile() {///data/user/0/io.virtualapp/virtual/data/app/system/virtual-loc.ini
        return new File(getSystemSecureDirectory(), "virtual-loc.ini");
    }

    public static File getDeviceInfoFile() {///data/user/0/io.virtualapp/virtual/data/app/system/device-info.ini
        return new File(getSystemSecureDirectory(), "device-info.ini");
    }

    public static File getPackageListFile() {///data/user/0/io.virtualapp/virtual/data/app/system/packages.ini
        return new File(getSystemSecureDirectory(), "packages.ini");
    }

    /**
     * @return Virtual storage config file
     */
    public static File getVSConfigFile() {///data/user/0/io.virtualapp/virtual/data/app/system/vss.ini
        return new File(getSystemSecureDirectory(), "vss.ini");
    }

    public static File getBakPackageListFile() {///data/user/0/io.virtualapp/virtual/data/app/system/packages.ini.bak
        return new File(getSystemSecureDirectory(), "packages.ini.bak");
    }


    public static File getJobConfigFile() {///data/user/0/io.virtualapp/virtual/data/app/system/job-list.ini
        return new File(getSystemSecureDirectory(), "job-list.ini");
    }

    public static File getDalvikCacheDirectory() {//  /data/user/0/io.virtualapp/virtual/opt
        return DALVIK_CACHE_DIRECTORY;
    }

    public static File getOdexFile(String packageName) {//  /data/user/0/io.virtualapp/virtual/opt/data@app@
        return new File(DALVIK_CACHE_DIRECTORY, "data@app@" + packageName + "-1@base.apk@classes.dex");
    }

    public static File getDataAppPackageDirectory(String packageName) {///data/user/0/io.virtualapp/virtual/data/app/com.tentcent.qq
        return ensureCreated(new File(getDataAppDirectory(), packageName));
    }

    public static File getAppLibDirectory(String packageName) {///data/user/0/io.virtualapp/virtual/data/app/com.tentcent.qq/lib
        return ensureCreated(new File(getDataAppPackageDirectory(packageName), "lib"));
    }

    public static File getPackageCacheFile(String packageName) {///data/user/0/io.virtualapp/virtual/data/app/com.tentcent.qq/package.ini
        return new File(getDataAppPackageDirectory(packageName), "package.ini");
    }

    public static File getSignatureFile(String packageName) {///data/user/0/io.virtualapp/virtual/data/app/com.tentcent.qq/signature.ini
        return new File(getDataAppPackageDirectory(packageName), "signature.ini");
    }

    public static File getUserSystemDirectory() {//  /data/user/0/io.virtualapp/virtual/data/user
        return USER_DIRECTORY;
    }

    public static File getUserSystemDirectory(int userId) {//  /data/user/0/io.virtualapp/virtual/data/user/123
        return new File(USER_DIRECTORY, String.valueOf(userId));
    }

    public static File getWifiMacFile(int userId) {//  /data/user/0/io.virtualapp/virtual/data/user/123/wifiMacAddress
        return new File(getUserSystemDirectory(userId), "wifiMacAddress");
    }

    public static File getDataDirectory() { // /data/user/0/io.virtualapp/virtual/data
        return DATA_DIRECTORY;
    }

    public static File getSystemSecureDirectory() { ///data/user/0/io.virtualapp/virtual/data/app/system
        return ensureCreated(new File(getDataAppDirectory(), "system"));
    }

    public static File getPackageInstallerStageDir() {// /data/user/0/io.virtualapp/virtual/data/.session_dir
        return ensureCreated(new File(DATA_DIRECTORY, ".session_dir"));
    }
}