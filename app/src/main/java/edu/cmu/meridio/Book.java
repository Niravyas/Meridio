package edu.cmu.meridio;

/**
 * Created by yadav on 7/22/2017.
 */

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String isbn;

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
}
