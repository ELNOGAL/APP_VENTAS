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
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.OpeneERPApiException;

import java.util.Date;

@Entity(tableName = "stock_picking_in")
@Remote(object = "stock.picking")
public class StockPickingIn extends BaseRemoteEntity {

    private static final long serialVersionUID = -3092381470399564259L;

    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "partner_id")
    @RemoteProperty(name = "partner_id")
    private Number partnerId;

    @Property(columnName = "picking_type_id")
    @RemoteProperty(name = "picking_type_id.code")
    private String pickingTypeId;

    // Diario de existencias
    @Property(columnName = "stock_journal_id")
    @RemoteProperty(name = "stock_journal_id")
    private Number stockJournalId;

    // Pedido de compra
    @Property(columnName = "purchase_id")
    @RemoteProperty(name = "purchase_id")
    private Number purchaseId;

    @Property(columnName = "date")
    @RemoteProperty(name = "date")
    private Date date;

    // Tiempo programado
    @Property(columnName = "min_date")
    @RemoteProperty(name = "min_date")
    private Date minDate;

    // Documento origen
    @Property(columnName = "origin")
    @RemoteProperty(name = "origin")
    private String origin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Number getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Number partnerId) {
        this.partnerId = partnerId;
    }

    public String getPickingTypeId() {
        return pickingTypeId;
    }

    public void setPickingTypeId(String pickingTypeId) {
        this.pickingTypeId = pickingTypeId;
    }

    public Number getStockJournalId() {
        return stockJournalId;
    }

    public void setStockJournalId(Number stockJournalId) {
        this.stockJournalId = stockJournalId;
    }

    public Number getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Number purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public FilterCollection getRemoteFilters() {
        FilterCollection filters = new FilterCollection();
        try {
            filters.add("user_id", "=", ((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER)).getId());
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
}
