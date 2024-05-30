package com.example.prj2practice.service;

import com.example.prj2practice.domain.Board;
import com.example.prj2practice.domain.BoardFile;
import com.example.prj2practice.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper mapper;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    String bucketName;

    @Value("${image.src.prefix}")
    String srcPrefix;

    public void write(Board board, Authentication authentication, MultipartFile[] fileList) throws IOException {

        board.setMemberId(Integer.valueOf(authentication.getName()));
        mapper.insert(board);

        // db 저장
        for (MultipartFile file : fileList) {
            String fileName = file.getOriginalFilename();
            mapper.insertFileName(board.getId(), fileName);

            String key = STR."prj2/\{board.getId()}/\{fileName}";
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        }


    }

    public Map<String, Object> getAll(Integer page, String type, String keyword) {
        Map pageInfo = new HashMap();
        Integer offset = (page - 1) * 10;
        Integer countAll = mapper.countAllWithSearch(page, type, keyword);

        Integer lastPageNumber = (countAll - 1) / 10 + 1;
        Integer leftPageNumber = (page - 1) / 10 * 10 + 1;
        Integer rightPageNumber = leftPageNumber + 9;

        rightPageNumber = Math.min(rightPageNumber, lastPageNumber);

        leftPageNumber = rightPageNumber - 9;
        leftPageNumber = Math.max(leftPageNumber, 1);

        Integer prevPageNumber = leftPageNumber - 1;
        Integer nextPageNumber = rightPageNumber + 1;

        if (prevPageNumber > 0) {
            pageInfo.put("prevPageNumber", prevPageNumber);
        }
        if (nextPageNumber <= lastPageNumber) {
            pageInfo.put("nextPageNumber", nextPageNumber);
        }

        pageInfo.put("lastPageNumber", lastPageNumber);
        pageInfo.put("currentPageNumber", page);
        pageInfo.put("leftPageNumber", leftPageNumber);
        pageInfo.put("rightPageNumber", rightPageNumber);

        List<Board> boardList = mapper.selectAll(offset, type, keyword);

        return Map.of("boardList", boardList, "pageInfo", pageInfo);
    }


    public Board getById(Integer id) {

        Board board = mapper.selectById(id);


        List<String> fileNames = mapper.selectFileNameByBoardId(id);

        List<String> srcList = fileNames.stream().map(name -> STR."\{srcPrefix}\{id}/\{name}").toList();

        BoardFile boardFile = new BoardFile(fileNames, srcList);
        board.setFiles(boardFile);

        return board;
    }

    public void deleteById(Integer id) {
        List<String> fileNames = mapper.selectFileNameByBoardId(id);

        for (String fileName : fileNames) {
            String key = STR."prj2/\{id}/\{fileName}";
            DeleteObjectRequest objectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key).build();

            s3Client.deleteObject(objectRequest);
        }

        //board_file
        mapper.deleteFileById(id);
        //board
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
