package com.jason.remotecamera_wja.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.DialogUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 创建热点
 */
public class WifiApAdmin {

    public static final String TAG = "WifiApAdmin";

    public static void closeWifiAp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        closeWifiAp(wifiManager);
    }

    private WifiManager mWifiManager = null;

    private Context mContext = null;
    public WifiApAdmin(Context context) {
        mContext = context;

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        closeWifiAp(mWifiManager);
    }

    private String mSSID = "";
    private String mPasswd = "";
    public void startWifiAp(String ssid, String passwd) {
        mSSID = ssid;
        mPasswd = passwd;

        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        //startWifiAp(ssid,passwd,true);
        //createWifiHotspot();
        createWifi(false);

        MyTimerCheck timerCheck = new MyTimerCheck() {

            @Override
            public void doTimerCheckWork() {

                if (isWifiApEnabled(mWifiManager)) {
                    Log.v(TAG, "Wifi enabled success!");
                    DialogUtil.getInstance().closeDialog();
                    this.exit();
                } else {
                    Log.v(TAG, "Wifi enabled failed!");
                }
            }

            @Override
            public void doTimeOutWork() {
                this.exit();
            }
        };
        timerCheck.start(15, 1000);

    }

    public boolean createWifi(boolean isOpen) {
        //如果wifi已经打开则先关闭wifi
        closeWifiAp(mWifiManager);
        //  Method method1=null;
        try {

            WifiConfiguration netConfig=new WifiConfiguration();

            netConfig.SSID= Constant.WIFISSID;
            netConfig.preSharedKey=Constant.WIFIPASSWORD;
            Log.d(TAG, "WifiPresenter：createAp----->netConfig.SSID:"
                    +netConfig.SSID+",netConfig.preSharedKey:"+netConfig.preSharedKey+",isOpen="+isOpen);
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            if (isOpen) {
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }else {
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            }
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                //保存ap设置
                Method methodsave = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                methodsave.invoke(mWifiManager, netConfig);
                //获取ConnectivityManager对象，便于后面使用
                ConnectivityManager connManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                Field field = connManager.getClass().getDeclaredField("TETHERING_WIFI");
                field.setAccessible(true);
                int mTETHERING_WIFI = (int)field.get(connManager);

                Field iConnMgrField = connManager.getClass().getDeclaredField("mService");
                iConnMgrField.setAccessible(true);
                Object iConnMgr = iConnMgrField.get(connManager);
                Class<?> iConnMgrClass = Class.forName(iConnMgr.getClass().getName());
                Method startTethering = iConnMgrClass.getMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                startTethering.invoke(iConnMgr, mTETHERING_WIFI, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                    }
                }, true);
                return true;

            } else {
                Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                return (boolean) method.invoke(mWifiManager, netConfig, true);
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void closeWifiAp(WifiManager wifiManager) {
        if (isWifiApEnabled(wifiManager)) {
            try {
                Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);

                WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);

                Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(wifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
