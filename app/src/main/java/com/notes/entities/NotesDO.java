package com.notes.entities;

import org.chalup.microorm.annotations.Column;

public class NotesDO {

    @Column("title")
    private String title;
    @Column("text")
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
