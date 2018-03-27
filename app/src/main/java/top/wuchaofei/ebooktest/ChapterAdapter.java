package top.wuchaofei.ebooktest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import top.wuchaofei.ebooktest.db.Chapter;

/**
 * 章节列表
 * recylerView适配器
 * Created by jspdba@163.com on 2018/3/27.
 */

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder>{
    private List<Chapter> chapterList;
    public ChapterAdapter(List<Chapter> chapterList) {
        this.chapterList=chapterList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false);
        final ViewHolder viewHolder=new ViewHolder(view);
        viewHolder.titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=viewHolder.getAdapterPosition();
                Chapter chapter=chapterList.get(position);
                Toast.makeText(view.getContext(),"文章标题是 "+chapter.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.titleTextView.setText(chapter.getTitle());
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
}
