package com.ciny.studynote.servlet;

import com.ciny.studynote.dto.ItemDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = "/api/search")
public class ItemSearchServlet extends HttpServlet {
    @Value("${spring.search.naver.client_id}")
    String client_id;
    @Value("${spring.search.naver.client_secret}")
    String client_secret;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // req 파라미터에서 검색어 추출
        String query = request.getParameter("query");

        // 네이버 API 호출시 요구되는 header와 body
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", client_id);
        headers.add("X-Naver-Client-Secret", client_secret);
        String body = "";
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        // 호출 결과값 json으로 받아오기
        ResponseEntity<String> responseEntity = rest.exchange("https://openapi.naver.com/v1/search/shop.json?query=" + query,
                HttpMethod.GET, requestEntity, String.class);
        String naverApiResponseJson = responseEntity.getBody();

        // json 결과값을 ItemDto에 넣기
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode itemsNode = objectMapper.readTree(naverApiResponseJson).get("items");
        List<ItemDto> itemDtoList = objectMapper
                .readerFor(new TypeReference<List<ItemDto>>() {
                })
                .readValue(itemsNode);

        // Response 보내기
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        String itemDtoListJson = objectMapper.writeValueAsString(itemDtoList);
        out.print(itemDtoListJson);
        out.flush();
    }
}
