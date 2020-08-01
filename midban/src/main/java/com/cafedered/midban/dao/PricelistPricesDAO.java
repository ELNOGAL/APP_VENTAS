package com.cafedered.midban.dao;

import android.database.Cursor;

import com.cafedered.midban.entities.PricelistPrices;

import java.util.ArrayList;
import java.util.List;

public class PricelistPricesDAO extends BaseDAO<PricelistPrices> {

    private static PricelistPricesDAO instance;

    public static PricelistPricesDAO getInstance() {
        if (instance == null)
            instance = new PricelistPricesDAO();
        return instance;
    }

    public List<Integer> getDiferentProductIds() {
        List<Integer> productIds = new ArrayList<Integer>();
        String query = "SELECT DISTINCT product_id FROM table_pricelist_prices";
        Cursor cursor = getDaoHelper().getReadableDatabase().rawQuery(query,
                null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                productIds.add(cursor.getInt(0));
                cursor.move(1);
            }
            cursor.close();
        }
        return productIds;
    }

}
