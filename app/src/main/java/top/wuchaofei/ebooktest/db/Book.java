package top.wuchaofei.ebooktest.db;

import org.litepal.crud.DataSupport;

/**
 * 书
 * Created by jspdba@163.com on 2018/3/26.
 */

public class Book extends DataSupport {
    private int id;
    private String title;
    private String author;
    private String category;
    private String description;
    private String url;
    private String logo;
    private String lastUpdateTime;
    private String lastChapterTitle;
    private String lastChapterUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastChapterTitle() {
        return lastChapterTitle;
    }

    public void setLastChapterTitle(String lastChapterTitle) {
        this.lastChapterTitle = lastChapterTitle;
    }

    public String getLastChapterUrl() {
        return lastChapterUrl;
    }

    public void setLastChapterUrl(String lastChapterUrl) {
        this.lastChapterUrl = lastChapterUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", logo='" + logo + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", lastChapterTitle='" + lastChapterTitle + '\'' +
                ", lastChapterUrl='" + lastChapterUrl + '\'' +
                '}';
    }
}
