package com.example.prj2practice.service;

import com.example.prj2practice.domain.Board;
import com.example.prj2practice.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper mapper;

    public void write(Board board, Authentication authentication) {
        board.setMemberId(Integer.valueOf(authentication.getName()));
        mapper.insert(board);
    }

    public List<Board> getAll() {
        return mapper.selectAll();
    }


    public Board getById(Integer id) {
        return mapper.selectById(id);
    }

    public void deleteById(Integer id) {
        mapper.deleteById(id);
    }

    public void edit(Board board) {
        mapper.update(board);
    }

    public boolean hasAccess(Integer id, Authentication authentication) {
        Board board = mapper.selectById(id);

        if (board.getMemberId().equals(Integer.valueOf(authentication.getName()))) {
            return true;
        }
        return false;
    }
}
