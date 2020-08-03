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
package com.cafedered.midban.dao;

import android.database.Cursor;
import android.util.Log;

import com.cafedered.cafedroidlitedao.annotations.Entity;
import com.cafedered.cafedroidlitedao.exceptions.BadConfigurationException;
import com.cafedered.cafedroidlitedao.exceptions.DatabaseException;
import com.cafedered.cafedroidlitedao.extractor.JDBCQueryMaker;
import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.BaseEntity;
import com.cafedered.midban.entities.Order;
import com.cafedered.midban.entities.OrderLine;
import com.cafedered.midban.entities.PricelistPrices;
import com.cafedered.midban.entities.Product;
import com.cafedered.midban.entities.ProductUom;
import com.cafedered.midban.entities.decorators.LastSaleCustomObject;
import com.cafedered.midban.service.repositories.OrderRepository;
import com.cafedered.midban.service.repositories.PartnerRepository;
import com.cafedered.midban.service.repositories.PricelistPricesRepository;
import com.cafedered.midban.service.repositories.ProductUomRepository;
import com.cafedered.midban.utils.DateUtil;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.ReflectionUtils;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ReflectionException;
import com.cafedered.midban.utils.exceptions.ServiceException;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO extends BaseDAO<Order> {

    private static OrderDAO instance;

    public static OrderDAO getInstance() {
        if (instance == null)
            instance = new OrderDAO();
        return instance;
    }
    public List<Product> getProductsOfPartnerWithDateFilters(Long idPartner, Long idShop, int datesBack) {
        String query;
        List<Product> products = new ArrayList<Product>();
        try {
            // Tenemos que buscar el commercial_partner_id, ya que las lineas de pedido no guardan la dirección de entrega
            // y si el idPartner corresponde a una dirección nunca tendría resultados
            Number commercialPartner = PartnerRepository.getInstance().getById(idPartner).getCommercialPartnerId();
            idPartner = commercialPartner.longValue();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            query = "SELECT p.*, "
                    + "l.price_unit as l_price_unit, l.discount as l_discount, "
                    + "l.app_discount_type as l_app_discount_type, MAX(o.date_order) as o_date_order "
                    + "FROM "
                    + ((BaseEntity) ReflectionUtils.createObject(Product.class))
                    .getClass().getAnnotation(Entity.class).tableName() + " p, "
                    + ((BaseEntity) ReflectionUtils.createObject(OrderLine.class))
                    .getClass().getAnnotation(Entity.class).tableName() + " l, "
                    + ((BaseEntity) ReflectionUtils.createObject(Order.class))
                    .getClass().getAnnotation(Entity.class).tableName() + " o"
                    + " WHERE l.order_partner_id = " + idPartner
                    + " AND l.order_id = o.id"
                    + " AND l.product_id = p.id";
            if (idShop != null) {
                query = query
                    + " AND o.shop_id = " + idShop;
            }
            if (datesBack != OrderRepository.DateFilters.LAST_ORDER.getDatesBack()) {
                Calendar date = Calendar.getInstance();
                date.add(Calendar.DATE, datesBack);
                query = query
                    + " AND o.date_order > '"
                    + DateUtil.toFormattedString(date.getTime(),
                    "yyyy-MM-dd") + "'";
                query = query + " GROUP BY p.id";
                query = query + " ORDER BY p.default_code";
            } else {
                Long idOrder = null;
                Cursor cursor = getDaoHelper()
                        .getReadableDatabase()
                        .rawQuery(
                                "SELECT MAX(id) FROM "
                                        + ((BaseEntity) ReflectionUtils.createObject(Order.class))
                                        .getClass()
                                        .getAnnotation(Entity.class)
                                        .tableName()
                                        + " WHERE partner_id = " + idPartner, null);
                if (!cursor.isAfterLast()) {
                    cursor.moveToFirst();
                    idOrder = cursor.getLong(0);
                }
                if (idOrder != null) {
                    query = query + " AND o.id = " + idOrder;
                } else
                    return products;
            }
            if (LoggerUtil.isDebugEnabled()) {
                Log.d(getClass().getName(), query);
            }
            Map<Long, Product> productMap = new LinkedHashMap<Long, Product>();
            Cursor cursor = getDaoHelper().getReadableDatabase().rawQuery(
                    query, null);
            if (!cursor.isAfterLast()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Product aProduct = (Product)JDBCQueryMaker.getObjectFromCursor(cursor, new Product());
                    aProduct.setIsFavourite(true);
                    // Precio última venta
                    aProduct.setLastPrice(cursor.getFloat(cursor.getColumnIndex("l_price_unit")));
                    // Descuento última venta
                    aProduct.setLastDiscount(cursor.getFloat(cursor.getColumnIndex("l_discount")));
                    // Tipo descuento última venta
                    String appDiscountType = cursor.getString(cursor.getColumnIndex("l_app_discount_type"));
                    if (appDiscountType == null || appDiscountType.equals("")) {
                        aProduct.setLastDiscountType("?");
                    } else {
                        aProduct.setLastDiscountType(appDiscountType);
                    }
                    String tarifaPorLaQueFiltrar = (String) MidbanApplication.getValueFromContext(ContextAttributes.ACTUAL_TARIFF);
                    if (!"".equals(tarifaPorLaQueFiltrar) && tarifaPorLaQueFiltrar != null) {
                        PricelistPrices pl = new PricelistPrices();
                        pl.setPricelistId(Long.parseLong(tarifaPorLaQueFiltrar));
                        pl.setProductId(aProduct.getId().longValue());
                        List<PricelistPrices> list = PricelistPricesRepository.getInstance().getByExample(pl, Restriction.AND, true, 0, 1);
                        if (list.size() == 1) {
                            // Precio tarifa
                            aProduct.setLstPrice(list.get(0).getPrice());
                            aProduct.setDiscount1(list.get(0).getDiscount1());
                            aProduct.setDiscount2(list.get(0).getDiscount2());
                            aProduct.setDiscount3(list.get(0).getDiscount3());
                        } else {
                            // No está en tarifa.
                            // Ponemos precio -1 para marcar el producto como que ha sido comprado pero no se puede vender por no estar en tarifa actual
                            aProduct.setLstPrice(-1);
                            aProduct.setDiscount1(0.0F);
                            aProduct.setDiscount2(0.0F);
                            aProduct.setDiscount3(0.0F);
                        }
                    }
                    productMap.put(aProduct.getId(), aProduct);
                    cursor.move(1);
                }
                cursor.close();
            }
            products.addAll(productMap.values());
        } catch (ReflectionException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (BadConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Map<Long, Number[]> getOrdersOfPartnerWithDateFilters(Long id,
            int datesBack) {
        Map<Long, Number[]> productIds = new HashMap<Long, Number[]>();
        String query;
        try {
            query = "SELECT l.product_id, l.product_uom_qty, l.product_packaging FROM "
                    + ((BaseEntity) ReflectionUtils
                            .createObject(OrderLine.class)).getClass()
                            .getAnnotation(Entity.class).tableName()
                    + " l, "
                    + ((BaseEntity) ReflectionUtils.createObject(Order.class))
                            .getClass().getAnnotation(Entity.class).tableName()
                    + " o WHERE o.partner_shipping_id = "
                    + id
                    + " AND l.order_id = o.id";

            if (datesBack != OrderRepository.DateFilters.LAST_ORDER
                    .getDatesBack()) {
                Calendar date = Calendar.getInstance();
                date.add(Calendar.DATE, datesBack);
                query = query
                        + " AND o.date_order > '"
                        + DateUtil.toFormattedString(date.getTime(),
                                "yyyy-MM-dd") + "'";
            } else {
                Long idOrder = null;
                Cursor cursor = getDaoHelper()
                        .getReadableDatabase()
                        .rawQuery(
                                "SELECT MAX(id) FROM "
                                        + ((BaseEntity) ReflectionUtils.createObject(Order.class))
                                                .getClass()
                                                .getAnnotation(Entity.class)
                                                .tableName()
                                        + " WHERE partner_shipping_id = " + id, null);
                if (!cursor.isAfterLast()) {
                    cursor.moveToFirst();
                    idOrder = cursor.getLong(0);
                }
                if (idOrder != null) {
                    query = query + " AND o.id = " + idOrder;
                } else
                    return productIds;
            }
            if (LoggerUtil.isDebugEnabled()) {
                Log.d(getClass().getName(), query);
            }
            Cursor cursor = getDaoHelper().getReadableDatabase().rawQuery(
                    query, null);
            if (!cursor.isAfterLast()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    productIds.put(
                            cursor.getLong(0),
                            new Number[] { cursor.getDouble(1),
                                    cursor.getLong(2) });
                    cursor.move(1);
                }
                cursor.close();
            }
        } catch (ReflectionException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return productIds;
    }

    public List<LastSaleCustomObject> getProductLastSalesForPartner(
            Long productId, Long partnerId, Long partnerShippingId) {
        String query = "SELECT o.date_order, l.price_unit, l.price_subtotal, l.product_uom_qty, l.discount, l.app_discount_type, l.product_uos, o.id"
                + " FROM sale_order o, sale_order_line l"
                + " WHERE 1=1";
        if (partnerShippingId != null) {
            query = query
                + " AND o.partner_shipping_id = " + partnerShippingId;
        } else {
            query = query
                + " AND o.partner_id = " + partnerId;
        }
        query = query
                + " AND l.product_id = " + productId
                + " AND l.order_id = o.id"
                + " ORDER BY o.date_order DESC";
        if (LoggerUtil.isDebugEnabled())
            System.out.println(query);
        Cursor cursor = getDaoHelper().getReadableDatabase().rawQuery(query,
                null);
        List<LastSaleCustomObject> result = new ArrayList<LastSaleCustomObject>();
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                LastSaleCustomObject lastSaleCO = new LastSaleCustomObject();
                try {
                    lastSaleCO.setDate(DateUtil.toFormattedString(DateUtil
                                    .parseDate(cursor.getString(0), "yyyy-MM-dd"),
                            "dd.MM.yyyy"));
                } catch (ParseException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                }
                lastSaleCO.setDiscount("" + cursor.getDouble(4));
                if (cursor.getString(5) != null) {
                    lastSaleCO.setDiscountType("" + cursor.getString(5));
                } else {
                    lastSaleCO.setDiscountType("");
                }
                try {
                    ProductUom uom = ProductUomRepository.getInstance().getById(Long.parseLong("" + cursor.getInt(6)));
                    lastSaleCO.setPackaging(uom.getName());
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                lastSaleCO.setPrice("" + cursor.getDouble(1));
                lastSaleCO.setQuantity("" + cursor.getDouble(3));
                lastSaleCO.setTotal("" + cursor.getDouble(2));
                String queryCount = "SELECT COUNT(*) FROM sale_order_line WHERE order_id = "
                        + cursor.getInt(7);
                if (LoggerUtil.isDebugEnabled())
                    System.out.println(queryCount);
                Cursor countCursor = getDaoHelper().getReadableDatabase()
                        .rawQuery(queryCount, null);
                if (!countCursor.isAfterLast()) {
                    countCursor.moveToFirst();
                    lastSaleCO.setLines("" + countCursor.getInt(0));
                }
                countCursor.close();
                result.add(lastSaleCO);
                cursor.move(1);
            }
        }
        cursor.close();
        return result;
    }

    public List<Order> getOrdersWithFilters(String state, String dateFrom,
            String dateTo, Float amountLessThan, Float amountMoreThan,
            Long partnerId, boolean orderByName) {
        String query;
        List<Order> result = new ArrayList<Order>();
        try {
            query = "SELECT * FROM "
                    + ((BaseEntity) ReflectionUtils.createObject(Order.class))
                            .getClass().getAnnotation(Entity.class).tableName()
                    + " WHERE 1=1 ";
            if (partnerId != null)
                query += " AND partner_id = " + partnerId + " ";
            String[] args = new String[5];
            int count = 0;
            if (state != null) {
                query += " AND state LIKE ?";
                args[count] = state;
                count++;
            }
            if (dateFrom != null) {
                query += " AND create_date > ?";
                args[count] = dateFrom;
                count++;
            }
            if (dateTo != null) {
                query += " AND create_date < ?";
                args[count] = dateTo;
                count++;
            }
            if (amountLessThan != null) {
                query += " AND amount_total < ?";
                args[count] = "" + amountLessThan;
                count++;
            }
            if (amountMoreThan != null) {
                query += " AND amount_total > ?";
                args[count] = "" + amountMoreThan;
                count++;
            }

            if (orderByName){
                query += " ORDER BY name DESC ";
            }

            String[] params = new String[count];
            for (int i = 0; i < count; i++)
                params[i] = args[i];
            if (LoggerUtil.isDebugEnabled())
                Log.d(getClass().getName(), "Query: [" + query + "], params: "
                        + params);
            Cursor cursor = getDaoHelper().getReadableDatabase().rawQuery(
                    query, params);
            if (!cursor.isAfterLast()) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Order order = new Order();
                    order.setId(cursor.getLong(cursor.getColumnIndex("id")));
                    order.setDateOrder(cursor.getString(cursor
                            .getColumnIndex("date_order")));
                    order.setAmountTotal(cursor.getDouble(cursor
                            .getColumnIndex("amount_total")));
                    order.setState(cursor.getString(cursor
                            .getColumnIndex("state")));

                    result.add(order);
                    cursor.move(1);
                }
                cursor.close();
            }
        } catch (ReflectionException e) {
            // unreached
        }
        return result;
    }

    public void updateSynchronizationStatus(Long id, int i) throws DatabaseException, ConfigurationException {
        Order order = getUniqueResult(id);
        order.setPendingSynchronization(1);
        saveOrUpdate(order);
    }
}
