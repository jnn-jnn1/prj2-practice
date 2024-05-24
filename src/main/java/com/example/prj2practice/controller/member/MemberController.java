package com.example.prj2practice.controller.member;

import com.example.prj2practice.domain.Member;
import com.example.prj2practice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    final MemberService service;

    @PostMapping("/add")
    public void add(@RequestBody Member member) {
        service.add(member);
    }

    @GetMapping("/list")
    public List<Member> list() {
        return service.getAll();
    }

    @GetMapping(value = "check", params = "email")
    public ResponseEntity checkEmail(String email) {
        Member member = service.getByEmail(email);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "check", params = "nickName")
    public ResponseEntity checkNickName(String nickName) {
        Member member = service.getByNickName(nickName);
        if (member == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Member member) {
        Map<String, Object> token = service.getToken(member);
        
        if (token != null) {
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
