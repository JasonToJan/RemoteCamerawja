package com.jason.remotecamera_wja.parta.pictures;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.util.BitmapUtil;
import com.jason.remotecamera_wja.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

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
        setContentView(R.layout.activity_pictures_detail);

        initView();
        getIntentData();
    }

    private void initView(){
        viewPager=findViewById(R.id.detail_viewpager);
        detail_tv=findViewById(R.id.detail_tv);
    }

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
                    Bitmap bitmap=BitmapUtil.compressImageFromFile(imageurl,480,800);
                    Bitmap bitmap2=BitmapUtil.rotateBitmapByDegree(bitmap,
                            BitmapUtil.readPictureDegree(imageurl));
                    zoomImageView.setImageBitmap(bitmap2);
                }catch (Exception e){
                    e.printStackTrace();
                    DebugUtil.debug("图片压缩异常！Imageurl="+imageurl+"Uri.parse="+Uri.parse(imageurl));
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

    public static void startImageList(Context context, int position, ArrayList<String> imageList) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", position);
        bundle.putStringArrayList("imageList", imageList);
        Intent intent = new Intent(context, PicturesDetail.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
