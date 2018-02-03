package com.lody.virtual.server.am;

import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.os.Process.FIRST_APPLICATION_UID;

/**
 * @author Lody
 */

public class UidSystem {

    private static final String TAG = UidSystem.class.getSimpleName();

    private final HashMap<String, Integer> mSharedUserIdMap = new HashMap<>();
    private int mFreeUid = FIRST_APPLICATION_UID;//10000自由uid，用来给没有UID的临时添加


    public void initUidList() {
        mSharedUserIdMap.clear();
        //从uid_list.ini文件或uid_list.ini.bak中获得mFreeUid和mSharedUserIdMap
        File uidFile = VEnvironment.getUidListFile();
        if (!loadUidList(uidFile)) {
            File bakUidFile = VEnvironment.getBakUidListFile();
            loadUidList(bakUidFile);
        }
    }

    private boolean loadUidList(File uidFile) {
        if (!uidFile.exists()) {
            return false;
        }
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(uidFile));
            mFreeUid = is.readInt();
            //noinspection unchecked
            Map<String, Integer> map = (HashMap<String, Integer>) is.readObject();
            mSharedUserIdMap.putAll(map);
            is.close();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    private void save() {
        File uidFile = VEnvironment.getUidListFile();
        File bakUidFile = VEnvironment.getBakUidListFile();
        if (uidFile.exists()) {
            if (bakUidFile.exists() && !bakUidFile.delete()) {//删除备份文件
                VLog.w(TAG, "Warning: Unable to delete the expired file --\n " + bakUidFile.getPath());
            }
            try {
                FileUtils.copyFile(uidFile, bakUidFile);//重新备份一份
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(uidFile));//将freeUid和shareUIDmap存进来
            os.writeInt(mFreeUid);
            os.writeObject(mSharedUserIdMap);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getOrCreateUid(VPackage pkg) {  //获取或创建uid
        String sharedUserId = pkg.mSharedUserId;
        if (sharedUserId == null) {
            sharedUserId = pkg.packageName;
        }
        Integer uid = mSharedUserIdMap.get(sharedUserId);//根据shareUid获得uid
        if (uid != null) {
            return uid;
        }
        int newUid = ++mFreeUid;//获取不到的话，创建一个新的uid
        mSharedUserIdMap.put(sharedUserId, newUid);//放进集合里
        save();
        return newUid;
    }
}
