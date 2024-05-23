package com.example.prj2practice.controller.member;

import com.example.prj2practice.domain.Member;
import com.example.prj2practice.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
