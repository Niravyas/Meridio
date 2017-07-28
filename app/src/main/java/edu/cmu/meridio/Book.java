package edu.cmu.meridio;

import android.support.annotation.NonNull;

/**
 * Created by yadav on 7/22/2017.
 */

public class Book implements Comparable<Book>{
    private int id;
    private String userId;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String isbn;
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Book(int id, String title){
        this.id = id;
        this.title = title;
    }

    public Book (int id, String isbn, String title){
        this.id = id;
        this.isbn = isbn;
        this.title = title;
    }

    public Book (int id, String userId, String isbn, String title, String genre, String description, String imageUrl){
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.isbn = isbn;
        this.genre = genre;
        this.description= description;
        this.imageUrl = imageUrl;
    }

    @Override
    public int compareTo(@NonNull Book b) {
        if (this.title.equals("") || b.title.equals(""))
                return 0;
        return this.title.compareToIgnoreCase(b.title);
    }
}
