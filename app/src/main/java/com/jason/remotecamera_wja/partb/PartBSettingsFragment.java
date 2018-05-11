package com.jason.remotecamera_wja.partb;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.SharePreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * B端进行A端参数设置的片段
 */
public class PartBSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
    public static PreferenceScreen preferenceScreen;

    static UpdateListener listener;//更新了参数设置的监听器，用来B端发送指令给

    interface UpdateListener{
        void updateSetting(int flag, String value);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_b);

        preferenceScreen=getPreferenceScreen();
        initSummary(getPreferenceScreen());
        init();
    }

    /**
     * B端主页面传递一个监听器给设置页面
     * 设置页面可以通过这个监听器发送更新消息给B端主页面
     * @param mylistener
     */
    public static void passCamera(UpdateListener mylistener) {

        listener=mylistener;
    }

    /**
     * 初始化，加载的数据是自己存放在SharePreferences文件中的
     */
    public static void init() {
        String value1=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_PIC_SIZE, "640x480");
        String value2=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_FLASH_MODE, "auto");
        String value3=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_FOCUS_MODE, "auto");
        String value4=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_WHITE_BALANCE, "auto");
        String value5=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_EXPOS_COMP, "0");
        String value6=(String)SharePreferencesUtil.getParam(InitApp.AppContext,KEY_PREF_JPEG_QUALITY, "100");

        loadSupportedPictureSize(value1);
        loadSupportedFlashMode(value2);
        loadSupportedFocusMode(value3);
        loadSupportedWhiteBalance(value4);
        loadSupportedExposeCompensation(value5);
        loadSupportedJpegQuality(value6);

    }

    private static void loadSupportedPictureSize(String value) {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("640x480");
        arrayList.add("1280x720");
        arrayList.add("2560x1920");
        stringListToListPreference(arrayList, KEY_PREF_PIC_SIZE,value);
    }

    private static void loadSupportedFlashMode(String value) {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("off");
        arrayList.add("on");
        arrayList.add("auto");
        stringListToListPreference(arrayList, KEY_PREF_FLASH_MODE,value);
    }

    private static void loadSupportedFocusMode(String value) {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("auto");
        arrayList.add("macro");
        arrayList.add("infinity");
        arrayList.add("continuous-picture");
        stringListToListPreference(arrayList, KEY_PREF_FOCUS_MODE,value);
    }

    private static void loadSupportedJpegQuality(String value) {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("100");
        arrayList.add("90");
        arrayList.add("80");
        arrayList.add("70");
        arrayList.add("60");
        arrayList.add("50");
        stringListToListPreference(arrayList, KEY_PREF_JPEG_QUALITY,value);
    }

    private static void loadSupportedExposeCompensation(String valueSetting) {
        int minExposComp = -2;
        int maxExposComp = 2;
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP,valueSetting);
    }

    private static void loadSupportedWhiteBalance(String value) {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("auto");
        arrayList.add("incandescent");
        arrayList.add("fluorescent");
        arrayList.add("warm-fluorescent");
        stringListToListPreference(arrayList, KEY_PREF_WHITE_BALANCE,value);
    }

    /**
     * 字符串数组，设置默认值
     * @param list
     * @param key
     * @param value
     */
    private  static void stringListToListPreference(List<String> list, String key,String value) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        if(preferenceScreen!=null){
            ListPreference listPref = (ListPreference) preferenceScreen.findPreference(key);
            listPref.setEntries(charSeq);
            listPref.setEntryValues(charSeq);
            DebugUtil.debug("在ListPreference中，设置的value为："+value);
            listPref.setValue(value);
        }
    }

    /**
     * 当用户改变了设置中的某一项则会调用该函数
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case KEY_PREF_PIC_SIZE:
                setPictureSize(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FOCUS_MODE:
                setFocusMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FLASH_MODE:
                setFlashMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_WHITE_BALANCE:
                setWhiteBalance(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_EXPOS_COMP:
                setExposComp(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_JPEG_QUALITY:
                setJpegQuality(sharedPreferences.getString(key, ""));
                break;
        }

    }

   
    private static void setPictureSize(String value) {
        String[] split = value.split("x");
        DebugUtil.debug("我选择的长="+Integer.parseInt(split[0])+"\n宽="+Integer.parseInt(split[1]));
        listener.updateSetting(Constant.PICTUREFLAG,value);
    }

    private static void setFocusMode(String value) {
        
        DebugUtil.debug("我选择的对焦模式："+value);
        listener.updateSetting(Constant.FOCUSFALG,value);
    }

    private static void setFlashMode(String value) {
        DebugUtil.debug("我选择的闪光灯："+value);
        listener.updateSetting(Constant.FLASHFALG,value);
    }

    private static void setWhiteBalance(String value) {
        DebugUtil.debug("我选择的白平衡："+value);
        listener.updateSetting(Constant.WHITEFALG,value);
    }

    private static void setExposComp(String value) {
        DebugUtil.debug("我选择的曝光值："+value);
        listener.updateSetting(Constant.EXPOSFALG,value);
    }

    private static void setJpegQuality(String value) {
        DebugUtil.debug("我选择的照片质量："+value);
        listener.updateSetting(Constant.JPEGFALG,value);
    }

    /**
     * 加载设置的可选项
     * @param pref
     */
    private static void initSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
                initSummary(prefGroup.getPreference(i));
            }
        } else {
            updatePrefSummary(pref);
        }
    }

    private static void updatePrefSummary(Preference pref) {
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
