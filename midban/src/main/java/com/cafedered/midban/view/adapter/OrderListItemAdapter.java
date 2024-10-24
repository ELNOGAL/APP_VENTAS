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
package com.cafedered.midban.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cafedered.midban.R;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.Order;
import com.cafedered.midban.entities.Shop;
import com.cafedered.midban.service.repositories.OrderRepository;
import com.cafedered.midban.service.repositories.ShopRepository;
import com.cafedered.midban.utils.DateUtil;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.activities.OrderActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderListItemAdapter extends BaseAdapter {

    private List<Order> orders;
    private static LayoutInflater inflater = null;
    private android.support.v4.app.Fragment fragment;

    public OrderListItemAdapter(android.support.v4.app.Fragment fragment, List<Order> orders) {
        this.orders = orders;
        if (fragment != null && fragment.getActivity() != null)
            inflater = (LayoutInflater) fragment.getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fragment = fragment;
    }

    public int getCount() {
        return orders.size();
    }

    public Object getItem(int position) {
        return orders.get(position);
    }

    public long getItemId(int position) {
        return orders.get(position).getId();
    }

    public static class ViewHolder {
        public TextView clientOrderRef;
        public TextView partnerName;
        public TextView lines;
        public TextView code;
        public TextView amount;
        public TextView amountUntaxed;
        public TextView state;
        public ImageView edit;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
            vi = inflater.inflate(R.layout.order_list_item, null);
            holder = new ViewHolder();
            holder.clientOrderRef = (TextView) vi
                    .findViewById(R.id.order_list_item_client_order_ref);
            holder.partnerName = (TextView) vi
                    .findViewById(R.id.order_list_item_partner_name);
            holder.code = (TextView) vi
                    .findViewById(R.id.order_list_item_code);
            holder.amount = (TextView) vi
                    .findViewById(R.id.order_list_item_amount);
            holder.amountUntaxed = (TextView) vi
                    .findViewById(R.id.order_list_item_amount_untaxed);
            holder.state = (TextView) vi
                    .findViewById(R.id.order_list_item_state);
            holder.lines = (TextView) vi
                    .findViewById(R.id.order_list_item_lines);
            holder.edit = (ImageView) vi
                    .findViewById(R.id.order_list_item_edit);

        final Order order = orders.get(position);
        if (order.getState() != null && !order.getState().equals("draft") && !order.getState().equals("not_sent")) {
            if (order.getPendingSynchronization() == null || order.getPendingSynchronization() != 1L) {
                holder.code.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OrderRepository.setCurrentOrder(order);
                        OrderRepository.getCurrentOrder().setLines(order.getLinesPersisted());
                        MidbanApplication.putValueInContext(ContextAttributes.READ_ONLY_ORDER_MODE,
                                Boolean.TRUE);
                        Intent intent = new Intent(v.getContext(),
                                OrderActivity.class);
                        intent.putExtras(new Bundle());
                        v.getContext().startActivity(intent);
                    }
                });
            }
        }
        if (!(order.getState().equals("manual") || order.getState().equals("progress") || order.getState().equals("wait_risk"))) {
            holder.edit.setVisibility(View.GONE);
        } else {
            holder.edit.setVisibility(View.VISIBLE);
        }
        // DAVID - VOLVER
        // LA EDICIÓN NO FUNCIONA ASÍ QUE VOY A DESACTIVAR EL BOTÓN
        holder.edit.setVisibility(View.GONE);

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.edit.setBackgroundColor(v.getResources().getColor(R.color.button_active_bg));
                OrderRepository.setCurrentOrder(order);
                OrderRepository.getCurrentOrder().setLines(order.getLinesPersisted());
                Intent intent = new Intent(v.getContext(),
                        OrderActivity.class);
                intent.putExtras(new Bundle());
                v.getContext().startActivity(intent);
                if (fragment != null)
                    fragment.getActivity().finish();
            }
        });
        holder.code.setText(order.getName());
        String shop_name = "";
        if (order.getShopId() != null) {
            try {
                Shop shop = ShopRepository.getInstance().getById(order.getShopId().longValue());
                if (shop != null) {
                    shop_name = shop.getName() == null ? "" : shop.getName();
                }
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
        }
        holder.clientOrderRef.setText(
                order.getClientOrderRef() == null || order.getClientOrderRef().equals("") ? shop_name : shop_name + "\n" + order.getClientOrderRef());
        holder.amount.setText("Total: " + new BigDecimal(order.getAmountTotal().floatValue()).setScale(2, RoundingMode.HALF_UP).toString()
                + " " + holder.amount.getResources().getString(
                        R.string.currency_symbol));
        if (order.getAmountUntaxed() != null) {
            holder.amountUntaxed.setText("Base: " + new BigDecimal(order.getAmountUntaxed().floatValue()).setScale(2, RoundingMode.HALF_UP).toString()
                    + " " + holder.amountUntaxed.getResources().getString(
                    R.string.currency_symbol));
        }
        if (order.getState() != null && order.getState().equals("draft"))
            holder.state.setText("Borrador");
        else if (order.getState() != null && order.getState().equals("sent"))
            holder.state.setText("Registrado");
        else if (order.getState() != null && order.getState().equals("not_sent"))
            holder.state.setText("Pdte. sincro");
        else if (order.getState() != null && order.getState().equals("wait_risk"))
            holder.state.setText("Pdte. riesgo");
        else if (order.getState() != null && order.getState().equals("risk_approved"))
            holder.state.setText("Riesgo OK");
        else if (order.getState() != null && order.getState().equals("progress"))
            holder.state.setText("Confirmado");
        else if (order.getState() != null && order.getState().equals("shipping_except"))
            holder.state.setText("Excepción");
        else if (order.getState() != null && order.getState().equals("done"))
            holder.state.setText("Realizado");
        else if (order.getState() != null && order.getState().equals("cancel"))
            holder.state.setText("Cancelado");
        else if (order.getState() != null)
            holder.state.setText(order.getState());
        if (order.getPendingSynchronization() != null && order.getPendingSynchronization() == 1) {
            holder.state.setText("Pdte. sincro");
            holder.edit.setVisibility(View.GONE);
            holder.state.setBackgroundColor(vi.getResources().getColor(R.color.button_active_bg));
        }
        try {
            holder.partnerName.setText(order.getPartnerShipping().getName()
                    .replace("null", ""));
        } catch (NullPointerException e) {
            // do nothing... it is better to catch this than instantiate new
            // partner for comparison
        }
        try {
            holder.lines.setText("Líneas: " + order.getLinesPersisted().size() + "");
            if (order.getState() != null && order.getState().equals("not_sent")) {
                // holder.lines.setText("Líneas: " + order.getLines().size());
                holder.lines.setText("");
            }
        } catch (Exception e) {
            holder.lines.setText("Líneas: " + order.getLines().size());
        }
        return vi;
    }
}