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
package com.cafedered.midban.service.repositories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.cafedered.cafedroidlitedao.exceptions.DatabaseException;
import com.cafedered.midban.dao.ProductCategoryDAO;
import com.cafedered.midban.entities.ProductCategory;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;

public class ProductCategoryRepository extends
        BaseRepository<ProductCategory, ProductCategoryDAO> {

    private static ProductCategoryRepository instance = null;

    public static ProductCategoryRepository getInstance() {
        if (instance == null)
            instance = new ProductCategoryRepository();
        return instance;
    }

    private ProductCategoryRepository() {
        dao = ProductCategoryDAO.getInstance();
    }

    public Collection<String> getFirstLevelCategories() {
        return dao.getFirstLevelCategories();
    }
}
