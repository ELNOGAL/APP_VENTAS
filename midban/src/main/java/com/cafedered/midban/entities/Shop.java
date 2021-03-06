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
import com.debortoliwines.openerp.api.OpeneERPApiException;

@Entity(tableName = "sale_shop")
@Remote(object = "sale.shop")
public class Shop extends BaseRemoteEntity implements Comparable<Order> {

    private static final long serialVersionUID = 2282169925763946165L;
    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "name")
    @RemoteProperty(name = "name")
    private String name;

    @Property(columnName = "indirect_invoicing")
    @RemoteProperty(name = "indirect_invoicing")
    private Boolean indirectInvoicing;

    @Property(columnName = "pricelist_id")
    @RemoteProperty(name = "pricelist_id")
    private Number pricelistId;

    @Property(columnName = "in_app")
    @RemoteProperty(name = "in_app")
    private Boolean inApp;

    @Property(columnName = "active")
    @RemoteProperty(name = "active")
    private Boolean active;

    @Property(columnName = "sequence")
    @RemoteProperty(name = "sequence")
    private Number sequence;


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

    @Override
    public FilterCollection getRemoteFilters() {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add(FilterCollection.FilterOperator.OR);
            filters.add("active", "=", true);
            filters.add("active", "=", false);
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

    @Override
    public int compareTo(Order another) {
        return 0;
    }


    public Boolean getIndirectInvoicing() {
        return indirectInvoicing;
    }

    public void setIndirectInvoicing(Boolean indirectInvoicing) {
        this.indirectInvoicing = indirectInvoicing;
    }

    public Number getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Number pricelistId) {
        this.pricelistId = pricelistId;
    }

    public Boolean getInApp() {
        return inApp;
    }

    public void setInApp(Boolean inApp) {
        this.inApp = inApp;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Number getSequence() {
        return sequence;
    }

    public void setSequence(Number sequence) {
        this.sequence = sequence;
    }
}
