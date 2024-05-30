package com.example.prj2practice.controller.member;

import com.example.prj2practice.domain.Board;
import com.example.prj2practice.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService service;

    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity write(Board board, Authentication authentication,
                                @RequestParam(required = false, value = "fileList[]") MultipartFile[] fileList) throws IOException {

        if (!board.getTitle().trim().isBlank() && !board.getContent().trim().isBlank()) {
            service.write(board, authentication, fileList);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("list")
    public Map<String, Object> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(defaultValue = "") String keyword) {
        return service.getAll(page, type, keyword);
    }

    @GetMapping("{id}")
    public ResponseEntity get(@PathVariable Integer id) {
        Board board = service.getById(id);

        if (board == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(board);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity delete(@PathVariable Integer id, Authentication authentication) {
        if (service.hasAccess(id, authentication)) {
            service.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("edit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity edit(Board board, @RequestParam(value = "removeFileList[]", required = false) List<String> removeFileList, Authentication authentication) {
        if (service.hasAccess(board.getId(), authentication)) {
            service.edit(board, removeFileList);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
