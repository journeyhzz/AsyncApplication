package cn.com.yg.egj.asyncapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by hzz on 2016/7/4.
 */
public class AdapterNoCaches extends BaseAdapter {
    private List<String> mData;
    private LayoutInflater mLayoutInflater;
    private ImageLoaderNoCaches mLoaderNoCaches;

    public AdapterNoCaches(Context context, List<String> data) {
        mLayoutInflater = LayoutInflater.from(context);//根据context取得LayoutInflater,为后面加载view_items布局做准备
        this.mData = data;
        mLoaderNoCaches = new ImageLoaderNoCaches();

    }

    @Override
    public int getCount() {
        //指定一共包含多少个选项
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;//返回值作为列表的ID
    }

    @Override //该方法返回的View作为列表框
    public View getView(int position, View convertView, ViewGroup parent) {
        String url = mData.get(position);
        //判断是否有缓存
        ImageHolder imageHolder = null;
/*        当我们判断 convertView == null  的时候，如果为空，就会根据设计好的List的Item布局（XML）进行转化为View，来为convertView赋值，
        并生成一个viewHolder来绑定converView里面的各个View控件（XML布局里面的那些控件）。再用convertView的setTag将viewHolder设置到Tag中，
        以便系统第二次绘制ListView时从Tag中取出。
        如果convertView不为空的时候，就会直接用convertView的getTag()，来获得一个ViewHolder。*/
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.view_items, null);
            imageHolder = new ImageHolder();
            imageHolder.mImageView = (ImageView) convertView.findViewById(R.id.view_items);//通过上面layout得到的view来获取里面的具体控件
            convertView.setTag(imageHolder);
        } else {
            imageHolder = (ImageHolder) convertView.getTag();
        }
        //把Holder中和某一个控件相关联的url（相当于key）设置到Tag中，方便以后取出。
        imageHolder.mImageView.setTag(url);
        //设置布局中控件（ImageView）要显示的视图
        imageHolder.mImageView.setImageResource(R.mipmap.ic_launcher);//默认显示的图片
        /**use Thread loads
         * */
//        mLoaderNoCaches.imageUseThread(imageHolder.mImageView,url);
        /**
         * use AsyncTask  经测试，用此方法，在没有用缓存机制的情况下，它比用Thread方法效果要差些。
         */
        mLoaderNoCaches.imageUseAsyncTask(imageHolder.mImageView,url);

        return convertView;
    }

    public class ImageHolder {
        public ImageView mImageView;
    }
}
