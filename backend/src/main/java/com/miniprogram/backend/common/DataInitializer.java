package com.miniprogram.backend.common;

import com.miniprogram.backend.domain.Entity.Product;
import com.miniprogram.backend.domain.DAO.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查数据库是否为空
        if (productRepository.count() == 0) {
            log.info("Initializing sample product data...");
            
            // 创建示例产品数据（更丰富的商品信息）
            Product product1 = new Product();
            product1.setProductNo("P001");
            product1.setTitle("iPhone 15 Pro Max");
            product1.setPrice(new BigDecimal("9999.00"));
            product1.setStock(50);
            product1.setImageUrl("/pics/iphone15promax.jpg");
            product1.setDescription("Apple iPhone 15 Pro Max，A17 Pro芯片，钛金属设计，4800万像素主摄");
            product1.setSales(1200);
            product1.setStatus(1);
            
            Product product2 = new Product();
            product2.setProductNo("P002");
            product2.setTitle("MacBook Pro 14英寸");
            product2.setPrice(new BigDecimal("14999.00"));
            product2.setStock(30);
            product2.setImageUrl("/pics/macbookpro14.jpg");
            product2.setDescription("MacBook Pro 14英寸，M3 Pro芯片，18GB内存，512GB SSD");
            product2.setSales(800);
            product2.setStatus(1);
            
            Product product3 = new Product();
            product3.setProductNo("P003");
            product3.setTitle("AirPods Pro 第二代");
            product3.setPrice(new BigDecimal("1899.00"));
            product3.setStock(100);
            product3.setImageUrl("/pics/airpodspro2.jpg");
            product3.setDescription("AirPods Pro 第二代，主动降噪，自适应通透模式，USB-C充电");
            product3.setSales(2500);
            product3.setStatus(1);
            
            // 使用批量保存提高效率
            List<Product> products = Arrays.asList(product1, product2, product3);
            productRepository.saveAll(products);
            
            log.info("Sample data initialized successfully! Total {} products created.", products.size());
        } else {
            log.debug("Database already contains data, skipping initialization.");
        }
    }
}