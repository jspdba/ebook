package top.wuchaofei.ebooktest.db;


import org.litepal.crud.DataSupport;

/**
 * 章节
 * Created by jspdba@163.com on 2018/3/26.
 */

public class Chapter extends DataSupport {
    private int id;
    private int bookId;
    private int orderIndex;
    private String title;
    private String content;
    private String link;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", orderIndex=" + orderIndex +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
