package com.example.prj2practice.service;

import com.example.prj2practice.domain.Board;
import com.example.prj2practice.domain.Member;
import com.example.prj2practice.mapper.BoardMapper;
import com.example.prj2practice.mapper.MemberMapper;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper mapper;

    @Value("${kakao.login.client_id}")
    String clientId;
    @Value("${kakao.login.redirect_uri}")
    String redirectUri;

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final BoardMapper boardMapper;
    private final BoardService boardService;

    public void add(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
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

    public Map<String, Object> getToken(Member member) {

        Map<String, Object> result = null;

        Member db = mapper.selectByEmail(member.getEmail());

        if (db != null) {
            List<String> authority = mapper.getAuthorityById(db.getId());

            String authorities = authority.stream().collect(Collectors.joining(" "));

            if (passwordEncoder.matches(member.getPassword(), db.getPassword())) {
                String token = "";
                result = new HashMap<>();
                Instant now = Instant.now();

                JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuer("self")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(60 * 60 * 24))
                        .subject(db.getId().toString())
                        .claim("nickName", db.getNickName())
                        .claim("scope", authorities)
                        .build();

                token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

                result.put("token", token);
            }
        }

        return result;
    }

    public boolean validate(Member member) {
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            return false;
        }
        if (member.getPassword() == null || member.getNickName().isBlank()) {
            return false;
        }
        if (member.getNickName() == null || member.getPassword().trim().isBlank()) {
            return false;
        }

        String emailPattern = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*";

        if (!member.getEmail().matches(emailPattern)) {
            return false;
        }

        return true;
    }

    public Member getById(Integer id) {
        return mapper.selectById(id);
    }

    public void delete(Integer id) {

        // 회원이 쓴 게시물 조회
        List<Board> boardList = boardMapper.selectByMemberId(id);

        boardList.forEach((board -> {
            boardService.deleteById(board.getId());
        }));

        boardMapper.deleteByMemberId(id);
        mapper.deleteById(id);
    }

    public boolean hasAccess(Member member, Authentication authentication) {
        if (!authentication.getName().equals(member.getId().toString())) {
            return false;
        }

        Member dbMember = mapper.selectById(member.getId());

        if (dbMember == null) {
            return false;
        }

        return passwordEncoder.matches(member.getPassword(), dbMember.getPassword());
    }

    public boolean hasAccess(Integer id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SCOPE_admin"));

        boolean self = authentication.getName().equals(id.toString());

        return self || isAdmin;
    }

    public boolean hasAccessModify(Member member, Authentication authentication) {

        if (!member.getId().toString().equals(authentication.getName())) {
            return false;
        }

        Member db = mapper.selectById(member.getId());

        if (db == null) {
            return false;
        }

        return passwordEncoder.matches(member.getOldPassword(), db.getPassword());
    }

    public Map<String, Object> modify(Member member, Authentication authentication) {

        if (member.getPassword() != null && member.getPassword().length() > 0) {
            member.setPassword(passwordEncoder.encode(member.getPassword()));
        } else {
            Member db = mapper.selectById(member.getId());
            member.setPassword(db.getPassword());
        }
        mapper.update(member);

        String token = "";

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Map<String, Object> claims = jwt.getClaims();
        JwtClaimsSet.Builder jwtClaimsSetBuilder = JwtClaimsSet.builder();
        claims.forEach(jwtClaimsSetBuilder::claim);
        jwtClaimsSetBuilder.claim("nickName", member.getNickName());

        JwtClaimsSet jwtClaimsSet = jwtClaimsSetBuilder.build();
        token = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();

        return Map.of("token", token);
    }

    public String getKaKaoAcessToken(String code) throws IOException {
        String accessToken = "";
        String refreshToken = "";
        String requestURL = "https://kauth.kakao.com/oauth/token";

        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        String sb = STR."grant_type=authorization_code&client_id=\{clientId}&redirect_uri=\{redirectUri}&code=\{code}";
        bufferedWriter.write(sb);
        bufferedWriter.flush();

        int responseCode = connection.getResponseCode();

        // 요청을 통해 얻은 데이터를 InputStreamReader을 통해 읽어 오기
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        StringBuilder result = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        JsonElement element = JsonParser.parseString(result.toString());

        accessToken = element.getAsJsonObject().get("access_token").getAsString();
        refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

        bufferedReader.close();
        bufferedWriter.close();

        return accessToken;
    }

    public Map<String, String> getUserInfo(String accessToken) throws IOException {
        HashMap<String, String> userInfo = new HashMap<>();
        String reqUrl = "https://kapi.kakao.com/v2/user/me";
        URL url = new URL(reqUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        int responseCode = conn.getResponseCode();

        BufferedReader br;
        if (responseCode >= 200 && responseCode <= 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        String line = "";
        StringBuilder responseSb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            responseSb.append(line);
        }
        String result = responseSb.toString();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(result);

        JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
        JsonObject kakaoAccount = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

        String nickname = properties.getAsJsonObject().get("nickname").getAsString();
        String email = kakaoAccount.getAsJsonObject().get("email").getAsString();

        userInfo.put("nickname", nickname);
        userInfo.put("email", email);

        br.close();

        return userInfo;
    }
}
