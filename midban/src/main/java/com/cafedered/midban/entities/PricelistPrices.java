package com.cafedered.midban.entities;

import com.cafedered.cafedroidlitedao.annotations.Entity;
import com.cafedered.cafedroidlitedao.annotations.Id;
import com.cafedered.cafedroidlitedao.annotations.Property;
import com.cafedered.midban.annotations.Remote;
import com.cafedered.midban.annotations.RemoteProperty;
import com.debortoliwines.openerp.api.FilterCollection;

@Entity(tableName = "table_pricelist_prices")
@Remote(object = "table.pricelist.prices")

public class PricelistPrices extends BaseRemoteEntity {
    private static final long serialVersionUID = 2325116999511804028L;

    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "product_id")
    @RemoteProperty(name = "product_id")
    private Number productId;

    @Property(columnName = "price")
    @RemoteProperty(name = "price")
    private Number price;

    @Property(columnName = "discount1")
    @RemoteProperty(name = "discount1")
    private Number discount1;

    @Property(columnName = "discount2")
    @RemoteProperty(name = "discount2")
    private Number discount2;

    @Property(columnName = "discount3")
    @RemoteProperty(name = "discount3")
    private Number discount3;

    @Property(columnName = "company_id")
    @RemoteProperty(name = "company_id")
    private Number companyId;

    @Property(columnName = "pricelist_id")
    @RemoteProperty(name = "pricelist_id")
    private Number pricelistId;


    @Override
    public Integer getPendingSynchronization() {
        return null;
    }

    @Override
    public void setPendingSynchronization(Integer pendingSynchronization) {

    }

    @Override
    public FilterCollection getRemoteFilters() {
        return null;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Number getProductId() {
        return productId;
    }

    public void setProductId(Number productId) {
        this.productId = productId;
    }

    public Number getPrice() {
        return price;
    }

    public void setPrice(Number price) {
        this.price = price;
    }

    public Number getDiscount1() {
        return discount1;
    }

    public void setDiscount1(Number discount1) {
        this.discount1 = discount1;
    }

    public Number getDiscount2() {
        return discount2;
    }

    public void setDiscount2(Number discount2) {
        this.discount2 = discount2;
    }

    public Number getDiscount3() {
        return discount3;
    }

    public void setDiscount3(Number discount3) {
        this.discount3 = discount3;
    }

    public Number getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Number companyId) {
        this.companyId = companyId;
    }

    public Number getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Number pricelistId) {
        this.pricelistId = pricelistId;
    }
}
