package com.example.prj2practice.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class Board {
    private Integer id;
    private String title;
    private String content;
    private String writer;
    private Integer memberId;
    private LocalDateTime inserted;
    private Integer numberOfImages;
    private List<BoardFile> files;

    public String getDateAndTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return inserted.format(formatter);
    }
}
