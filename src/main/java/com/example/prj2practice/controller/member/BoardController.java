package com.example.prj2practice.controller.member;

import com.example.prj2practice.domain.Board;
import com.example.prj2practice.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService service;

    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public void write(@RequestBody Board board, Authentication authentication) {
        service.write(board, authentication);
    }

    @GetMapping("list")
    public List<Board> list() {
        return service.getAll();
    }
}
