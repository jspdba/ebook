package top.wuchaofei.ebooktest;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import top.wuchaofei.ebooktest.util.Constant;
import top.wuchaofei.ebooktest.util.HttpUtil;

public class MainActivity extends AppCompatActivity{
    public static final String bookAddress = "http://www.biquge.tw/86_86745/";
    public static final int bookId = 1;
    private Context mContext;
    private ProgressDialog progressDialog;
    private List<Chapter> chapterList =  new ArrayList<Chapter>();
    private ChapterAdapter chapterAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
//        DataSupport.deleteAll(Chapter.class);
        getBookChapter();
        recyclerView = (RecyclerView) findViewById(R.id.chapter_list);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        chapterAdapter = new ChapterAdapter(chapterList);
        chapterAdapter.setOnItemClickListener(new ChapterAdapter.OnItemClickListener() {
            @Override
            public void onClick(final int position) {
                final Chapter chapter = chapterList.get(position);
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
                                            startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent()));
                                        }
                                    });
                                }
                            });
                        }else{
                            startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent()));
                        }
                    }
                }else{
                    startActivity(ReadActiviity.starti(mContext,chapter.getTitle(),chapter.getContent()));
                }
            }
        });
        recyclerView.setAdapter(chapterAdapter);
    }

    private String parseContent(String resp) {
        Document document = Jsoup.parse(resp);
        Elements elements = document.select(Constant.BOOK_CONTENT_SELECTOR);
        String str = elements.html();
        str = str.replaceAll("&nbsp;", " ").replaceAll("<br>","");
        Log.i("content===", str);
        return str==null?str:str.trim();
    }

    /**
     * 远程获取章节列表
     */
    private void getBookChapter() {
        chapterList = DataSupport.findAll(Chapter.class);
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
                    parse2ChapterList(resp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            chapterAdapter.setChapterList(chapterList);
                            chapterAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    private void parse2ChapterList(String resp) {
        Document document = Jsoup.parse(resp);
        Elements elements = document.select(Constant.BOOK_CHAPTER_SELECTOR);
        List<Chapter>  chapterList=new ArrayList<Chapter>();
        for (Element dd : elements) {
            if(dd.childNodeSize()>0){
                Element link = dd.child(0);
                Chapter chapter=new Chapter();
                chapter.setBookId(bookId);
                chapter.setTitle(link.text().trim());
                chapter.setLink(parsePath(bookAddress, link.attr("href")));
                chapter.save();
                chapterList.add(chapter);
            }
        }
        this.chapterList = chapterList;
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
                sb.append(c);
                if(c=='/'){
                    count++;
                    if(count==3){
                        break;
                    }
                }
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
}
