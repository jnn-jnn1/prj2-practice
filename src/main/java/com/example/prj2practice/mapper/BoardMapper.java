package com.example.prj2practice.mapper;

import com.example.prj2practice.domain.Board;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BoardMapper {

    @Insert("""
                INSERT INTO board (title, content, member_id)
                VALUES (#{title}, #{content}, #{memberId})
            """)
    int insert(Board board);

    @Select("""
                <script>
                SELECT b.id, b.title, b.content, m.nick_name writer, b.inserted
                FROM board b JOIN member m ON b.member_id = m.id
                <trim prefix="WHERE" prefixOverrides="OR">
                    <if test="type != null">
                        <bind name="pattern" value="'%' + keyword + '%'"/>
                        <if test="type == 'all' || type == 'text'">
                            OR b.title LIKE #{pattern}
                            OR b.content LIKE #{pattern}
                        </if>
                        <if test="type == 'all' || type == 'nickName'">
                            OR m.nick_name LIKE #{pattern}
                        </if>
                    </if>
                </trim>
                ORDER BY id DESC
                LIMIT #{offset} , 10
                </script>
            """)
    List<Board> selectAll(Integer offset, String type, String keyword);

    @Select("""
                SELECT b.id, b.title, b.content, m.nick_name writer, b.inserted, b.member_id
                FROM board b JOIN member m ON b.member_id = m.id
                WHERE b.id = #{id}
            """)
    Board selectById(Integer id);

    @Delete("""
                DELETE FROM board
                WHERE id = #{id}
            """)
    int deleteById(Integer id);

    @Update("""
                UPDATE board
                SET title = #{title},
                    content = #{content}
                WHERE id = #{id}
            """)
    int update(Board board);

    @Delete("""
                DELETE FROM board
                WHERE member_id = #{memberId}
            """)
    int deleteByMemberId(Integer memberId);

    @Select("""
                SELECT COUNT(*) FROM board
            """)
    Integer countAll();

    @Select("""
                       <script>
                            SELECT COUNT(*)
                            FROM board b JOIN member m ON b.member_id = m.id
                            <trim prefix="WHERE" prefixOverrides="OR">
                                <if test="type != null">
                                    <bind name="pattern" value="'%' + keyword + '%'"/>
                                    <if test="type == 'all' || type == 'text'">
                                        OR b.title LIKE #{pattern}
                                        OR b.content LIKE #{pattern}
                                    </if>
                                    <if test="type == 'all' || type == 'nickName'">
                                        OR m.nick_name LIKE #{pattern}
                                    </if>
                                </if>
                            </trim>
                            </script>
            """)
    Integer countAllWithSearch(Integer page, String type, String keyword);
}
