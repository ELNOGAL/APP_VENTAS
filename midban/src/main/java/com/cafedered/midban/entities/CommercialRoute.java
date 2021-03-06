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

@Entity(tableName = "commercial_route")
@Remote(object = "commercial.route")
public class CommercialRoute extends BaseRemoteEntity implements Comparable<Order> {

    private static final long serialVersionUID = -6091215785405428790L;
    @Id(autoIncrement = false, column = "id")
    @RemoteProperty(name = "id")
    private Long id;

    @Property(columnName = "code")
    @RemoteProperty(name = "code")
    private String code;

    @Property(columnName = "name")
    @RemoteProperty(name = "name")
    private String name;

    @Property(columnName = "user_id")
    @RemoteProperty(name = "user_id")
    private Number userId;

    @Property(columnName = "sequence")
    @RemoteProperty(name = "sequence")
    private Number sequence;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
            Long uId = ((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER)).getId();
            {
                filters.add("user_id", "=", uId);
            }
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

    public Number getUserId() {
        return userId;
    }

    public void setUserId(Number userId) {
        this.userId = userId;
    }

    public Number getSequence() {
        return sequence;
    }

    public void setSequence(Number sequence) {
        this.sequence = sequence;
    }
}
