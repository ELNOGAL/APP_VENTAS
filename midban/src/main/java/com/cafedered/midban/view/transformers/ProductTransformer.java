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
package com.cafedered.midban.view.transformers;

import com.cafedered.midban.entities.Product;
import com.cafedered.midban.utils.exceptions.ConfigurationException;

public class ProductTransformer extends BaseTransformer<Product> {

    public ProductTransformer() {
        super();
    }

    public Product toEntity(Product product, android.support.v4.app.Fragment fragment)
            throws ConfigurationException {
        if (product == null)
            product = new Product();
        transformUiToEntity(product, fragment);
        return product;
    }

    public void toUi(Product product, android.support.v4.app.Fragment fragment)
            throws ConfigurationException {
        transformEntityToUi(product, fragment);
    }
}
