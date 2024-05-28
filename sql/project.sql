use prj2;

CREATE TABLE member
(
    id        INT PRIMARY KEY AUTO_INCREMENT,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    nick_name VARCHAR(100) NOT NULL UNIQUE,
    inserted  DATETIME     NOT NULL DEFAULT NOW()
);

CREATE TABLE board
(
    id        INT PRIMARY KEY AUTO_INCREMENT,
    title     VARCHAR(100)  NOT NULL,
    content   VARCHAR(1000) NOT NULL,
    writer    VARCHAR(100),
    member_id INT           NOT NULL,
    inserted  DATETIME      NOT NULL DEFAULT NOW(),
    FOREIGN KEY (member_id) REFERENCES member (id)
);

SELECT *
FROM board;

CREATE TABLE authority
(
    member_id INT         NOT NULL REFERENCES member (id),
    name      VARCHAR(20) NOT NULL,
    PRIMARY KEY (member_id, name)
);

INSERT INTO authority (member_id, name) VALUE (5, 'admin');

SELECT *
FROM authority;

INSERT INTO board (title, content, member_id)
SELECT title, content, member_id
FROM board;

CREATE TABLE board_file
(
    board_id INT          NOT NULL REFERENCES board (id),
    name     VARCHAR(500) NOT NULL,
    PRIMARY KEY (board_id, name)
);

SELECT *
FROM board_file;

SELECT b.id, b.title, b.content, m.nick_name writer, b.inserted, COUNT(f.name) as numberOfImages
FROM board b
         JOIN member m ON b.member_id = m.id
         LEFT JOIN board_file f ON b.id = f.board_id
WHERE b.id = 901
GROUP BY b.id;
