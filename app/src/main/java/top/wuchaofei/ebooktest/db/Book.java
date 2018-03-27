package top.wuchaofei.ebooktest.db;

import org.litepal.crud.DataSupport;

/**
 * 书
 * Created by jspdba@163.com on 2018/3/26.
 */

public class Book extends DataSupport {
    private int id;
    private String name;
    private String author;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
