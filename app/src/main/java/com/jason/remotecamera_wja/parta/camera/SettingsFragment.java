package com.jason.remotecamera_wja.parta.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A端进行设置相机参数的一个片段，继承自PreferenceFragment，实现监听条目改变事件
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_PREV_SIZE = "preview_size";
    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
    static Camera mCamera;
    static Camera.Parameters mParameters;
    static CameraPreview mCameraPreview;
    static UpdatepreviewListener listener;

    /**
     * 更新预览的接口，当用户改变了预览分辨率，这里设置回调来更新界面
     */
    interface UpdatepreviewListener{
        void updatepreview();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_a);

        //导入可选值
        loadSupportedPreviewSize();
        loadSupportedPictureSize();
        loadSupportedFlashMode();
        loadSupportedFocusMode();
        loadSupportedWhiteBalance();
        loadSupportedExposeCompensation();
        loadSupportedJpegQuality();
        initSummary(getPreferenceScreen());
    }

    /**
     * 传递自定义SurfaceView实例，相机实例，监听器对象给当前类
     * @param cameraPreview
     * @param camera
     * @param mylistener
     */
    public static void passCamera(CameraPreview cameraPreview,Camera camera,UpdatepreviewListener mylistener) {
        mCamera = camera;
        mParameters = camera.getParameters();
        mCameraPreview = cameraPreview;
        listener=mylistener;
    }

    /**
     * 当B端发送修改设置的消息后，需要更新SharedPreferences文件中的数据
     * @param sharedPrefs
     */
    public static void setUpdate(SharedPreferences sharedPrefs) {

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
        editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
        editor.putString(KEY_PREF_FLASH_MODE, getDefaultFlashMode());
        editor.putString(KEY_PREF_WHITE_BALANCE, getDefaultWhiteBlance());
        editor.putString(KEY_PREF_EXPOS_COMP, getDefaultExposure());
        editor.putString(KEY_PREF_JPEG_QUALITY, getDefaultJpegQuality());
        editor.putString(KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
        editor.apply();

    }

    public static String getDefaultPreviewSize() {
        Size previewSize = mParameters.getPreviewSize();
        return previewSize.width + "x" + previewSize.height;
    }

    public static String getDefaultPictureSize() {
        Size pictureSize = mParameters.getPictureSize();
        return pictureSize.width + "x" + pictureSize.height;
    }

    public static String getDefaultFlashMode() {
        String flashMode = mParameters.getFlashMode();
        return flashMode;
    }

    public static String getDefaultWhiteBlance() {
        String whiteBalance = mParameters.getWhiteBalance();
        return whiteBalance;
    }

    public static String getDefaultExposure() {
        int exposureCompensation = mParameters.getExposureCompensation();
        return String.valueOf(exposureCompensation);
    }

    public static String getDefaultJpegQuality() {
        int jpegQuality = mParameters.getJpegQuality();
        return String.valueOf(jpegQuality);
    }

    public static String getDefaultFocusMode() {
        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
        if (supportedFocusModes.contains("continuous-picture")) {
            return "continuous-picture";
        }
        return "continuous-video";
    }

    /**
     * 初始化操作，用于设置相机的属性，打开相机页面会调用
     * @param sharedPref
     */
    public static void init(SharedPreferences sharedPref) {
        setPreviewSize(sharedPref.getString(KEY_PREF_PREV_SIZE, "640x480"));
        setPictureSize(sharedPref.getString(KEY_PREF_PIC_SIZE, "640x480"));
        setFlashMode(sharedPref.getString(KEY_PREF_FLASH_MODE, "auto"));
        setFocusMode(sharedPref.getString(KEY_PREF_FOCUS_MODE, "auto"));
        setWhiteBalance(sharedPref.getString(KEY_PREF_WHITE_BALANCE, "auto"));
        setExposComp(sharedPref.getString(KEY_PREF_EXPOS_COMP, "0"));
        setJpegQuality(sharedPref.getString(KEY_PREF_JPEG_QUALITY, "100"));

        mCamera.stopPreview();
        int rotation=getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        //Camera.Parameters parameters = mCamera.getParameters();
        mParameters.setRotation(rotation);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    /**
     * 获取相机预览的旋转角度
     * @return
     */
    public static int getDisplayOrientation() {

        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);


        Display display = ((WindowManager) InitApp.AppContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    public void loadSupportedPreviewSize() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("640x480");
        arrayList.add("1280x720");
        arrayList.add("1088x1088");
        arrayList.add("1440x1080");
        stringListToListPreference(arrayList, KEY_PREF_PREV_SIZE);
    }

    public void loadSupportedPictureSize() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("640x480");
        arrayList.add("1280x720");
        arrayList.add("2560x1920");
        stringListToListPreference(arrayList, KEY_PREF_PIC_SIZE);
    }

    public void loadSupportedFlashMode() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("off");
        arrayList.add("on");
        arrayList.add("auto");
        stringListToListPreference(arrayList, KEY_PREF_FLASH_MODE);
    }

    public void loadSupportedFocusMode() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("auto");
        arrayList.add("macro");
        arrayList.add("infinity");
        arrayList.add("continuous-picture");
        stringListToListPreference(arrayList, KEY_PREF_FOCUS_MODE);
    }

    public void loadSupportedWhiteBalance() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("auto");
        arrayList.add("incandescent");
        arrayList.add("fluorescent");
        arrayList.add("warm-fluorescent");
        stringListToListPreference(arrayList, KEY_PREF_WHITE_BALANCE);
    }

    public void loadSupportedExposeCompensation() {
        int minExposComp = -2;
        int maxExposComp = 2;
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
    }

    public void loadSupportedJpegQuality() {
        ArrayList<String> arrayList=new ArrayList<>();
        arrayList.add("100");
        arrayList.add("90");
        arrayList.add("80");
        arrayList.add("70");
        arrayList.add("60");
        arrayList.add("50");
        stringListToListPreference(arrayList, KEY_PREF_JPEG_QUALITY);
    }

    /**
     * 将字符串数组添加到文件中，只是设置了可选项，还未加载用户自己选的值
     * @param list
     * @param key
     */
    public void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    /**
     * 当用户改变了设置中的某一项会运行该函数
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        switch (key) {
            case KEY_PREF_PREV_SIZE:
                setPreviewSize(sharedPreferences.getString(key, ""));
                break;
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
        mCamera.stopPreview();
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
        listener.updatepreview();

    }

    public static void setPreviewSize(String value) {
        String[] split = value.split("x");
        mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static void setPictureSize(String value) {
        String[] split = value.split("x");
        mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public static void setFocusMode(String value) {
        mParameters.setFocusMode(value);
    }

    public static void setFlashMode(String value) {
        mParameters.setFlashMode(value);
    }

    public static void setWhiteBalance(String value) {
        mParameters.setWhiteBalance(value);
    }

    public static void setExposComp(String value) {
        mParameters.setExposureCompensation(Integer.parseInt(value));
    }

    public static void setJpegQuality(String value) {
        mParameters.setJpegQuality(Integer.parseInt(value));
    }

    public static void initSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
                initSummary(prefGroup.getPreference(i));
            }
        } else {
            updatePrefSummary(pref);
        }
    }

    /**
     * 设置条目的值
     * @param pref
     */
    public static void updatePrefSummary(Preference pref) {
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
