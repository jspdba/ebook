package top.wuchaofei.ebooktest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wuchaofei.ebooktest.util.HttpUtil;

public class ReadActiviity extends AppCompatActivity {
    private static final String TAG = "scroll";
    private ImageView bingPicImg;
    private ScrollView mScrollView;
    private int lastScrollY = 0;
    private Handler mHandler;
    private boolean isLastChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate", "onCreate method execute!!!");
        setContentView(R.layout.activity_read_activiity);

        Intent intent=getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        isLastChapter = intent.getBooleanExtra("isLastChapter", false);
        setTitle(title);
        TextView contentView = (TextView)findViewById(R.id.chapter_content);
        contentView.setText(content);
        mScrollView = (ScrollView) findViewById(R.id.content_scrollview);
        //该方法是监控对ScorllView的触碰事件
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    lastScrollY =  mScrollView.getScrollY();
                    saveLastScrollY(lastScrollY);
                    Log.i(TAG, "last time scrollY=>" + lastScrollY);
                }
                return false;
            }
        });

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }*/

       /* final SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.chapter_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBingPic();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/
        mHandler = new Handler();
    }

    /**
     * 保存上次阅读位置
     * @param lastScrollY 阅读位置
     */
    private void saveLastScrollY(int lastScrollY) {
            SharedPreferences.Editor perf = PreferenceManager.getDefaultSharedPreferences(ReadActiviity.this).edit();
            perf.putInt("lastScrollY", lastScrollY);
            perf.apply();
    }

    /**
     * 获取上次阅读位置
     * @return
     */
    private int getLastScrollY() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ReadActiviity.this);
        return prefs.getInt("lastScrollY", 0);
    }


    public static Intent starti(Context mContext, String title, String content, boolean isLastChapter){
        Intent intent = new Intent(mContext, ReadActiviity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("isLastChapter", isLastChapter);
        return intent;
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ReadActiviity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(ReadActiviity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onResume() {
        // 如果是最近一次阅读位置，则滚动到上次阅读位置
        if(isLastChapter){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.scrollTo(0, getLastScrollY());
                }
            }, 100);
        }
        super.onResume();
    }
}
