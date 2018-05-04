package com.jason.remotecamera_wja.sqlite.sqlitea;

public class PictureBean {

    private String uri;
    private String createTime;
    private String picture_size;
    private String falsh_mode;
    private String focus_mode;
    private String white_balance;
    private String scene_mode;
    private String exposure_compensation;
    private String jpeg_quality;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPicture_size() {
        return picture_size;
    }

    public void setPicture_size(String picture_size) {
        this.picture_size = picture_size;
    }

    public String getFalsh_mode() {
        return falsh_mode;
    }

    public void setFalsh_mode(String falsh_mode) {
        this.falsh_mode = falsh_mode;
    }

    public String getFocus_mode() {
        return focus_mode;
    }

    public void setFocus_mode(String focus_mode) {
        this.focus_mode = focus_mode;
    }

    public String getWhite_balance() {
        return white_balance;
    }

    public void setWhite_balance(String white_balance) {
        this.white_balance = white_balance;
    }

    public String getScene_mode() {
        return scene_mode;
    }

    public void setScene_mode(String scene_mode) {
        this.scene_mode = scene_mode;
    }

    public String getExposure_compensation() {
        return exposure_compensation;
    }

    public void setExposure_compensation(String exposure_compensation) {
        this.exposure_compensation = exposure_compensation;
    }

    public String getJpeg_quality() {
        return jpeg_quality;
    }

    public void setJpeg_quality(String jpeg_quality) {
        this.jpeg_quality = jpeg_quality;
    }

    @Override
    public String toString() {
        return "PictureBean{" +
                "uri='" + uri + '\'' +
                ", createTime='" + createTime + '\'' +
                ", picture_size='" + picture_size + '\'' +
                ", falsh_mode='" + falsh_mode + '\'' +
                ", focus_mode='" + focus_mode + '\'' +
                ", white_balance='" + white_balance + '\'' +
                ", scene_mode='" + scene_mode + '\'' +
                ", expos_comp='" + exposure_compensation + '\'' +
                ", jpeg_quality='" + jpeg_quality + '\'' +
                '}';
    }
}
