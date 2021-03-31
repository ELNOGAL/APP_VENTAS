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
package com.cafedered.midban.view.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.R;
import com.cafedered.midban.annotations.Fragment;
import com.cafedered.midban.annotations.Wire;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.Order;
import com.cafedered.midban.entities.Shop;
import com.cafedered.midban.service.repositories.OrderRepository;
import com.cafedered.midban.service.repositories.PartnerRepository;
import com.cafedered.midban.service.repositories.ShopRepository;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.MessagesForUser;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.adapter.OrderListItemAdapter;
import com.cafedered.midban.view.base.BaseSupportFragment;
import com.cafedered.midban.view.listeners.ITodayDataChangedListenerFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

@Fragment(R.layout.fragment_order_list)
public class OrderListFragment extends BaseSupportFragment implements
        ITodayDataChangedListenerFragment {

    @Wire(view = R.id.fragment_order_num_orders)
    TextView numOrders;
    @Wire(view = R.id.fragment_order_amount_orders_untaxed)
    TextView amountOrdersUntaxed;
    @Wire(view = R.id.fragment_order_amount_orders)
    TextView amountOrders;
    @Wire(view = R.id.fragment_order_margin_orders)
    TextView marginOrders;
    @Wire(view = R.id.fragment_order_list_listview)
    ListView list;
    List<Order> currentOrders;
    OrderListItemAdapter adapter;
    Order example;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = super.onCreateView(inflater, container,
                savedInstanceState);
        setHasOptionsMenu(true);
        try {
            if (OrderRepository.getUnsynchronizedOrders().size() > 0) {
                String ordersToSynchronize = "Hay " + OrderRepository.getUnsynchronizedOrders().size() + " pedidos pendientes de sincronizar:\n";
                for (HashMap<String, Object> pendingOrder : OrderRepository.getUnsynchronizedOrders()) {
                    String partnerName = PartnerRepository.getInstance().getById(((Integer)pendingOrder.get("partner_id")).longValue()).getName();
                    ordersToSynchronize += "- " + partnerName + "\n";
                }
                MessagesForUser.showMessage(rootView, ordersToSynchronize, Toast.LENGTH_LONG, Level.WARNING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadListItems(
                MidbanApplication.getValueFromContext(
                        ContextAttributes.TODAY_ACTIVITY_DATE_SELECTED)
                        .toString(), rootView);
        return rootView;
    }

    private void loadListItems(final String date, final View view) {
        new AsyncTask<Void, Void, List<Order>>() {
            @Override
            protected List<Order> doInBackground(Void... params) {
                try {
                    example = new Order();
                    example.setDateOrder(date);
                    List<Order> orders = OrderRepository.getInstance().getByExample(example,
                            Restriction.AND, false, 0, 100);

                    // DAVID - voy a añadir los pedidos no sincronizados
                    if (OrderRepository.getUnsynchronizedOrders().size() > 0) {
                        for (HashMap<String, Object> pendingOrder : OrderRepository.getUnsynchronizedOrders()) {
                            Order order = new Order();
                            if (pendingOrder.get("id") != null) {
                                order.setId((Long) pendingOrder.get("id"));
                            } else {
                                order.setId(Calendar.getInstance().getTimeInMillis());
                            }
                            if (pendingOrder.get("name").equals("/")) {
                                order.setName("(Pendiente)");
                                order.setState("not_sent");
                            } else {
                                order.setName((String) pendingOrder.get("name"));
                                order.setState((String) pendingOrder.get("state"));
                            }
                            order.setDateOrder((String) pendingOrder.get("date_order"));
                            order.setCreateDate((String) pendingOrder.get("create_date"));
                            order.setAmountTax((Number) pendingOrder.get("amount_tax"));
                            order.setAmountTotal((Number) pendingOrder.get("amount_total"));
                            order.setAmountUntaxed((Number) pendingOrder.get("amount_untaxed"));
                            order.setPartnerId((Number) pendingOrder.get("partner_id"));
                            order.setClientOrderRef((String) pendingOrder.get("client_order_ref"));
                            // Leemos las lineas
                            /*
                            Object order_line[] = (Object[]) pendingOrder.get("order_line");
                            List<OrderLine> orderLines = new ArrayList();
                            for (int i = 0; i < order_line.length; i++) {
                                Object lines2[] = (Object[]) order_line[i];
                                Object lines = (Object) lines2[(lines2).length - 1];
                                OrderLine line = new OrderLine();
                                line.setProductId(((HashMap<String, Number>) lines).get("product_id").intValue());
                                line.setName(((HashMap<String, String>) lines).get("name"));
                                line.setProductUom(((HashMap<String, Number>) lines).get("product_uom").intValue());
                                line.setProductUos(((HashMap<String, Number>) lines).get("product_uos").intValue());
                                line.setProductUomQuantity(((HashMap<String, Number>) lines).get("product_uom_qty").intValue());
                                line.setProductUosQuantity(((HashMap<String, Number>) lines).get("product_uos_qty").intValue());
                                line.setPriceUnit(((HashMap<String, Number>) lines).get("price_unit").doubleValue());
                                line.setDiscount(((HashMap<String, Number>) lines).get("discount").doubleValue());
                                line.setDiscountType(((HashMap<String, String>) lines).get("discount_type"));
                                orderLines.add(line);
                            }
                            order.setLines(orderLines);
                            */
                            // Añadimos el pedido a la lista
                            orders.add(order);
                        }
                    }

                    Iterator<Order> itOrderes = orders.iterator();
                    while(itOrderes.hasNext()) {
                        if (itOrderes.next().getName().equals("/"))
                            itOrderes.remove();
                    }
                    return orders;
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                    return new ArrayList<Order>();
                }
            }

            @Override
            protected void onPostExecute(List<Order> result) {
                super.onPostExecute(result);
                currentOrders = result;
                adapter = new OrderListItemAdapter(OrderListFragment.this,
                        currentOrders);
                list.setAdapter(adapter);
                numOrders.setText(view.getResources().getString(
                        R.string.fragment_order_list_num_orders)
                        + " " + result.size());
                float amount_untaxed = 0.0F;
                float amount_total = 0.0F;
                float margin = 0.0F;
                float margin_perc = 0.0F;
                float amount_untaxed_direct_invoicing = 0.0F;
                for (Order order : result) {
                    amount_untaxed += order.getAmountUntaxed() != null ? order
                            .getAmountUntaxed().floatValue() : 0.0F;
                    amount_total += order.getAmountTotal() != null ? order
                            .getAmountTotal().floatValue() : 0.0F;
                    if (order.getShopId() != null) {
                        try {
                            Shop shop = ShopRepository.getInstance().getById(order.getShopId().longValue());
                            if (shop != null && shop.getIndirectInvoicing() == false) {
                                amount_untaxed_direct_invoicing += order.getAmountUntaxed() != null ? order
                                        .getAmountUntaxed().floatValue() : 0.0F;
                                margin += order.getMargin() != null ? order
                                        .getMargin().floatValue() : 0.0F;
                            }
                        } catch (ConfigurationException e) {
                            e.printStackTrace();
                        } catch (ServiceException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (amount_untaxed_direct_invoicing != 0.0F) {
                    margin_perc = 100 * margin / amount_untaxed_direct_invoicing;
                }
                amountOrdersUntaxed.setText(view.getResources().getString(
                        R.string.fragment_order_list_amount_untaxed_orders)
                        + " "
                        + new BigDecimal(amount_untaxed).setScale(2, RoundingMode.HALF_UP).toString()
                        + " " + view.getResources().getString(
                        R.string.currency_symbol));
                amountOrders.setText(view.getResources().getString(
                        R.string.fragment_order_list_amount_orders)
                        + " "
                        + new BigDecimal(amount_total).setScale(2, RoundingMode.HALF_UP).toString()
                        + " " + view.getResources().getString(
                        R.string.currency_symbol));
                marginOrders.setText(view.getResources().getString(
                        R.string.fragment_order_list_margin_orders)
                        + " "
                        + new BigDecimal(margin).setScale(2, RoundingMode.HALF_UP).toString()
                        + " " + view.getResources().getString(
                        R.string.currency_symbol)
                        + " (" + new BigDecimal(margin_perc).setScale(2, RoundingMode.HALF_UP).toString() + "%)");
            }
        }.execute();
    }

    @Override
    public void notifyDataChangedListener(String newDate) {
        loadListItems(newDate, getView());
    }
}
