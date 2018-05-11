package com.jason.remotecamera_wja.util;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.jason.remotecamera_wja.R;

/**
 * 对话框工具类
 */
public class DialogUtil {

    /**
     * DialogUtil实例
     */
    private static DialogUtil mDialogUtil;

    /**
     * Dialog实例
     */
    public Dialog progressDialog;

    /**
     * 获取DialogUtil单例
     */
    public static DialogUtil getInstance()
    {
        if (mDialogUtil == null)
        {
            mDialogUtil = new DialogUtil();
        }
        return mDialogUtil;
    }

    /**
     * 销毁DialogUtil
     */
    public static void destroyDialogUtil(){
        if(mDialogUtil!=null) {
            mDialogUtil = null;
        }
    }

    /**
     * 关闭提示对话框
     */
    public void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

    public  void showLoading(Context context) {
        progressDialog = new Dialog(context,R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("连接中...");
        progressDialog.show();
    }

    public void tokeAPhoto(Context context) {
        progressDialog = new Dialog(context,R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("正在控制A端拍照...");
        progressDialog.show();
    }

    public  void makeWifi(Context context) {
        progressDialog = new Dialog(context,R.style.progress_dialog);
        progressDialog.setContentView(R.layout.dialog);
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("正在创建热点...");
        progressDialog.show();
    }

}
