package top.wuchaofei.ebooktest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import top.wuchaofei.ebooktest.db.Chapter;

/**
 * 章节列表
 * recylerView适配器
 * Created by jspdba@163.com on 2018/3/27.
 */

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder>{
    private List<Chapter> chapterList;
    // 点击事件处理器
    private OnItemClickListener mOnItemClickListener;

    public ChapterAdapter(List<Chapter> chapterList) {
        this.chapterList=chapterList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Chapter chapter = chapterList.get(position);
        holder.titleTextView.setText(chapter.getTitle());
        if(mOnItemClickListener!=null){
            holder.titleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return chapterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView titleTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            titleTextView=(TextView)itemView.findViewById(R.id.chapter_title);
        }
    }

    public List<Chapter> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<Chapter> chapterList) {
        this.chapterList = chapterList;
    }

    public interface OnItemClickListener{
        void onClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this.mOnItemClickListener = onItemClickListener;
    }
}
