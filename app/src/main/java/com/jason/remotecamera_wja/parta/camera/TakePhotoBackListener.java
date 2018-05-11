package com.jason.remotecamera_wja.parta.camera;

/**
 * Created by jasonjan on 2018/5/6.
 */

/**
 * A端接收到B端的拍照请求后，拍照完成后的回调，为了发送图片流给B端，参数为图片文件的地址
 */
public interface TakePhotoBackListener {
    void uploadPictureToB(String fileName);
}
