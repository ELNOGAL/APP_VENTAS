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
import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.annotations.Remote;
import com.cafedered.midban.annotations.RemoteProperty;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.service.repositories.OrderLineRepository;
import com.cafedered.midban.service.repositories.PartnerRepository;
import com.cafedered.midban.utils.DateUtil;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.OpeneERPApiException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity(tableName = "sale_order")
@Remote(object = "sale.order")
public class Order extends BaseRemoteEntity implements Comparable<Order> {

    private static final long serialVersionUID = 2282169925763946161L;
    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "pendingSynchronization")
    private Integer pendingSynchronization;

    @Property(columnName = "amount_tax", type = Property.SQLType.REAL)
    @RemoteProperty(name = "amount_tax")
    private Number amountTax;

    //FIXME warehouse_id

    @Property(columnName = "margin", type = Property.SQLType.REAL)
    @RemoteProperty(name = "margin")
    private Number margin;

    @Property(columnName = "margin_perc", type = Property.SQLType.REAL)
    @RemoteProperty(name = "margin_perc")
    private Number marginPerc;

    @Property(columnName = "amount_untaxed", type = Property.SQLType.REAL)
    @RemoteProperty(name = "amount_untaxed")
    private Number amountUntaxed;

    @Property(columnName = "partner_id")
    @RemoteProperty(name = "partner_id")
    private Number partnerId;

    @Property(columnName = "amount_total", type = Property.SQLType.REAL)
    @RemoteProperty(name = "amount_total")
    private Number amountTotal;

    @Property(columnName = "pricelist_id")
    @RemoteProperty(name = "pricelist_id")
    private Number pricelistId;

    @Property(columnName = "partner_shipping_id")
    @RemoteProperty(name = "partner_shipping_id")
    private Number partnerShippingId;

    @Property(columnName = "create_date")
    @RemoteProperty(name = "create_date")
    private String createDate;

    @Property(columnName = "channel")
    @RemoteProperty(name = "channel")
    private String channel;

    @Property(columnName = "note")
    @RemoteProperty(name = "note")
    private String note;

    @Property(columnName = "name")
    @RemoteProperty(name = "name")
    private String name;

    @Property(columnName = "partner_invoice_id")
    @RemoteProperty(name = "partner_invoice_id")
    private Number partnerInvoiceId;

    @Property(columnName = "state")
    @RemoteProperty(name = "state")
    private String state;

    @Property(columnName = "date_order")
    @RemoteProperty(name = "date_order")
    private String dateOrder;

    @Property(columnName = "requested_date")
    @RemoteProperty(name = "requested_date")
    private String requestedDate;

    @Property(columnName = "invoiced")
    @RemoteProperty(name = "invoiced")
    private Boolean invoiced;

    @Property(columnName = "shipped")
    @RemoteProperty(name = "shipped")
    private Boolean shipped;

    @Property(columnName = "client_order_ref")
    @RemoteProperty(name = "client_order_ref")
    private String clientOrderRef;

    @Property(columnName = "shop_id")
    @RemoteProperty(name = "shop_id")
    private Number shopId;

    @Property(columnName = "user_id")
    @RemoteProperty(name = "user_id")
    private Number userId;


    private String addressSelected;

    public String getAddressSelected() {
        return addressSelected;
    }

    public void setAddressSelected(String addressSelected) {
        this.addressSelected = addressSelected;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    private List<OrderLine> lines = new ArrayList<OrderLine>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Number getAmountTax() {
        return amountTax;
    }

    public void setAmountTax(Number amountTax) {
        this.amountTax = amountTax;
    }

    public Number getMargin() {
        return margin;
    }

    public void setMargin(Number margin) {
        this.margin = margin;
    }

    public Number getMarginPerc() {
        return marginPerc;
    }

    public void setMarginPerc(Number marginPerc) {
        this.marginPerc = marginPerc;
    }

    public Number getAmountUntaxed() {
        return amountUntaxed;
    }

    public void setAmountUntaxed(Number amountUntaxed) {
        this.amountUntaxed = amountUntaxed;
    }

    public Number getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Number partnerId) {
        this.partnerId = partnerId;
    }

    public Number getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(Number amountTotal) {
        this.amountTotal = amountTotal;
    }

    public Number getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Number pricelistId) {
        this.pricelistId = pricelistId;
    }

    public Number getPartnerShippingId() {
        return partnerShippingId;
    }

    public void setPartnerShippingId(Number partnerShippingId) {
        this.partnerShippingId = partnerShippingId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Number getPartnerInvoiceId() {
        return partnerInvoiceId;
    }

    public void setPartnerInvoiceId(Number partnerInvoiceId) {
        this.partnerInvoiceId = partnerInvoiceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(String dateOrder) {
        this.dateOrder = dateOrder;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Boolean getShipped() {
        return shipped;
    }

    public boolean isShipped() {
        return shipped;
    }

    public void setShipped(Boolean shipped) {
        this.shipped = shipped;
    }

    public Boolean getInvoiced() {
        return invoiced;
    }

    public boolean isInvoiced() {
        return invoiced;
    }

    public void setInvoiced(Boolean invoiced) {
        this.invoiced = invoiced;
    }

    public String getClientOrderRef() {
        return clientOrderRef;
    }

    public void setClientOrderRef(String clientOrderRef) {
        this.clientOrderRef = clientOrderRef;
    }

    public Partner getPartner() {
        try {
            return PartnerRepository.getInstance().getById(
                    partnerId.longValue());
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return new Partner();
    }

    public Partner getPartnerShipping() {
        try {
            if (partnerShippingId != null) {
                return PartnerRepository.getInstance().getById(
                        partnerShippingId.longValue());
            } else {
                return getPartner();
            }
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return new Partner();
    }

    @Override
    public FilterCollection getRemoteFilters() {
        FilterCollection filters = new FilterCollection();
        Calendar calendar = Calendar.getInstance();
        // Dias totales que se van a sincronizar los pedidos
        calendar.add(Calendar.DATE, -MidbanApplication.getMaxDaysToSync()); // igual que MAX_DAYS_TO_SYNC / PGC
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            filters.add("date_order", ">", formatter.format(calendar.getTime()));
            //filters.add("user_id", "=", ((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER)).getId());
            List<Integer> partnerIds = new ArrayList<Integer>();
            Partner partnerExample = new Partner();
            partnerExample.setUserId(((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER)).getId());
            try {
                for (Partner p : PartnerRepository.getInstance().getByExampleUser(partnerExample, Restriction.AND, true, 100000, 0)) {
                    partnerIds.add(p.getId().intValue());
                }
                filters.add("partner_shipping_id", "in", partnerIds.toArray(new Integer[]{}));
            } catch (ServiceException e) {
                if (LoggerUtil.isDebugEnabled())
                    e.printStackTrace();
            }
        } catch (OpeneERPApiException e) {
            e.printStackTrace();
        }
        return filters;
    }

    public List<OrderLine> getLinesPersisted() {
        return OrderLineRepository.getInstance().getLinesByOrderId(id);
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getPendingSynchronization() {
        return pendingSynchronization;
    }

    @Override
    public void setPendingSynchronization(Integer pendingSynchronization) {
        this.pendingSynchronization = pendingSynchronization;
    }

    public void cloneWithoutIdentifiers(Order o) {
        o.setAmountTax(this.getAmountTax());
        o.setAmountTotal(this.getAmountTotal());
        o.setAmountUntaxed(this.getAmountUntaxed());
        o.setMargin(this.getMargin());
        o.setCreateDate(this.getCreateDate());
        o.setDateOrder(this.getDateOrder());
        o.setName(this.getName() + "cp" + DateUtil.toFormattedString(new Date(), "mmss"));
        o.setNote(this.getNote());
        o.setPartnerId(this.getPartnerId());
        o.setPartnerInvoiceId(this.getPartnerInvoiceId());
        o.setPartnerShippingId(this.getPartnerShippingId());
        o.setPricelistId(this.getPricelistId());
        o.setState(this.getState());
        for (OrderLine line : this.getLinesPersisted()) {
            OrderLine copy = new OrderLine();
            line.cloneWithoutIdentifiers(copy);
            o.getLines().add(copy);
        }
    }

    @Override
    public int compareTo(Order another) {
        if (getDateOrder() != null)
            return another.getDateOrder().compareTo(this.getDateOrder());
        return 0;
    }

    public Number getShopId() {
        return shopId;
    }

    public void setShopId(Number shopId) {
        this.shopId = shopId;
    }

    public Number getUserId() {
        return userId;
    }

    public void setUserId(Number userId) {
        this.userId = userId;
    }
}
