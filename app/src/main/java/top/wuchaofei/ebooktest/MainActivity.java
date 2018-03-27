package top.wuchaofei.ebooktest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wuchaofei.ebooktest.db.Book;
import top.wuchaofei.ebooktest.db.Chapter;
import top.wuchaofei.ebooktest.util.HttpUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String bookAddress = "http://www.biquge.tw/86_86745/";
    private String chapterListSelector="#list > dl > dd";
    private Context mContext;
    private ProgressDialog progressDialog;
    private List<Chapter> chapterList =  new ArrayList<Chapter>();
    private ChapterAdapter chapterAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button requestBtn=(Button)findViewById(R.id.request);
        requestBtn.setOnClickListener(this);
        mContext = getApplicationContext();
        getBookChapter();
        recyclerView = (RecyclerView) findViewById(R.id.chapter_list);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        chapterAdapter = new ChapterAdapter(chapterList);
        recyclerView.setAdapter(chapterAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.request:
                getBookChapter();
        }
    }

    /**
     * 远程获取章节列表
     */
    private void getBookChapter() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bookHtml = prefs.getString("bookHtml", null);
        if(TextUtils.isEmpty(bookHtml)){
            // 开启进度条
//            showProgressDialog();
            // 缓存没有则发送请求获取章节列表
            HttpUtil.sendOkHttpRequest(bookAddress, new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            closeProgressDialog();
                            chapterAdapter.notifyDataSetChanged();
                            Toast.makeText(mContext, "请求失败="+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    closeProgressDialog();
                    String resp = response.body().string();
                    parse2ChapterList(resp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chapterAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    private void parse2ChapterList(String resp) {
        Document document = Jsoup.parse(resp);
        Elements elements = document.select(chapterListSelector);
        List<Chapter>  chapterList=new ArrayList<Chapter>();
        for (Element dd : elements) {
            if(dd.childNodeSize()>0){
                Element link = dd.child(0);
                Chapter chapter=new Chapter();
                chapter.setTitle(link.text().trim());
                chapter.setLink(link.attr("href"));
                chapterList.add(chapter);
            }
        }
        Log.i("response===", chapterList.toString());
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
