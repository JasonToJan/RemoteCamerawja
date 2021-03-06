package com.jason.remotecamera_wja.pictures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.util.BitmapUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 相册主页面的适配器，每一个Item规定了默认的长宽
 */
public class MyGridViewAdapter extends BaseAdapter {

    private Context mContext = null;
    private LayoutInflater mLayoutInflater = null;
    private List<String> mList = null;

    private int width = 120;//每个Item的宽度,可以根据实际情况修改
    private int height = 150;//每个Item的高度,可以根据实际情况修改

    public static class MyGridViewHolder{
        public ImageView imageview_thumbnail;
        public TextView textview_test;
    }

    public MyGridViewAdapter(Context context,List<String> list) {
        this.mContext = context;
        this.mList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyGridViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new MyGridViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.layout_my_gridview_item, null);
            viewHolder.imageview_thumbnail = (ImageView)convertView.findViewById(R.id.imageview_thumbnail);
            viewHolder.textview_test = (TextView)convertView.findViewById(R.id.textview_test);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (MyGridViewHolder)convertView.getTag();
        }

        String url = mList.get(position);
        //首先我们先通过cancelPotentialLoad方法去判断imageview是否有线程正在为它加载图片资源，
        //如果有现在正在加载，那么判断加载的这个图片资源（url）是否和现在的图片资源一样，不一样则取消之前的线程（之前的下载线程作废）。
        //见下面cancelPotentialLoad方法代码
        if (cancelPotentialLoad(url, viewHolder.imageview_thumbnail)) {
            AsyncLoadImageTask task = new AsyncLoadImageTask(viewHolder.imageview_thumbnail);
            LoadedDrawable loadedDrawable = new LoadedDrawable(task);
            viewHolder.imageview_thumbnail.setImageDrawable(loadedDrawable);
            task.execute(position);
        }
        viewHolder.textview_test.setText((position+1)+"");
        return convertView;
    }

    /**
     * 通过url 获取图片
     * @param url
     * @return
     */
    private Bitmap getBitmapFromUrl(String url){
        Bitmap bitmap = null;
        bitmap = PicturesAll.gridviewBitmapCaches.get(url);
        if(bitmap != null){
            return bitmap;
        }
        try {
            FileInputStream is = new FileInputStream(url);
            //用decodeFileDescriptor()方法来生成bitmap会节省内存
            bitmap = BitmapFactory.decodeFileDescriptor(is.getFD());
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bitmap = BitmapUtil.getBitmapThumbnail(bitmap,width,height);
        return bitmap;
    }

    /**
     * 加载图片的异步任务
     */
    private class AsyncLoadImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private String url = null;
        private final WeakReference<ImageView> imageViewReference;

        public AsyncLoadImageTask(ImageView imageview) {
            super();
            imageViewReference = new WeakReference<ImageView>(imageview);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {

            Bitmap bitmap = null;
            this.url = mList.get(params[0]);
            bitmap=getBitmapFromUrl(url);
            PicturesAll.gridviewBitmapCaches.put(mList.get(params[0]), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultBitmap) {

            if(isCancelled()){
                resultBitmap = null;
            }
            if(imageViewReference != null){
                ImageView imageview = imageViewReference.get();
                AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);
                if (this == loadImageTask) {
                    imageview.setImageBitmap(resultBitmap);
                    imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
            super.onPostExecute(resultBitmap);
        }
    }

    /**
     * 是否有线程正在为他加载图片资源
     * @param url
     * @param imageview
     * @return
     */
    private boolean cancelPotentialLoad(String url,ImageView imageview){
        AsyncLoadImageTask loadImageTask = getAsyncLoadImageTask(imageview);

        if (loadImageTask != null) {
            String bitmapUrl = loadImageTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                loadImageTask.cancel(true);
            } else {
                // 相同的url已经在加载中.
                return false;
            }
        }
        return true;

    }

    /**
     * 获取异步加载图片任务
     * @param imageview
     * @return
     */
    private AsyncLoadImageTask getAsyncLoadImageTask(ImageView imageview){
        if (imageview != null) {
            Drawable drawable = imageview.getDrawable();
            if (drawable instanceof LoadedDrawable) {
                LoadedDrawable loadedDrawable = (LoadedDrawable)drawable;
                return loadedDrawable.getLoadImageTask();
            }
        }
        return null;
    }

    /**
     * 该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
     */
    public static class LoadedDrawable extends ColorDrawable {
        private final WeakReference<AsyncLoadImageTask> loadImageTaskReference;

        public LoadedDrawable(AsyncLoadImageTask loadImageTask) {
            super(Color.TRANSPARENT);
            loadImageTaskReference = new WeakReference<AsyncLoadImageTask>(loadImageTask);
        }

        public AsyncLoadImageTask getLoadImageTask() {
            return loadImageTaskReference.get();
        }

    }
}
