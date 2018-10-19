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
import com.debortoliwines.openerp.api.FilterCollection;

@Entity(tableName = "product_uom_categ")
@Remote(object = "product.uom.categ")
public class ProductUomCateg extends BaseRemoteEntity {

    private static final long serialVersionUID = -2820418892351702742L;

    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "name")
    @RemoteProperty(name = "name")
    private String name;

    @Override
    public Integer getPendingSynchronization() {
        // not needed
        return null;
    }

    @Override
    public void setPendingSynchronization(Integer pendingSynchronization) {
        // not needed
    }

    @Override
    public FilterCollection getRemoteFilters() {
        // not needed
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
