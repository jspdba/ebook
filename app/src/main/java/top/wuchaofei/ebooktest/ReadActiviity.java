package top.wuchaofei.ebooktest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ReadActiviity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_activiity);

        Intent intent=getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        setTitle(title);
        TextView contentView = (TextView)findViewById(R.id.chapter_content);
        contentView.setText(content);
    }

    public static Intent starti(Context mContext, String title, String content){
        Intent intent = new Intent(mContext, ReadActiviity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        return intent;
    }
}
