package top.wuchaofei.ebooktest;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wuchaofei.ebooktest.db.Chapter;
import top.wuchaofei.ebooktest.service.ThreadDownloadService;
import top.wuchaofei.ebooktest.util.Constant;
import top.wuchaofei.ebooktest.util.HttpUtil;

public class MainActivity extends AppCompatActivity{
    public static final String bookAddress = "https://www.xs.la/86_86745/";
    public static final int bookId = 1;
    private Context mContext;
    private ProgressDialog progressDialog;
    private List<Chapter> chapterList =  new ArrayList<Chapter>();
    private ChapterAdapter chapterAdapter;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ThreadDownloadService.DownloadBinder downloadBinder;
    ServiceConnection connection;
    // service 发送消息给handler
    public static Handler handler;
//    SQLScout 插件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
//        DataSupport.deleteAll(Chapter.class);
        initBookChapters();
        recyclerView = (RecyclerView) findViewById(R.id.chapter_list);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        //添加Android自带的分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        chapterAdapter = new ChapterAdapter(chapterList);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int position = prefs.getInt("read_position", 0);
        recyclerView.scrollToPosition(position);
        chapterAdapter.setOnItemClickListener(new ChapterAdapter.OnItemClickListener() {
            @Override
            public void onClick(final int position) {
                final Chapter chapter = chapterList.size()-1>=position?chapterList.get(position):null;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                int read_position = prefs.getInt("read_position", 0);
                final boolean isLastPosition = read_position==position?true:false;
                // 保存本次阅读位置
                SharedPreferences.Editor perf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                perf.putInt("read_position", position);
                perf.apply();
//                滚动到点击位置
                if (position != -1) {
                    recyclerView.scrollToPosition(position);
                    LinearLayoutManager mLayoutManager =(LinearLayoutManager) recyclerView.getLayoutManager();
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                }

                if(TextUtils.isEmpty(chapter.getContent())){
                    if(!TextUtils.isEmpty(chapter.getLink())){
                        if(TextUtils.isEmpty(chapter.getContent())){
                            showProgressDialog();

                            HttpUtil.sendOkHttpRequest(chapter.getLink(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgressDialog();
                                            Toast.makeText(mContext,"请求失败 "+chapter.getTitle(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String resp = response.body().string();
                                    String content = parseContent(resp);
                                    chapter.setContent(content);
                                    chapter.save();
                                    chapterList.set(position, chapter);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeProgressDialog();
                                            chapterAdapter.notifyDataSetChanged();
                                            startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent(), isLastPosition));
                                        }
                                    });
                                }
                            });
                        }else{
                            startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent(),isLastPosition));
                        }
                    }
                }else{
                    startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent(),isLastPosition));
                }
            }
        });
        recyclerView.setAdapter(chapterAdapter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(mContext, "refresh", Toast.LENGTH_SHORT).show();
                chapterList = DataSupport.findAll(Chapter.class);
                chapterAdapter.notifyDataSetChanged();
                downloadBinder.startDownload(10);
                swipeRefresh.setRefreshing(false);
            }
        });
        // 绑定下载服务
        Intent intent=new Intent(this, ThreadDownloadService.class);
        startService(intent);// 启动服务
        connection=new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("service", "onServiceConnected");
                downloadBinder = (ThreadDownloadService.DownloadBinder)iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("service", "onServiceDisconnected");
                downloadBinder = null;
            }
        };
        bindService(intent, connection, BIND_AUTO_CREATE);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if(message.arg1==1){
                    chapterList=DataSupport.findAll(Chapter.class);
                    chapterAdapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
    }


    public static String parseContent(String resp) {
        Document document = Jsoup.parse(resp);
        Elements elements = document.select(Constant.BOOK_CONTENT_SELECTOR);
        String str = elements.html();
        str = str.replaceAll("&nbsp;", " ").replaceAll("<br>","").replace("\\?", " ");
        return str==null?str:str.trim();
    }

    /**
     * 远程获取章节列表
     */
    private void initBookChapters() {
        chapterList = DataSupport.findAll(Chapter.class);
//        DataSupport.deleteAll(Chapter.class, null);
//        if(chapterList == null || chapterList.size()==0 || updateLastTime()){
        if(chapterList == null || chapterList.size()==0){
            // 开启进度条
            showProgressDialog();
            // 缓存没有则发送请求获取章节列表
            HttpUtil.sendOkHttpRequest(bookAddress, new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(mContext, "请求失败="+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body().string();
                    Log.d("resp===",resp);
                    updateBookChapters(resp);

                }
            });
        }
    }

    /**
     * 是否需要更新
     * @return true需要更新，false不需要更新
     */
    private boolean updateLastTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long lastUpdateTime = prefs.getLong("lastUpdateTime", 0);
        long currentTime = System.currentTimeMillis();

        // 一天更新一次
        if(currentTime-lastUpdateTime>1000*60*60*24){
            SharedPreferences.Editor perf = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            perf.putLong("lastUpdateTime", currentTime);
            perf.apply();
            return true;
        }
        return false;

    }

    private void updateBookChapters(String resp) {
        Document document = Jsoup.parse(resp);
        Elements elements = document.select(Constant.BOOK_CHAPTER_SELECTOR);
        List<Chapter>  onLineChapterList = new ArrayList<Chapter>();
        int index = 0;
        for (Element dd : elements) {
            if(dd.childNodeSize()>0){
                Element link = dd.child(0);
                Chapter chapter = new Chapter();
                chapter.setBookId(bookId);
                chapter.setOrderIndex(index++);
                chapter.setTitle(link.text().trim());
                chapter.setLink(parsePath(bookAddress, link.attr("href")));
                onLineChapterList.add(chapter);
            }
        }
        if(onLineChapterList.size()>0 && onLineChapterList.size()>this.chapterList.size()){
            if(this.chapterList.size()==0){
                DataSupport.saveAll(onLineChapterList);
            }else{
                List<Chapter> newChapterList = new ArrayList<Chapter>();
                for (int i = this.chapterList.size(); i < onLineChapterList.size() ; i++) {
                    newChapterList.add(onLineChapterList.get(i));
                }
                DataSupport.saveAll(newChapterList);
            }
        }
        this.chapterList = onLineChapterList;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeProgressDialog();
                chapterAdapter.setChapterList(chapterList);
                chapterAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 生产path
     * 增加相对路径及绝对路径的判断
     * @param path
     * @return
     */
    public static String parsePath(final String basePath, String path){
        path = path.trim();
        if(path.startsWith("/")){
            StringBuilder sb=new StringBuilder();
            int count=0;
            for (int i = 0; i < basePath.length(); i++) {
                char c = basePath.charAt(i);
                if(c=='/'){
                    count++;
                    if(count==3){
                        break;
                    }
                }
                sb.append(c);
            }
            return sb.toString()+path;
        }
        if(!path.startsWith("http")){
            return basePath.endsWith("/")?basePath+path:basePath+"/"+path;
        }
        return path;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public static Intent starti(Context mContext, String bookTitle, String bookUrl){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("bookTitle", bookTitle);
        intent.putExtra("bookUrl", bookUrl);
        return intent;
    }
}
