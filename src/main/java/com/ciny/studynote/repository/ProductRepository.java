package com.ciny.studynote.repository;

import com.ciny.studynote.model.Product;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@NoArgsConstructor
public class ProductRepository {
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbId;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    // 즐겨찾기의 전체 상품 불러오기
    public List<Product> getProducts() throws SQLException {
        List<Product> products = new ArrayList<>();

        Connection connection = DriverManager.getConnection(dbUrl, dbId, dbPassword);

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from product");

        while (rs.next()) {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setImage(rs.getString("image"));
            product.setLink(rs.getString("link"));
            product.setLprice(rs.getInt("lprice"));
            product.setMyprice(rs.getInt("myprice"));
            product.setTitle(rs.getString("title"));
            products.add(product);
        }

        rs.close();
        connection.close();

        return products;
    }

    // 즐겨찾기의 특정 상품 불러오기
    public Product getProduct(Long id) throws SQLException {
        Product product = new Product();
        Connection connection = DriverManager.getConnection(dbUrl, dbId, dbPassword);

        PreparedStatement ps = connection.prepareStatement("select * from product where id = ?");
        ps.setLong(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            product.setId(rs.getLong("id"));
            product.setTitle(rs.getString("title"));
            product.setLink(rs.getString("link"));
            product.setImage(rs.getString("image"));
            product.setLprice(rs.getInt("lprice"));
            product.setMyprice(rs.getInt("myprice"));
        } else {
            product = null;
        }

        rs.close();
        ps.close();
        connection.close();

        return product;
    }

    // 즐겨찾기에 상품 추가
    public Product createProduct(Product product) throws SQLException {
        Connection connection = DriverManager.getConnection(dbUrl, dbId, dbPassword);

        PreparedStatement ps = connection.prepareStatement("select max(id) as id from product");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            product.setId(rs.getLong("id") + 1);
        } else {
            throw new SQLException("Product 테이블의 마지막 id값을 찾아오지 못했습니다.");
        }

        ps = connection.prepareStatement("insert into product(id, title, image, link, lprice, myprice) values(?, ?, ?, ?, ?, ?)");
        ps.setLong(1, product.getId());
        ps.setString(2, product.getTitle());
        ps.setString(3, product.getImage());
        ps.setString(4, product.getLink());
        ps.setInt(5, product.getLprice());
        ps.setInt(6, product.getMyprice());

        // 쿼리 실행
        ps.executeUpdate();

        // DB 연결 해제
        ps.close();
        connection.close();

        return product;
    }

    // 관심가격 설정하기
    public void updateMyprice(Long id, int myprice) throws SQLException {
        Connection connection = DriverManager.getConnection(dbUrl, dbId, dbPassword);

        PreparedStatement ps = connection.prepareStatement("update product set myprice = ? where id = ?");
        ps.setInt(1, myprice);
        ps.setLong(2, id);
        ps.executeUpdate();

        ps.close();
        connection.close();
    }
}
