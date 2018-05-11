package com.jason.remotecamera_wja.pictures;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.DebugUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 图库的预览页面，这里加载路径中的所有图片，通过异步任务来加载
 */
public class PicturesAll extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "PicturesAll";
    private GridView gridview_test = null;
    private ArrayList<String> mList = null;
    //图片缓存用来保存GridView中每个Item的图片，以便释放
    public static Map<String,Bitmap> gridviewBitmapCaches = new HashMap<String,Bitmap>();
    private MyGridViewAdapter adapter = null;

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, PicturesAll.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures_all);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();
        initData();
        setAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        initData();
        setAdapter();
        super.onResume();
    }

    private void findViews(){
        gridview_test = (GridView)findViewById(R.id.picturesAll_gv);
    }

    private void initData(){
        mList = new ArrayList<String>();
        String url = Constant.picturePath;
        getFiles(url);
    }

    /**
     * 遍历指定路径
     * @param url 遍历的路径
     */
    private void getFiles(String url){
        File files=new File(url);//创建文件对象
        File[] file=files.listFiles();
        int length=file.length;
        try {
            for(int i=length-1;i>=0;i--){//通过for循环遍历获取到的文件数组
                File f=file[i];
                if(f.isDirectory()){//如果是目录，也就是文件夹
                    getFiles(f.getAbsolutePath());//递归调用
                }else{
                    mList.add(f.getPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();//输出异常信息
        }
    }

    /**
     * 设置图库的适配器
     */
    private void setAdapter(){
        adapter = new MyGridViewAdapter(this, mList);
        gridview_test.setAdapter(adapter);
        gridview_test.setOnScrollListener(this);
        gridview_test.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", position);
                bundle.putStringArrayList("imageList", mList);
                Intent intent = new Intent(PicturesAll.this, PicturesDetail.class);
                intent.putExtras(bundle);
                startActivity(intent);
                DebugUtil.debug("点击了item"+position);
            }
        });
    }

    /**
     * 释放图片的函数
     * @param fromPosition
     * @param toPosition
     */
    private void recycleBitmapCaches(int fromPosition,int toPosition){

        Bitmap delBitmap = null;
        for(int del=fromPosition;del<toPosition;del++){
            delBitmap = gridviewBitmapCaches.get(mList.get(del));
            if(delBitmap != null){
                //如果非空则表示有缓存的bitmap，需要清理
                Log.d(TAG, "release position:"+ del);
                //从缓存中移除该del->bitmap的映射
                gridviewBitmapCaches.remove(mList.get(del));
                delBitmap.recycle();
                delBitmap = null;
            }
        }

    }

    /**
     * @param view
     * @param firstVisibleItem 第一个可见的Item的position，从0开始，随着拖动会改变
     * @param visibleItemCount 当前页面总共可见的Item的项数
     * @param totalItemCount 当前总共已经出现的Item的项数
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        recycleBitmapCaches(0,firstVisibleItem);
        recycleBitmapCaches(firstVisibleItem+visibleItemCount, totalItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
}
