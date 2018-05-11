package com.jason.remotecamera_wja.pictures;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.util.BitmapUtil;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.FileUtil;
import com.jason.remotecamera_wja.util.ToastUtil;

import java.util.List;

/**
 * 大图预览的详细页面，通过ViewPager来实现分页效果
 */
public class PicturesDetail extends AppCompatActivity implements ViewPager.OnPageChangeListener{


    private ViewPager viewPager;//管理图片滑动
    private TextView detail_tv;//设置当前图片的页数
    private String imageurl;
    private List<String> imageList;//传递过来的url数组
    private int index;//当前选择的图片索引
    private int page;//当前页数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppUtil.fullScreen(this);
        setContentView(R.layout.activity_pictures_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        getIntentData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.pictures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.picture_details:
                readPicturesDetail(imageList.get(page));
                //ToastUtil.showToast(PicturesDetail.this,"当前图片链接："+imageList.get(page));
                break;
            case R.id.picture_delete:
                showISDeleteDialog(imageList.get(page));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        viewPager=findViewById(R.id.detail_viewpager);
        detail_tv=findViewById(R.id.detail_tv);
    }

    /**
     * 接收到图库列表传过来的索引和图片地址数组
     */
    private void getIntentData(){
        Bundle bundle = getIntent().getExtras();
        index = bundle.getInt("index");
        imageList = bundle.getStringArrayList("imageList");
        imageurl = imageList.get(index);
        MyPageAdapter myPageAdapter=new MyPageAdapter(this);
        viewPager.setAdapter(myPageAdapter);
        viewPager.setCurrentItem(index);
        viewPager.setOnPageChangeListener(this);
        viewPager.setEnabled(true);
        detail_tv.setText((index + 1) + " / " + imageList.size());
    }

    /**
     * 读取图片详情，通过一个ExifInterface来读取图片的信息
     * @param path
     */
    private void readPicturesDetail(String path){
        //android读取图片EXIF信息
        try {
            ExifInterface exifInterface=new ExifInterface(path);

            //执行保存
            exifInterface.saveAttributes();
            //获取图片的方向
            String orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            //获取图片的时间
            String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            String flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            String iso=exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
            String height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            String width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);

            String orientation_real="";
            switch (Integer.parseInt(orientation)){
                case 0:
                    orientation_real="0度";
                    break;
                case 6:
                    orientation_real="顺时针90度";
                    break;
                case 1:
                    orientation_real="0度";
                    break;
                case 8:
                    orientation_real="逆时针90度";
                    break;
                case 3:
                    orientation_real="180度";
                    break;
            }

            String flash_real="";
            switch(Integer.parseInt(flash)){
                case 0:
                    flash_real="未开启";
                    break;
                case 1:
                    flash_real="开启";
                    break;

            }

            String[] list=new String[8];
            list[0]="旋转角度："+orientation_real;
            list[1]="拍摄日期："+dateTime;
            list[2]="拍摄品牌："+make;
            list[3]="设备型号："+model;
            list[4]="闪光灯："+flash_real;
            list[5]="感光度："+iso;
            list[6]="图片高度："+height;
            list[7]="图片宽度："+width;

            showListAlertDialog(list);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 本应用图片适配器
     */
    class MyPageAdapter extends PagerAdapter {

        public Context context;

        MyPageAdapter(Context context){
            this.context=context;
        }

        @Override
        public int getCount() {
            if (imageList == null || imageList.size() == 0) {
                return 0;
            }
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.viewpager_image, container, false);
            ImageView zoomImageView = view.findViewById(R.id.zoom_image_view);
            imageurl=imageList.get(position);

            if (!TextUtils.isEmpty(imageurl)) {
                try{
                    Bitmap bitmap=BitmapUtil.compressImageFromFile(imageurl,600,800);
                    Bitmap bitmap2=BitmapUtil.rotateBitmapByDegree(bitmap,
                            BitmapUtil.readPictureDegree(imageurl));
                    zoomImageView.setImageBitmap(bitmap2);
                }catch (Exception e){
                    e.printStackTrace();
                    DebugUtil.debug("图片压缩异常！Imageurl="+imageurl+"Uri.parse="+ Uri.parse(imageurl));
                }
            }
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    /**
     * 本方法主要监听viewpager滑动的时候的操作
     */
    @Override
    public void onPageSelected(int arg0) {
        // 每当页数发生改变时重新设定一遍当前的页数和总页数
        detail_tv.setText((arg0 + 1) + " / " + imageList.size());
        page = arg0;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    // 信息列表提示框
    private AlertDialog alertDialog1;
    public void showListAlertDialog(final String[] list){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int index) {
                ToastUtil.showToast(InitApp.AppContext,list[index]);
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    public void showISDeleteDialog(final String path){
        final AlertDialog.Builder builder = new AlertDialog.Builder(PicturesDetail.this);
        builder.setTitle("提示");
        builder.setMessage("确定删除吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FileUtil.deleteFile(path);
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("忽略", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //    显示出该对话框
        builder.show();
    }
}
