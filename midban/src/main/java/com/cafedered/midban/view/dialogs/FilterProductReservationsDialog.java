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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.R;
import com.cafedered.midban.entities.Product;
import com.cafedered.midban.entities.ProductCategory;
import com.cafedered.midban.entities.ProductTemplate;
import com.cafedered.midban.service.repositories.ProductCategoryRepository;
import com.cafedered.midban.service.repositories.ProductRepository;
import com.cafedered.midban.service.repositories.ProductTemplateRepository;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.adapter.CustomArrayAdapter;
import com.cafedered.midban.view.adapter.ProductCatalogItemAdapter;
import com.cafedered.midban.view.fragments.PartnerReservationsFragment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FilterProductReservationsDialog extends Dialog {

    private String temperature;
    private String category;
    private String subcategory;
    private List<Product> currentProducts;
    private ProductCatalogItemAdapter adapter;
    private Product productExample;

    private static FilterProductReservationsDialog instance = null;

    public static FilterProductReservationsDialog getInstance(Context context,
            final PartnerReservationsFragment fragment, ListView list,
            boolean first) {
        if (first)
            instance = null;
        if (instance == null)
            instance = new FilterProductReservationsDialog(context, fragment,
                    list);
        return instance;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public FilterProductReservationsDialog(Context context,
            final PartnerReservationsFragment fragment, final ListView list) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_product_reservations_filters);
        final Spinner categories = (Spinner) findViewById(R.id.dialog_product_reservations_filters_spinner_category);
        final Spinner subcategories = (Spinner) findViewById(R.id.dialog_product_reservations_filters_spinner_subcategory);
        final Set setCategories = new LinkedHashSet<String>();
        setCategories.add(getContext().getResources().getString(
                R.string.dialog_product_reservations_filters_spinner_category));
        setCategories.addAll(ProductCategoryRepository.getInstance()
                .getFirstLevelCategories());
        categories.setAdapter(new CustomArrayAdapter<String>(context,
                setCategories));

        categories
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                            android.view.View v, int position, long id) {
                        Object[] categories = setCategories.toArray();
                        String categoryName = categories[position].toString();
                        ProductCategory categoryExample = new ProductCategory();
                        categoryExample.setCompleteName(categoryName);
                        ProductCategory category = new ProductCategory();

                        try {
                            List<ProductCategory> listCategory = ProductCategoryRepository
                                    .getInstance().getByExample(
                                            categoryExample, Restriction.AND,
                                            true, 0, 100000);
                            if (listCategory.size() > 0) {
                                category = listCategory.get(0);
                            }
                        } catch (ServiceException e2) {
                            if (LoggerUtil.isDebugEnabled())
                                e2.printStackTrace();
                        }

                        Set setSubcategories = new LinkedHashSet<String>();
                        setSubcategories
                                .add(getContext()
                                        .getResources()
                                        .getString(
                                                R.string.dialog_product_reservations_filters_spinner_subcategory));
                        try {
                            List<ProductCategory> categoriesList = ProductCategoryRepository
                                    .getInstance().getAll(0, 100000);
                            Long categoryId = category.getId();
                            if (categoriesList.size() > 0) {
                                for (int i = 0; i < categoriesList.size(); i++) {

                                    if ((null != categoriesList.get(i)
                                            .getParentId())
                                            && (null != categoryId)) {
                                        if (categoryId.equals((categoriesList
                                                .get(i).getParentId())
                                                .longValue())) {
                                            setSubcategories.add(categoriesList
                                                    .get(i).getName());
                                        }
                                    }
                                }
                            }

                        } catch (ServiceException e1) {
                            if (LoggerUtil.isDebugEnabled())
                                e1.printStackTrace();
                        } catch (ConfigurationException e) {
                            if (LoggerUtil.isDebugEnabled())
                                e.printStackTrace();
                        }
                        subcategories
                                .setAdapter(new CustomArrayAdapter<String>(v
                                        .getContext(), setSubcategories));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
        final EditText temperatureText = (EditText) findViewById(R.id.dialog_product_reservations_filters_editext_temperature);

        Button apply = (Button) findViewById(R.id.dialog_product_reservations_filters_button_apply);
        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                category = categories.getSelectedItem().toString();
                temperature = temperatureText.getText().toString();
                subcategory = subcategories.getSelectedItem().toString();
                productExample = new Product();
                if (!subcategory
                        .equals(getContext()
                                .getResources()
                                .getString(
                                        R.string.dialog_product_reservations_filters_spinner_subcategory))) {

                    ProductCategory productCategory = new ProductCategory();
                    productCategory.setName(subcategory);
                    try {
                        ProductTemplate productTemplate = new ProductTemplate();
                        productTemplate.setCategId(ProductCategoryRepository
                                .getInstance()
                                .getByExample(productCategory, Restriction.AND,
                                        true, 0, 100000).get(0).getId());
                        List<ProductTemplate> templatesList = ProductTemplateRepository
                                .getInstance().getByExample(productTemplate,
                                        Restriction.AND, true, 0, 100);
                        if (templatesList.size() > 0) {
                            productExample.setProductTmplId(templatesList
                                    .get(0).getId());
                        }
                    } catch (ServiceException e) {
                        // Unreachable
                    }
                } else {

                    if (!category
                            .equals(getContext()
                                    .getResources()
                                    .getString(
                                            R.string.dialog_product_reservations_filters_spinner_category))) {
                        ProductCategory productCategory = new ProductCategory();
                        productCategory.setCompleteName(category);
                        try {
                            ProductTemplate productTemplate = new ProductTemplate();
                            productTemplate
                                    .setCategId(ProductCategoryRepository
                                            .getInstance()
                                            .getByExample(productCategory,
                                                    Restriction.AND, true, 0,
                                                    100000).get(0).getId());
                            List<ProductTemplate> templatesList = ProductTemplateRepository
                                    .getInstance().getByExample(
                                            productTemplate, Restriction.AND,
                                            true, 0, 100);
                            if (templatesList.size() > 0) {
                                productExample.setProductTmplId(templatesList
                                        .get(0).getId());
                            }

                        } catch (ServiceException e) {
                            // Unreachable
                        }
                    }
                }

                if (!temperature.isEmpty())
                    productExample.setTemperature(Double
                            .parseDouble(temperature));
                try {
                    currentProducts = ProductRepository.getInstance()
                            .getByExample(productExample, Restriction.AND,
                                    true, 0, 10);
                    adapter = new ProductCatalogItemAdapter(fragment,
                            currentProducts);
                    list.setAdapter(adapter);
                    list.setOnScrollListener(new FilterProductReservationsDialogProductListener());
                    instance.cancel();
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                }
            }
        });

        TextView clearFilters = (TextView) findViewById(R.id.dialog_product_reservations_filters_clear);
        clearFilters.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                categories.setSelection(0);
                subcategories.setSelection(0);
                temperatureText.setText("");
            }
        });
    }

    public class FilterProductReservationsDialogProductListener implements
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
                        currentProducts.addAll(ProductRepository.getInstance()
                                .getByExample(
                                productExample, Restriction.AND, true,
 totalItemCount, 5));

                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

}
