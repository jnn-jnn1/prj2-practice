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
FROM member;