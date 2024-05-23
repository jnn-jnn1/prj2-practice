package com.example.prj2practice.service;

import com.example.prj2practice.domain.Member;
import com.example.prj2practice.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper mapper;


    public void add(Member member) {
        mapper.insert(member);
    }

    public List<Member> getAll() {
        return mapper.selectAll();
    }

    public Member getByEmail(String email) {
        return mapper.selectByEmail(email);
    }

    public Member getByNickName(String nickName) {
        return mapper.selectByNickName(nickName);
    }
}
