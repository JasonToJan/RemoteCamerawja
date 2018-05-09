package com.jason.remotecamera_wja.camera;

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
import com.jason.remotecamera_wja.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;



public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_PREV_SIZE = "preview_size";
    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_VIDEO_SIZE = "video_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_SCENE_MODE = "scene_mode";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
    static Camera mCamera;
    static Camera.Parameters mParameters;
    static CameraPreview mCameraPreview;
    static UpdatepreviewListener listener;

    interface UpdatepreviewListener{
        void updatepreview();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_a);
        //getActivity().setTheme(R.style.PreferenceTheme);

        loadSupportedPreviewSize();
        loadSupportedPictureSize();
        loadSupportedFlashMode();
        loadSupportedFocusMode();
        loadSupportedWhiteBalance();
        loadSupportedSceneMode();
        loadSupportedExposeCompensation();
        initSummary(getPreferenceScreen());
    }

    public static void passCamera(CameraPreview cameraPreview,Camera camera,UpdatepreviewListener mylistener) {
        mCamera = camera;
        mParameters = camera.getParameters();
        mCameraPreview = cameraPreview;
        listener=mylistener;
    }

    public static void setUpdate(SharedPreferences sharedPrefs) {

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
        editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
        editor.putString(KEY_PREF_FLASH_MODE, getDefaultFlashMode());
        editor.putString(KEY_PREF_WHITE_BALANCE, getDefaultWhiteBlance());
        editor.putString(KEY_PREF_EXPOS_COMP, getDefaultExposure());
        editor.putString(KEY_PREF_JPEG_QUALITY, getDefaultJpegQuality());
        editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
        editor.putString(KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
        editor.apply();

    }

    public static void setDefault(SharedPreferences sharedPrefs) {

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
        DebugUtil.debug("在SettingsFragment中，setDefault方法中，预览的size为："+sharedPrefs.getString(KEY_PREF_PREV_SIZE, ""));
        editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
        editor.putString(KEY_PREF_FLASH_MODE, getDefaultFlashMode());
        editor.putString(KEY_PREF_WHITE_BALANCE, getDefaultWhiteBlance());
        editor.putString(KEY_PREF_EXPOS_COMP, getDefaultExposure());
        editor.putString(KEY_PREF_JPEG_QUALITY, getDefaultJpegQuality());
        editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
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

    public static String getDefaultVideoSize() {
        Size VideoSize = mParameters.getPreferredPreviewSizeForVideo();
        return VideoSize.width + "x" + VideoSize.height;
    }

    public static String getDefaultFocusMode() {
        List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
        if (supportedFocusModes.contains("continuous-picture")) {
            return "continuous-picture";
        }
        return "continuous-video";
    }

    public static void init(SharedPreferences sharedPref) {
        setPreviewSize(sharedPref.getString(KEY_PREF_PREV_SIZE, ""));
        setPictureSize(sharedPref.getString(KEY_PREF_PIC_SIZE, ""));
        setFlashMode(sharedPref.getString(KEY_PREF_FLASH_MODE, ""));
        setFocusMode(sharedPref.getString(KEY_PREF_FOCUS_MODE, ""));
        setWhiteBalance(sharedPref.getString(KEY_PREF_WHITE_BALANCE, ""));
        setSceneMode(sharedPref.getString(KEY_PREF_SCENE_MODE, ""));
        setExposComp(sharedPref.getString(KEY_PREF_EXPOS_COMP, ""));
        setJpegQuality(sharedPref.getString(KEY_PREF_JPEG_QUALITY, ""));

        DebugUtil.debug("在SettingsFragment中，init方法中，预览的size为："+sharedPref.getString(KEY_PREF_PREV_SIZE, ""));

        mCamera.stopPreview();
        int rotation=getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    //获取显示的旋转角度
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
        cameraSizeListToListPreference(mParameters.getSupportedPreviewSizes(), KEY_PREF_PREV_SIZE);
    }

    public void loadSupportedPictureSize() {
        cameraSizeListToListPreference(mParameters.getSupportedPictureSizes(), KEY_PREF_PIC_SIZE);
    }

    public void loadSupportedFlashMode() {
        stringListToListPreference(mParameters.getSupportedFlashModes(), KEY_PREF_FLASH_MODE);
    }

    public void loadSupportedFocusMode() {
        stringListToListPreference(mParameters.getSupportedFocusModes(), KEY_PREF_FOCUS_MODE);
    }

    public void loadSupportedWhiteBalance() {
        stringListToListPreference(mParameters.getSupportedWhiteBalance(), KEY_PREF_WHITE_BALANCE);
    }

    public void loadSupportedSceneMode() {
        stringListToListPreference(mParameters.getSupportedSceneModes(), KEY_PREF_SCENE_MODE);
    }

    public void loadSupportedExposeCompensation() {
        int minExposComp = mParameters.getMinExposureCompensation();
        int maxExposComp = mParameters.getMaxExposureCompensation();
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
    }

    public void cameraSizeListToListPreference(List<Size> list, String key) {
        List<String> stringList = new ArrayList<>();
        for (Size size : list) {
            String stringSize = size.width + "x" + size.height;
            stringList.add(stringSize);
        }
        stringListToListPreference(stringList, key);
    }

    public void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
        switch (key) {
            case KEY_PREF_PREV_SIZE:
                setPreviewSize(sharedPreferences.getString(key, ""));
                DebugUtil.debug("在onSharedPreferenceChanged中，更新的预览size为："+sharedPreferences.getString(key, ""));
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
            case KEY_PREF_SCENE_MODE:
                setSceneMode(sharedPreferences.getString(key, ""));
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
        DebugUtil.debug("长="+Integer.parseInt(split[0])+"\n宽="+Integer.parseInt(split[1]));
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

    public static void setSceneMode(String value) {
        mParameters.setSceneMode(value);
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
