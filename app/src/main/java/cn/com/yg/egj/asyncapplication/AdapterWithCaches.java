package cn.com.yg.egj.asyncapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by hzz on 2016/7/5.
 */
public class AdapterWithCaches extends BaseAdapter implements AbsListView.OnScrollListener {
    private List<String> mData;
    private LayoutInflater mLayoutInflater;
    private ImageLoaderWithCaches mImageLoaderWithCaches;
    private int mStart=0;
    private int mEnd=0;
    private static String[] URLS;//用来保存当前我们所获取到的所有已经加载到的图片的地址。
    private boolean mFirsFlag = true; //判断当前是否是第一次加载，如果不设置，那么第一次启动后默认也不显示图片。自由滑动后才显示。

    public AdapterWithCaches(Context context, List<String> data, ListView listView) {
        mLayoutInflater = LayoutInflater.from(context);//根据context取得LayoutInflater,为后面加载view_items布局做准备
        this.mData = data;
        mImageLoaderWithCaches = new ImageLoaderWithCaches(listView);
        //把传进来的ListView注册监听事件，否则图片不能正常显示
        listView.setOnScrollListener(this);

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
        imageHolder.mImageView.setImageResource(R.mipmap.ic_launcher);
        mImageLoaderWithCaches.showImagesByAsyncTask(url,imageHolder.mImageView);

        return convertView;
    }
    //滚动的时候加载数据没有意义。就让她开心的滚动吧。等滚动完毕后再根据第一项和最后一项进行加载。
//第一次初始化的时候，一定要手动来加载图片，不然系统判断你没滚，只能调用onScroll方法，不会调用onScrollStateChanged方法。
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE){//当前状态是滚动停止状态
            //加载可见项
            mImageLoaderWithCaches.loadImages(mStart,mEnd);
        }else {
            //停止加载任务
            mImageLoaderWithCaches.cancelAllTasks();

        }

    }
    //在onScroll方法中来不断获取可见的Item。特别要注意的是visibleItemCount，它只要大于0的时候，才认为是开始显示图片了。
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem+visibleItemCount;
        if (mFirsFlag && visibleItemCount> 0){
            mImageLoaderWithCaches.loadImages(mStart,mEnd);
            //第一次加载的标志只有第一次才使用，以后就不会使用了。所以，要false掉。
            mFirsFlag = false;
        }

    }

    public class ImageHolder {
        public ImageView mImageView;
    }
}
