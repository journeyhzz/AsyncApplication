package cn.com.yg.egj.asyncapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2016/7/5.
 */
public class ImageLoaderWithCaches {
    //创建Cache
    private LruCache<String, Bitmap> mLruCache;
    private ListView mListView;
    private Set<AsyncTaskImageDownload> mAsyncTaskImageDownloadSet;

    public ImageLoaderWithCaches(ListView listView) {
        mListView = listView;
        mAsyncTaskImageDownloadSet = new HashSet<>();
        //获取应用最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        //分配cache
        int cacheSize = maxMemory / 3;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用，用于
                return value.getByteCount();
            }
        };
    }

    public void showImagesByAsyncTask(String url, ImageView imageView) {
        //初始化时会从缓存中取出对应的图片
        Bitmap bitmap = getBitmapFromCaches(url);
        if (bitmap == null) {//如果缓存中没有，就设置一个默认的图片
            imageView.setImageResource(R.mipmap.ic_launcher);
//            new AsyncTaskImageDownload(imageView,url).execute(url);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    //让ListView滚动后触发下载任务。ListView停止滑动后开始加载数据的时候，获取第一个能显示的Item和最后一个可见的Item，只加载这一部分。
    public void loadImages(int start, int end) {
        for (int i = start; i < end; i++) {
            String url = Images.IMAGE_SOURSCE[i];
            //和showImagesByAsyncTask方法基本一样
            Bitmap bitmap = getBitmapFromCaches(url);
            if (bitmap == null) {
                AsyncTaskImageDownload asyncTask = new AsyncTaskImageDownload(url);
                asyncTask.execute(url);
                //把启动后的任务存储到管理表里
                mAsyncTaskImageDownloadSet.add(asyncTask);
            } else {
                ImageView imageView = (ImageView) mListView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void cancelAllTasks() {
        if (mAsyncTaskImageDownloadSet != null) {
            for (AsyncTaskImageDownload taskImageDownload : mAsyncTaskImageDownloadSet) {
                taskImageDownload.cancel(false);
            }
        }
    }
    //增加缓存到LruCache
    public void addBitmapToMemoryCaches(String url, Bitmap bitmap) {
        if (getBitmapFromCaches(url) == null) {
            mLruCache.put(url, bitmap);
        }
    }

    //从LruCache中的key获取缓存图像方法
    public Bitmap getBitmapFromCaches(String url) {
        return mLruCache.get(url);
    }

    public Bitmap getBitmap(String urlString) {
        Bitmap bitmap;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(conn.getInputStream());

            bitmap = BitmapFactory.decodeStream(inputStream);
            conn.disconnect();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) ;
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private class ImgHolder {
        public Bitmap bitmap;
        public ImageView imageView;
        public String url;

        public ImgHolder(ImageView iv, Bitmap bm, String url) {
            this.imageView = iv;
            this.bitmap = bm;
            this.url = url;
        }
    }

    private class AsyncTaskImageDownload extends AsyncTask<String, Void, Bitmap> {
        //        private ImageView mImageView;
        private String mUrl;

        //两个构造方法用于生成启动AsyncTask的对象
        public AsyncTaskImageDownload(String url) {
            mUrl = url;
        }
//之所以不用含ImageView参数的构造，是因为此类中已经含有ListView,此对象中已包含ImageView控件。
/*        public AsyncTaskImageDownload(ImageView imageView, String url) {
            mImageView = imageView;
            mUrl = url;
        }*/

        @Override
        protected Bitmap doInBackground(String... params) {
            mUrl = params[0];
            Bitmap bitmap = getBitmap(mUrl);
            //从网络获取图片
            if (bitmap != null) {
                //将不在缓存的图片加入缓存
                addBitmapToMemoryCaches(mUrl, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            //Task已经完成使命，那么就从集合中把它移除掉.
            mAsyncTaskImageDownloadSet.remove(this);
        }
    }
}
