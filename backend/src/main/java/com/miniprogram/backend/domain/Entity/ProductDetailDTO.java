package com.miniprogram.backend.domain.Entity;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetailDTO {
    private Long id;
    private String no;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
    private Integer stock;
    private List<String> banners;
    private List<String> detailImages;
    private List<ProductSku> skus;
    private List<SpecGroupDTO> specs;

    public static class SpecGroupDTO {
        private Long id;
        private String name;
        private List<SpecValueDTO> values;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<SpecValueDTO> getValues() {
            return values;
        }

        public void setValues(List<SpecValueDTO> values) {
            this.values = values;
        }
    }

    public static class SpecValueDTO {
        private Long id;
        private String value;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public List<String> getBanners() {
        return banners;
    }

    public void setBanners(List<String> banners) {
        this.banners = banners;
    }

    public List<String> getDetailImages() {
        return detailImages;
    }

    public void setDetailImages(List<String> detailImages) {
        this.detailImages = detailImages;
    }

    public List<ProductSku> getSkus() {
        return skus;
    }

    public void setSkus(List<ProductSku> skus) {
        this.skus = skus;
    }

    public List<SpecGroupDTO> getSpecs() {
        return specs;
    }

    public void setSpecs(List<SpecGroupDTO> specs) {
        this.specs = specs;
    }
}
