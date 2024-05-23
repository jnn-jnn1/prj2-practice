package com.example.prj2practice.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Member {
    private int id;
    private String email;
    private String password;
    private String nickName;
    private LocalDateTime inserted;
}
