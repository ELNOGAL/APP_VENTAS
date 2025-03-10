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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cafedered.midban.R;
import com.cafedered.midban.annotations.Click;
import com.cafedered.midban.annotations.Fragment;
import com.cafedered.midban.annotations.Transformer;
import com.cafedered.midban.annotations.Wire;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.Configuration;
import com.cafedered.midban.entities.Warehouse;
import com.cafedered.midban.service.repositories.ConfigurationRepository;
import com.cafedered.midban.service.repositories.WarehouseRepository;
import com.cafedered.midban.utils.AndroidDatabaseManager;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.MessagesForUser;
import com.cafedered.midban.utils.SessionFactory;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.activities.LoginActivity;
import com.cafedered.midban.view.adapter.CustomArrayAdapter;
import com.cafedered.midban.view.base.BaseSupportFragment;
import com.cafedered.midban.view.transformers.ConfigurationTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Fragment(R.layout.fragment_configuration)
public class ConfigurationFragment extends BaseSupportFragment {

    @Wire(field = "protocol", view = R.id.fragment_configuration_field_protocol)
    private EditText protocol;
    @Wire(field = "urlOpenErp", view = R.id.fragment_configuration_field_url)
    private EditText openErpServer;
    @Wire(field = "portOpenErp", view = R.id.fragment_configuration_field_port)
    private EditText openErpPort;
    @Wire(field = "dbOpenErp", view = R.id.fragment_configuration_field_db)
    private EditText openErpDatabase;
    @Wire(field = "username", view = R.id.fragment_configuration_field_username)
    private EditText defaultUser;
    @Wire(field = "maxDaysToSync", view = R.id.fragment_configuration_field_max_days_to_synchronize)
    private EditText defaultMaxDaysToSync;
    @Wire(view = R.id.tv_configuration_version)
    private TextView version;
    @Wire(view = R.id.fragment_configuration_field_warehouse)
    private Spinner warehouseSelector;
    private int numberOfClicksForEasterEgg = 0;
//    @Wire(field = "emailLogs", view = R.id.fragment_configuration_field_logemail)
//    private EditText emailLogs;
    @Transformer
    private ConfigurationTransformer configurationTransformer;

    // Here we can use the onCreateView method if needed
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        try {
            Configuration configuration = ConfigurationRepository.getInstance()
                    .getConfiguration();
            if (configuration != null)
                configurationTransformer.toUi(configuration, this);
            else {
                // FIXME delete this hardcoded test data
                // PGC
                protocol.setText("http");
                openErpServer.setText("ip/hostname");
                openErpPort.setText("80");
                openErpDatabase.setText("database_name");
                defaultUser.setText("user_name");
                defaultMaxDaysToSync.setText("365");

            }

            version.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (numberOfClicksForEasterEgg == 7) {
                        Intent dbmanager = new Intent(getActivity(),AndroidDatabaseManager.class);
                        startActivity(dbmanager);
                    } else {
                        if (numberOfClicksForEasterEgg >= 4)
                            Toast.makeText(getActivity(), "Te quedan " + (7 - numberOfClicksForEasterEgg++) + " clicks para ver la BDD.", Toast.LENGTH_SHORT).show();
                        else
                            numberOfClicksForEasterEgg++;
                    }
                }
            });
            warehouseSelector.setAdapter(new CustomArrayAdapter<Warehouse>(
                    getActivity(), getWarehouseAdapter()));
            if (configuration != null) {
                for (int i = 0; i < warehouseSelector.getCount(); i++)
                    if (((Warehouse) warehouseSelector.getItemAtPosition(i)).getId().equals(configuration.getWarehouseId().longValue()))
                        warehouseSelector.setSelection(i);
                warehouseSelector.setEnabled(false);
            }
        } catch (ConfigurationException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        } catch (ServiceException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
        }
        return view;
    }

    @Click(view = R.id.fragment_configuration_button_save)
//    @Background(onOK = R.string.saved, onKO = R.string.cannot_save)
    public void save() throws ServiceException, ConfigurationException {
        try {
            final Configuration conf = configurationTransformer.toEntity(
                    new Configuration(), this);
            System.out.println(conf.toString());
            conf.setId(1L);
            Boolean guardarOk = true;
            if (Integer.parseInt(defaultMaxDaysToSync.getText().toString()) > 730) {
                guardarOk = false;
                MessagesForUser.showMessage(getView(), "Máximo de días a sincronizar = 730 días (2 años)", Toast.LENGTH_LONG, Level.SEVERE);
            }
            if (warehouseSelector.getSelectedItem() == null) {
                guardarOk = false;
                MessagesForUser.showMessage(getView(), "Ha de seleccionar un almacén", Toast.LENGTH_LONG, Level.SEVERE);
            }
            if (guardarOk) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.confirmar_configuracion)
                        .setMessage(R.string.realmente_desea_confirmar_configuracion)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                conf.setWarehouseId(((Warehouse) warehouseSelector.getSelectedItem()).getId());
                                try {
                                    ConfigurationRepository.getInstance().saveOrUpdate(conf);
                                } catch (ServiceException e) {
                                    e.printStackTrace();
                                }
                                SessionFactory.invalidateFactory();
                                // if (getValueFromContext(ContextAttributes.LOGGED_USER) == null)
                                startActivityForResult(
                                        getNextIntent(new Bundle(), getView(),
                                                LoginActivity.class), 0);
                                // else
                                // startActivityForResult(
                                // getNextIntent(new Bundle(), getView(),
                                // PortadaActivity.class), 0);
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Warehouse[] getWarehouseAdapter() {
        List<Warehouse> warehouses = new ArrayList<>();
        try {
            warehouses = WarehouseRepository.getInstance()
                    .getAll(0, 100000);
        } catch (ConfigurationException e) {
            // do nothing
        } catch (ServiceException e) {
            // do nothing
        }
        if (warehouses.size() == 0) {
            Warehouse wh1 = new Warehouse();
            wh1.setId(1L);
            wh1.setName("Quival");

            Warehouse wh2 = new Warehouse();
            wh2.setId(6L);
            wh2.setName("Valquin");

            Warehouse wh3 = new Warehouse();
            wh3.setId(4L);
            wh3.setName("Depósito Valquin");

            if (MidbanApplication.activeCompany == 3) {
                warehouses.add(wh1);
            } else {
                if (MidbanApplication.activeCompany == 2) {
                    warehouses.add(wh2);
                    warehouses.add(wh3);
                } else {
                    warehouses.add(wh1);
                    warehouses.add(wh2);
                    warehouses.add(wh3);
                }
            }
        }
        Warehouse[] result = new Warehouse[warehouses.size()];
        result = warehouses.toArray(result);
        return result;
    }
}
