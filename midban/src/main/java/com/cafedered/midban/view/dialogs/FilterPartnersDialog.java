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
package com.cafedered.midban.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.R;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.CommercialRoute;
import com.cafedered.midban.entities.Partner;
import com.cafedered.midban.entities.User;
import com.cafedered.midban.service.repositories.CommercialRouteRepository;
import com.cafedered.midban.service.repositories.PartnerRepository;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.adapter.CustomArrayAdapter;
import com.cafedered.midban.view.adapter.PartnerListItemAdapter;
import com.cafedered.midban.view.fragments.PartnerListFragment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FilterPartnersDialog extends Dialog {

    private String route;
    private String city;
    private String zipCode;
    List<Partner> currentPartners;
    PartnerListItemAdapter adapter;
    Partner partnerExample;

    private static FilterPartnersDialog instance = null;

    public static FilterPartnersDialog getInstance(Context context,
            final PartnerListFragment fragment, ListView list, boolean first) {
        if (first)
            instance = null;
        if (instance == null)
            instance = new FilterPartnersDialog(context, fragment, list);
        return instance;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public FilterPartnersDialog(Context context,
            final PartnerListFragment fragment, final ListView list) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_partner_filters);
        final Spinner routes = (Spinner) findViewById(R.id.dialog_partner_filters_spinner_commercial_route);
        Set setRoutes = new LinkedHashSet<String>();
        setRoutes.add(getContext().getResources().getString(
                R.string.filter_partners_dialog_commercial_route));
        try {
            setRoutes.addAll(CommercialRouteRepository.getInstance()
                    .getAllDistinctSomeProperty("name"));
        } catch (ServiceException e1) {
            if (LoggerUtil.isDebugEnabled())
                e1.printStackTrace();
        }
        routes.setAdapter(new CustomArrayAdapter<String>(context, setRoutes));
        final Spinner cities = (Spinner) findViewById(R.id.dialog_partner_filters_spinner_city);
        Set setCities = new LinkedHashSet<String>();
        setCities.add(getContext().getResources().getString(
                R.string.filter_partners_dialog_cities));
        try {
            setCities.addAll(PartnerRepository.getInstance()
                    .getAllDistinctSomeProperty("city"));
        } catch (ServiceException e1) {
            if (LoggerUtil.isDebugEnabled())
                e1.printStackTrace();
        }
        cities.setAdapter(new CustomArrayAdapter<String>(context, setCities));

        final EditText zip = (EditText) findViewById(R.id.dialog_partner_filters_editext_zip);

        Button apply = (Button) findViewById(R.id.dialog_partner_filters_button_apply);
        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // FIXME add the correct values.
                route = routes.getSelectedItem().toString();
                city = cities.getSelectedItem().toString();
                zipCode = zip.getText().toString();
                partnerExample = new Partner();
                partnerExample.setUserId(((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER)).getId());
                if (!route.equals(getContext().getResources().getString(
                        R.string.filter_partners_dialog_commercial_route))) {
                    CommercialRoute routeExample = new CommercialRoute();
                    routeExample.setName(route);
                    try {
                        partnerExample.setCommercialRouteId(CommercialRouteRepository.getInstance().getByExample(routeExample, Restriction.AND, false, 0, 100000).get(0).getId());
                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                }
                if (!city.equals(getContext().getResources().getString(
                        R.string.filter_partners_dialog_cities)))
                    partnerExample.setCity(city);
                if (!zipCode.isEmpty())
                    partnerExample.setZip(zipCode);
                try {
                    currentPartners = PartnerRepository.getInstance()
                            .getByExampleUser(partnerExample, Restriction.AND,
                                    true, 100000, 0);
                    adapter = new PartnerListItemAdapter(fragment,
                            currentPartners);
                    list.setAdapter(adapter);
                    instance.cancel();
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                }
            }
        });

        TextView clearFilters = (TextView) findViewById(R.id.dialog_partner_filters_clear);
        clearFilters.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                routes.setSelection(0);
                cities.setSelection(0);
                zip.setText("");
            }
        });
    }

    public class FilterPartnersDialogScrollListener implements
            AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView arg0, int arg1) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, final int totalItemCount) {
            if (totalItemCount > 0) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (lastInScreen == totalItemCount) {
                    try {
                        currentPartners.addAll(PartnerRepository.getInstance()
                                .getByExample(partnerExample, Restriction.AND,
                                        true, totalItemCount, 10));

                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
