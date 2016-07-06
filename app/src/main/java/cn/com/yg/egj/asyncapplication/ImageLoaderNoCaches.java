package cn.com.yg.egj.asyncapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hzz on 2016/7/4.
 */
public class ImageLoaderNoCaches {
    private Handler mHandler;
    /**
     * 使用多线程方法。这里必须要调用线程的start方法才算使用多线程，否则不是。而是用的UI Thread。
     */
    public void imageUseThread(final ImageView imageView,final String url){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                ImgHolder iholder = (ImgHolder) msg.obj;
                if (iholder.imageView.getTag().equals(iholder.url)){
                    iholder.imageView.setImageBitmap(iholder.bitmap);
                }
                Log.d("Thread", "handleMessage: "+Thread.currentThread());
            }
        };
        new Thread(){
            @Override
            public void run() {
                Bitmap bitmap = getBitmap(url);
                Message message = Message.obtain();
                message.obj = new ImgHolder(imageView,bitmap,url);
                mHandler.sendMessage(message);
                Log.d("Thread", "Threadrun: "+Thread.currentThread());
            }
        }.start();

    }

    public void imageUseAsyncTask(ImageView imageView,String url){
        AsyncTaskImageDownload asyncTask = new AsyncTaskImageDownload(imageView,url);
        asyncTask.execute(url);
    }

    public  Bitmap getBitmap(String urlString){
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
        }finally {
            if (inputStream !=null);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private class ImgHolder{
        public Bitmap bitmap;
        public ImageView imageView;
        public String url;

        public ImgHolder(ImageView iv,Bitmap bm,String url){
            this.imageView = iv;
            this.bitmap = bm;
            this.url = url;
        }
    }
    private class AsyncTaskImageDownload extends AsyncTask<String,Void,Bitmap>{
        private ImageView mImageView;
        private String mUrl;
        public AsyncTaskImageDownload(ImageView imageView, String url) {
            mImageView = imageView;
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mImageView.getTag().equals(mUrl)){
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

}
