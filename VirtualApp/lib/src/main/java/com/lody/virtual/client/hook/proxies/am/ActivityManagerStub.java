package com.lody.virtual.client.hook.proxies.am;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationStub;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodInvocationProxy;
import com.lody.virtual.client.hook.base.MethodInvocationStub;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastUidMethodProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.compat.BuildCompat;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.remote.AppTaskInfo;

import java.lang.reflect.Method;
import java.util.List;

import mirror.android.app.ActivityManagerNative;
import mirror.android.app.ActivityManagerOreo;
import mirror.android.app.IActivityManager;
import mirror.android.content.pm.ParceledListSlice;
import mirror.android.os.ServiceManager;
import mirror.android.util.Singleton;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */

@Inject(MethodProxies.class)
public class ActivityManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public ActivityManagerStub() {//反射系统ActivityManagerNative获得真正的AMS，并建立本地代理方法封装起来
        super(new MethodInvocationStub<>(ActivityManagerNative.getDefault.call()));
    }

    @Override
    public void inject() throws Throwable {
        if (BuildCompat.isOreo()) {
            //Android Oreo(8.X)
            Object singleton = ActivityManagerOreo.IActivityManagerSingleton.get();
            Singleton.mInstance.set(singleton, getInvocationStub().getProxyInterface());//8.0以上系统，通过IActivityManagerSingleton字段获得服务，故将其替换为代理
        } else {
            //根据返回值的类型，处理设置代理
            if (ActivityManagerNative.gDefault.type() == IActivityManager.TYPE) {
                ActivityManagerNative.gDefault.set(getInvocationStub().getProxyInterface());
            } else if (ActivityManagerNative.gDefault.type() == Singleton.TYPE) {
                Object gDefault = ActivityManagerNative.gDefault.get();
                Singleton.mInstance.set(gDefault, getInvocationStub().getProxyInterface());
            }
        }
        //baseInterface ----  系统远端service实现了IActivityManager或singleton，执行上面的操作后，getDefault被替换成了代理，baseInterface是真实的远端service
        //获得完善的远程代理，包括代理类、远程binder、并包装了远程binder的实现供调用
        BinderInvocationStub hookAMBinder = new BinderInvocationStub(getInvocationStub().getBaseInterface());
        hookAMBinder.copyMethodProxies(getInvocationStub());//因为已经创建过了，直接复制过来
        ServiceManager.sCache.get().put(Context.ACTIVITY_SERVICE, hookAMBinder);//通过反射放进系统的service map中，系统getService流程就会被截断，调用hookAMBinder
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (VirtualCore.get().isVAppProcess()) {
            addMethodProxy(new StaticMethodProxy("navigateUpTo") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    throw new RuntimeException("Call navigateUpTo!!!!");
                }
            });
            addMethodProxy(new ReplaceLastUidMethodProxy("checkPermissionWithToken"));
            addMethodProxy(new isUserRunning());
            addMethodProxy(new ResultStaticMethodProxy("updateConfiguration", 0));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setAppLockedVerifying"));
            addMethodProxy(new StaticMethodProxy("checkUriPermission") {
                @Override
                public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
                    return PackageManager.PERMISSION_GRANTED;
                }
            });
            addMethodProxy(new StaticMethodProxy("getRecentTasks") {
                @Override
                public Object call(Object who, Method method, Object... args) throws Throwable {
                    Object _infos = method.invoke(who, args);
                    //noinspection unchecked
                    List<ActivityManager.RecentTaskInfo> infos =
                            ParceledListSliceCompat.isReturnParceledListSlice(method)
                                    ? ParceledListSlice.getList.call(_infos)
                                    : (List) _infos;
                    for (ActivityManager.RecentTaskInfo info : infos) {
                        AppTaskInfo taskInfo = VActivityManager.get().getTaskInfo(info.id);
                        if (taskInfo == null) {
                            continue;
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                info.topActivity = taskInfo.topActivity;
                                info.baseActivity = taskInfo.baseActivity;
                            } catch (Throwable e) {
                                // ignore
                            }
                        }
                        try {
                            info.origActivity = taskInfo.baseActivity;
                            info.baseIntent = taskInfo.baseIntent;
                        } catch (Throwable e) {
                            // ignore
                        }
                    }
                    return _infos;
                }
            });
        }
    }

    @Override
    public boolean isEnvBad() {
        //如果inject成功,getDefault将被替换为代理，即二者会等同
        return ActivityManagerNative.getDefault.call() != getInvocationStub().getProxyInterface();
    }

    private class isUserRunning extends MethodProxy {
        @Override
        public String getMethodName() {
            return "isUserRunning";
        }

        @Override
        public Object call(Object who, Method method, Object... args) {
            int userId = (int) args[0];
            return userId == 0;
        }
    }
}
