package com.jason.remotecamera_wja;

import android.app.Application;
import android.content.Context;

import com.jason.remotecamera_wja.util.CrashHandlerUtil;

/**
 * 全局入口
 */
public class InitApp extends Application {

    public static Context AppContext;

    @Override
    public void onCreate(){
        super.onCreate();

        AppContext=getApplicationContext();

        //错误日志分析
        CrashHandlerUtil.getInstance().init(AppContext);


    }

}
