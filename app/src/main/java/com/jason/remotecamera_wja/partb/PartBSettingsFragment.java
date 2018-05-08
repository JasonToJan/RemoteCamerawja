package com.jason.remotecamera_wja.partb;

import android.content.SharedPreferences;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;


public class PartBSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_PREV_SIZE = "preview_size";
    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_VIDEO_SIZE = "video_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_SCENE_MODE = "scene_mode";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
    /*static Camera mCamera;
    static Camera.Parameters mParameters;
    static CameraPreview mCameraPreview;*/
    static UpdateListener listener;

    interface UpdateListener{
        void updateSetting();
    }

    public void setUpdateListener(UpdateListener listener){
        this.listener=listener;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
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

    public static void passCamera(UpdateListener mylistener) {
       /* mCamera = camera;
        mParameters = camera.getParameters();

        mCameraPreview = cameraPreview;*/
        listener=mylistener;
    }

    public static void setDefault(SharedPreferences sharedPrefs) {
        String valPreviewSize = sharedPrefs.getString(KEY_PREF_PREV_SIZE, null);
        if (valPreviewSize == null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
            editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
            editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
            editor.putString(KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
            editor.apply();
        }
    }

    public static String getDefaultPreviewSize() {
        return "x";
    }

    private static String getDefaultPictureSize() {
        return "x";
    }

    private static String getDefaultVideoSize() {
        return "x";
    }

    private static String getDefaultFocusMode() {
        
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
        
    }
    private void loadSupportedPreviewSize() {
        cameraSizeListToListPreference(new ArrayList(), KEY_PREF_PREV_SIZE);
    }

    private void loadSupportedPictureSize() {
        cameraSizeListToListPreference(new ArrayList(), KEY_PREF_PIC_SIZE);
    }

    private void loadSupportedFlashMode() {
        stringListToListPreference(new ArrayList(), KEY_PREF_FLASH_MODE);
    }

    private void loadSupportedFocusMode() {
        stringListToListPreference(new ArrayList(), KEY_PREF_FOCUS_MODE);
    }

    private void loadSupportedWhiteBalance() {
        stringListToListPreference(new ArrayList(), KEY_PREF_WHITE_BALANCE);
    }

    private void loadSupportedSceneMode() {
        stringListToListPreference(new ArrayList(), KEY_PREF_SCENE_MODE);
    }

    private void loadSupportedExposeCompensation() {
        int minExposComp = -3;
        int maxExposComp = 3;
        List<String> exposComp = new ArrayList<>();
        for (int value = minExposComp; value <= maxExposComp; value++) {
            exposComp.add(Integer.toString(value));
        }
        stringListToListPreference(exposComp, KEY_PREF_EXPOS_COMP);
    }

    private void cameraSizeListToListPreference(List<Size> list, String key) {
        List<String> stringList = new ArrayList<>();
        for (Size size : list) {
            String stringSize = size.width + "x" + size.height;
            stringList.add(stringSize);
        }
        stringListToListPreference(stringList, key);
    }

    private void stringListToListPreference(List<String> list, String key) {
        final CharSequence[] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

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
        listener.updateSetting();

    }

    public static void setPreviewSize(String value) {
        String[] split = value.split("x");
        //mParameters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setPictureSize(String value) {
        String[] split = value.split("x");
        DebugUtil.debug("长="+Integer.parseInt(split[0])+"\n宽="+Integer.parseInt(split[1]));
        //mParameters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setFocusMode(String value) {
        
    }

    private static void setFlashMode(String value) {
       
    }

    private static void setWhiteBalance(String value) {
        
    }

    private static void setSceneMode(String value) {
        
    }

    private static void setExposComp(String value) {
        
    }

    private static void setJpegQuality(String value) {
        
    }

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
