package cn.com.yg.egj.asyncapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private ListView mListView;
    private List<String> mData;
//    private AdapterNoCaches mAdapterNoCaches;
    private AdapterWithCaches mAdapterWithCaches;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.list_view);
        mData = Arrays.asList(Images.IMAGE_SOURSCE);
//        mAdapterNoCaches = new AdapterNoCaches(this,mData);
        mAdapterWithCaches = new AdapterWithCaches(this,mData,mListView);
        mListView.setAdapter(mAdapterWithCaches);
        Log.d("Thread", "onCreate: "+Thread.currentThread());

    }
}
