package io.virtualapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import com.flurry.android.FlurryAgent;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;

import io.virtualapp.delegate.MyAppRequestListener;
import io.virtualapp.delegate.MyComponentDelegate;
import io.virtualapp.delegate.MyPhoneInfoDelegate;
import io.virtualapp.delegate.MyTaskDescriptionDelegate;
import jonathanfinerty.once.Once;

/**
 * ps:fork这个项目的主要目的是为了学习和注释
 * @author Lody
 */
public class VApp extends MultiDexApplication {

    private static VApp gApp;
    private SharedPreferences mPreferences;

    public static VApp getApp() {
        return gApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mPreferences = base.getSharedPreferences("va", Context.MODE_MULTI_PROCESS);
        VASettings.ENABLE_IO_REDIRECT = true;//重定向文件路径
        VASettings.ENABLE_INNER_SHORTCUT = false;//创建桌面快捷方式
        try {
            VirtualCore.get().startup(base);//初始化第一步
        } catch (Throwable e) {
            e.printStackTrace();
        }
        //然后会执行contentprovider子类的oncreate
        //然后执行oncreate()
    }

    @Override
    public void onCreate() {
        gApp = this;
        super.onCreate();
        VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {   //VirtualApp所在的主进程
                Once.initialise(VApp.this);
                new FlurryAgent.Builder()       //统计数据信息
                        .withLogEnabled(true)
                        .withListener(() -> {
                            // nothing
                        })
                        .build(VApp.this, "48RJJP7ZCZZBB6KMMWW5");
            }

            @Override
            public void onVirtualProcess() {    //VAservice所在的进程,即:x进程
                //listener components
                virtualCore.setComponentDelegate(new MyComponentDelegate());
                //fake phone imei,macAddress,BluetoothAddress
                virtualCore.setPhoneInfoDelegate(new MyPhoneInfoDelegate());
                //fake task description's icon and title
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
            }

            @Override
            public void onServerProcess() {     //内置App所在的进程
                virtualCore.setAppRequestListener(new MyAppRequestListener(VApp.this));
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqq");
                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqqi");
                virtualCore.addVisibleOutsidePackage("com.tencent.minihd.qq");
                virtualCore.addVisibleOutsidePackage("com.tencent.qqlite");
                virtualCore.addVisibleOutsidePackage("com.facebook.katana");
                virtualCore.addVisibleOutsidePackage("com.whatsapp");
                virtualCore.addVisibleOutsidePackage("com.tencent.mm");
                virtualCore.addVisibleOutsidePackage("com.immomo.momo");
            }
        });
    }

    public static SharedPreferences getPreferences() {
        return getApp().mPreferences;
    }

}
