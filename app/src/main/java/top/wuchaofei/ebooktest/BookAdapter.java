package top.wuchaofei.ebooktest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import top.wuchaofei.ebooktest.db.Book;

/**
 * 章节列表
 * recylerView适配器
 * Created by jspdba@163.com on 2018/3/27.
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder>{
    private Context mContext;
    private List<Book> bookList;
    // 点击事件处理器
    private OnItemClickListener mOnItemClickListener;

    public BookAdapter(List<Book> bookList, Context context) {
        this.bookList=bookList;
        this.mContext=context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Book book = bookList.get(position);
        Glide.with(mContext).load(book.getLogo()).into(holder.logoImageView);
        holder.titleTextView.setText(book.getTitle());
        holder.bookLastChapterView.setText(book.getLastChapterTitle());
        if(mOnItemClickListener!=null){
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mView;

        ImageView logoImageView;
        TextView titleTextView;
        TextView bookLastChapterView;
        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            logoImageView=(ImageView)itemView.findViewById(R.id.book_logo);
            titleTextView=(TextView)itemView.findViewById(R.id.book_title);
            bookLastChapterView = (TextView) itemView.findViewById(R.id.book_last_chapter);
        }
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }
}
