package com.example.prj2practice.mapper;

import com.example.prj2practice.domain.Board;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BoardMapper {

    @Insert("""
                INSERT INTO board (title, content, member_id)
                VALUES (#{title}, #{content}, #{memberId})
            """)
    int insert(Board board);

    @Select("""
                SELECT b.id, b.title, b.content, m.nick_name writer, b.inserted
                FROM board b JOIN member m ON b.member_id = m.id
                ORDER BY id DESC 
            """)
    List<Board> selectAll();

    @Select("""
                SELECT b.id, b.title, b.content, m.nick_name writer, b.inserted
                FROM board b JOIN member m ON b.member_id = m.id
                WHERE b.id = #{id}
            """)
    Board selectById(Integer id);

    @Delete("""
                DELETE FROM board
                WHERE id = #{id}
            """)
    int deleteById(Integer id);
}
