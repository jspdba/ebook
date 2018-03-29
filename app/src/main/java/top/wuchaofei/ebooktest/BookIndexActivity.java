package top.wuchaofei.ebooktest;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.wuchaofei.ebooktest.db.Book;
import top.wuchaofei.ebooktest.db.Chapter;

public class BookIndexActivity extends AppCompatActivity {
    private List<Book> bookList=new ArrayList<Book>();
    private BookAdapter bookAdapter;
    private SwipeRefreshLayout swipeRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_index);
        initBook();
//        updateBook();
        RecyclerView bookRecyclerView = (RecyclerView) findViewById(R.id.book_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        StaggeredGridLayoutManager layoutManager =new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        bookRecyclerView.setLayoutManager(layoutManager);
        //添加Android自带的分割线
//        bookRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        bookAdapter = new BookAdapter(bookList, BookIndexActivity.this);
        bookRecyclerView.setAdapter(bookAdapter);

        bookAdapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Book book = bookList.get(position);
                startActivity(MainActivity.starti(BookIndexActivity.this, book.getTitle(), book.getUrl()));
            }
        });
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.book_swipeLayout);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateBook();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void initBook() {
        bookList = DataSupport.findAll(Book.class);
        // 初始化一本书
        if(bookList==null || bookList.size()==0){
            Book book=new Book();
            book.setUrl("https://www.xs.la/86_86745/");
            bookList.add(book);
            updateBook();
        }
    }

    private void updateBook() {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();
        Request.Builder builder=new Request.Builder();
        for (final Book book : bookList) {
            Request request = builder.url(book.getUrl()).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BookIndexActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Document doc = Jsoup.parse(response.body().string());
                    Elements metaElements = doc.getElementsByTag("meta");
                    for (Element metaElement : metaElements) {
                        if(metaElement.hasAttr("property")){
                            String property = metaElement.attr("property");
                            String content = metaElement.attr("content");
                            if("og:title".equalsIgnoreCase(property)){
                                book.setTitle(content);
                            }
                            if("og:description".equalsIgnoreCase(property)){
                                book.setDescription(content);
                            }
                            if("og:image".equalsIgnoreCase(property)){
                                book.setLogo(content);
                            }
                            if("og:novel:category".equalsIgnoreCase(property)){
                                book.setCategory(content);
                            }
                            if("og:novel:author".equalsIgnoreCase(property)){
                                book.setAuthor(content);
                            }
                            if("og:novel:update_time".equalsIgnoreCase(property)){
                                book.setLastUpdateTime(content);
                            }
                            if("og:novel:latest_chapter_name".equalsIgnoreCase(property)){
                                book.setLastChapterTitle(content);
                            }
                            if("og:novel:latest_chapter_url".equalsIgnoreCase(property)){
                                book.setLastChapterUrl(content);
                            }

                        }
                    }

                    book.save();
                    Log.d("book===", book.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bookAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }
}
