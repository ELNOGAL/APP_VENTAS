/*******************************************************************************
 * MidBan is an Android App which allows to interact with OpenERP
 *     Copyright (C) 2014  CafedeRed
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.cafedered.midban.entities;

import com.cafedered.cafedroidlitedao.annotations.Entity;
import com.cafedered.cafedroidlitedao.annotations.Id;
import com.cafedered.cafedroidlitedao.annotations.Property;
import com.cafedered.midban.annotations.Remote;
import com.cafedered.midban.annotations.RemoteProperty;
import com.cafedered.midban.service.repositories.OrderLineRepository;
import com.cafedered.midban.service.repositories.PricelistPricesRepository;
import com.cafedered.midban.service.repositories.ProductTemplateRepository;
import com.cafedered.midban.service.repositories.ProductUlRepository;
import com.cafedered.midban.service.repositories.ProductUomRepository;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.OpeneERPApiException;

import java.math.BigDecimal;
import java.util.List;

@Entity(tableName = "product_product")
@Remote(object = "product.product")
public class Product extends BaseRemoteEntity {

    private static final long serialVersionUID = 2325116999521804028L;

    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "image") // Usamos este campo en lugar de image_medium porque tiene mayor calidad
    @RemoteProperty(name = "image")
    private byte[] imageMedium;

    @Property(columnName = "name_template")
    @RemoteProperty(name = "name")  // Usamos este campo en lugar de name_template porque corresponde con el nombre en el idioma del usuario
    private String nameTemplate;

    @Property(columnName = "product_tmpl_id")
    @RemoteProperty(name = "product_tmpl_id")
    private Number productTmplId;

    @Property(columnName = "ul")
    // @RemoteProperty(name = "ul",
    //         entityRef = ProductUl.class,
    //         repositoryRef = ProductUlRepository.class,
    //         orderedProperties = { "id", "name" })
    private Number ul;

    @Property(columnName = "lst_price")
    @RemoteProperty(name = "lst_price")
    private Number lstPrice;

    @Property(columnName = "sale_ok")
    @RemoteProperty(name = "sale_ok")
    private Boolean saleOk;

    @Property(columnName = "purchase_ok")
    @RemoteProperty(name = "purchase_ok")
    private Boolean purchaseOk;

    @Property(columnName = "type")
    @RemoteProperty(name = "type")
    private String type;

    @Property(columnName = "uom_id")
    @RemoteProperty(name = "uom_id")
    private Number uomId;

    @Property(columnName = "uos_id")
    @RemoteProperty(name = "uos_id")
    private Number uosId;

    @Property(columnName = "default_code")
    @RemoteProperty(name = "default_code")
    private String defaultCode;

    @Property(columnName = "ean13")
    @RemoteProperty(name = "ean13")
    private String ean13;

    @Property(columnName = "dun14")
    @RemoteProperty(name = "dun14")
    private String dun14;

    @Property(columnName = "temperature", type = Property.SQLType.REAL)
    // @RemoteProperty(name = "temperature")
    private Number temperature;

    @Property(columnName = "standard_price")
    @RemoteProperty(name = "standard_price")
    private Number standardPrice;

    @Property(columnName = "active")
    @RemoteProperty(name = "active")
    private Boolean active;

    @Property(columnName = "qty_available", type = Property.SQLType.REAL)
    @RemoteProperty(name = "qty_available")
    private Number qtyAvailable;

    @Property(columnName = "virtual_available")
    @RemoteProperty(name = "virtual_available")
    private Number virtualAvailable;

    @Property(columnName = "volume", type = Property.SQLType.REAL)
    @RemoteProperty(name = "volume")
    private Number volume;

    @Property(columnName = "weight", type = Property.SQLType.REAL)
    @RemoteProperty(name = "weight")
    private Number weight;

    @Property(columnName = "weight_net", type = Property.SQLType.REAL)
    @RemoteProperty(name = "weight_net")
    private Number weightNet;

    @Property(columnName = "box_units")
    // @RemoteProperty(name = "box_units")
    private Number boxUnits;

    @Property(columnName = "pallet_boxes_pallet")
    // @RemoteProperty(name = "pallet_boxes_pallet")
    private Number boxesPerPallet;

    @Property(columnName = "pallet_gross_weight")
    // @RemoteProperty(name = "pallet_gross_weight")
    private Number palletGrossWeight;

    @Property(columnName = "pallet_total_height")
    // @RemoteProperty(name = "pallet_total_height")
    private Number palletTotalHeight;

    @Property(columnName = "pallet_ul")
    // @RemoteProperty(name = "pallet_ul")
    private Number typeOfPallet;

    @Property(columnName = "substitute_products")
    private String substituteProducts;

    @Property(columnName = "last_price")
    private Number lastPrice;

    @Property(columnName = "last_discount")
    private Number lastDiscount;

    @Property(columnName = "last_discount_type")
    private String lastDiscountType;

    @Property(columnName = "discount1")
    private Number discount1;

    @Property(columnName = "discount2")
    private Number discount2;

    @Property(columnName = "discount3")
    private Number discount3;

    @Property(columnName = "is_favourite")
    private Boolean isFavourite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(byte[] imageMedium) {
        this.imageMedium = imageMedium;
    }

    public String getNameTemplate() {
        return nameTemplate;
    }

    public void setNameTemplate(String nameTemplate) {
        this.nameTemplate = nameTemplate;
    }

    public Number getProductTmplId() {
        return productTmplId;
    }

    public void setProductTmplId(Number productTmplId) {
        this.productTmplId = productTmplId;
    }

    public String getCode() {
        return defaultCode;
    }

    public void setCode(String code) {
        this.defaultCode = code;
    }

    public Number getLstPrice() {
        return lstPrice;
    }

    public void setLstPrice(Number lstPrice) {
        this.lstPrice = lstPrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Number getUomId() {
        return uomId;
    }

    public void setUomId(Number uomId) {
        this.uomId = uomId;
    }

    public String getDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(String defaultCode) {
        this.defaultCode = defaultCode;
    }

    public String getEan13() {
        return ean13;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    public String getDun14() {
        return dun14;
    }

    public void setDun14(String dun14) {
        this.dun14 = dun14;
    }

    public Number getTemperature() {
        return temperature;
    }

    public void setTemperature(Number temperature) {
        this.temperature = temperature;
    }

    public Number getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(Number standardPrice) {
        this.standardPrice = standardPrice;
    }

    public Number getQtyAvailable() {
        return qtyAvailable;
    }

    public void setQtyAvailable(Number qtyAvailable) {
        this.qtyAvailable = qtyAvailable;
    }

    public Number getVirtualAvailable() {
        if (virtualAvailable != null)
            return (Number) new BigDecimal(virtualAvailable.floatValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
        return virtualAvailable;
    }

    public void setVirtualAvailable(Number virtualAvailable) {
        this.virtualAvailable = virtualAvailable;
    }

    public Number getVolume() {
        return volume;
    }

    public void setVolume(Number volume) {
        this.volume = volume;
    }

    public Number getWeight() {
        return weight;
    }

    public void setWeight(Number weight) {
        this.weight = weight;
    }

    public Number getWeightNet() {
        return weightNet;
    }

    public void setWeightNet(Number weightNet) {
        this.weightNet = weightNet;
    }

    public Boolean getSaleOk() {
        return saleOk;
    }

    public void setSaleOk(Boolean saleOk) {
        this.saleOk = saleOk;
    }

    public Boolean getPurchaseOk() {
        return purchaseOk;
    }

    public void setPurchaseOk(Boolean purchaseOk) {
        this.purchaseOk = purchaseOk;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSubstituteProducts() {
        return substituteProducts;
    }

    public void setSubstituteProducts(String substituteProducts) {
        this.substituteProducts = substituteProducts;
    }

    public Number getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Number lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Number getLastDiscount() {
        return lastDiscount;
    }

    public void setLastDiscount(Number lastDiscount) {
        this.lastDiscount = lastDiscount;
    }

    public String getLastDiscountType() {
        return lastDiscountType;
    }

    public void setLastDiscountType(String lastDiscountType) {
        this.lastDiscountType = lastDiscountType;
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

    public Boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(Boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    public Number getUl() {
        return ul;
    }

    public void setUl(Number ul) {
        this.ul = ul;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public FilterCollection getRemoteFilters() {
        FilterCollection filters = new FilterCollection();
        try {
            List<Integer> productIds1 = OrderLineRepository.getInstance().getDiferentProductIds();
            List<Integer> productIds2 = PricelistPricesRepository.getInstance().getDiferentProductIds();
            if (productIds1.size() > 0 && productIds2.size() > 0) {
                filters.add(FilterCollection.FilterOperator.OR);
            }
            if (productIds1.size() > 0) {
                filters.add("id", "in", productIds1.toArray(new Integer[]{}));
            }
            if (productIds2.size() > 0) {
                filters.add("id", "in", productIds2.toArray(new Integer[]{}));
            }
            //filters.add("sale_ok", "=", true);
        } catch (OpeneERPApiException e) {
            e.printStackTrace();
        }
        return filters;
    }

    @Override
    public Integer getPendingSynchronization() {
        return null;
    }

    @Override
    public void setPendingSynchronization(Integer pendingSynchronization) {

    }

    public ProductTemplate getProductTemplate() {
        try {
            return ProductTemplateRepository.getInstance().getById(
                    this.productTmplId.longValue());
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return null;//TODO no enviar
    }

    public ProductUl getProductUl() {
        try {
            return ProductUlRepository.getInstance().getById(
                    this.ul.longValue());
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return null;
    }

    public ProductUom getUom() {
        try {
            return ProductUomRepository.getInstance().getById(
                    this.uomId.longValue());
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return null;
    }

    public ProductUom getUos() {
        try {
            if ((this.uosId != null) && (this.uosId.longValue() != 0.0)){
                return ProductUomRepository.getInstance().getById(
                        this.uosId.longValue());
            }
            else
                return getUom();

        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return null;
    }

    // FIXME implement this when we know data of minPrice for product
    // public Float getMinPrice() {
    // return price.floatValue() - (0.1F * price.floatValue());
    // }

    // FIXME implement this when we know data of maxDiscount for product
    public Float getMaxDiscount() {
        return 40F;
    }

    public Number getUosId() {
        return uosId;
    }

    public void setUosId(Number uosId) {
        this.uosId = uosId;
    }

    public Number getBoxUnits() {
        return boxUnits;
    }

    public void setBoxUnits(Number boxUnits) {
        this.boxUnits = boxUnits;
    }

    public Number getBoxesPerPallet() {
        return boxesPerPallet;
    }

    public void setBoxesPerPallet(Number boxesPerPallet) {
        this.boxesPerPallet = boxesPerPallet;
    }

    public Number getPalletGrossWeight() {
        return palletGrossWeight;
    }

    public void setPalletGrossWeight(Number palletGrossWeight) {
        this.palletGrossWeight = palletGrossWeight;
    }

    public Number getPalletTotalHeight() {
        return palletTotalHeight;
    }

    public void setPalletTotalHeight(Number palletTotalHeight) {
        this.palletTotalHeight = palletTotalHeight;
    }

    public Number getTypeOfPallet() {
        return typeOfPallet;
    }

    public void setTypeOfPallet(Number typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
    }
}
