package com.cafedered.midban.view.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.R;
import com.cafedered.midban.annotations.Click;
import com.cafedered.midban.annotations.Fragment;
import com.cafedered.midban.annotations.Wire;
import com.cafedered.midban.async.CancelAsyncTaskListener;
import com.cafedered.midban.async.OrderSynchronizationService;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.entities.AccountPaymentTerm;
import com.cafedered.midban.entities.CommercialRoute;
import com.cafedered.midban.entities.Order;
import com.cafedered.midban.entities.OrderLine;
import com.cafedered.midban.entities.Partner;
import com.cafedered.midban.entities.PaymentMode;
import com.cafedered.midban.entities.Product;
import com.cafedered.midban.entities.ProductUom;
import com.cafedered.midban.entities.Shop;
import com.cafedered.midban.entities.Tax;
import com.cafedered.midban.entities.User;
import com.cafedered.midban.service.repositories.AccountPaymentTermRepository;
import com.cafedered.midban.service.repositories.CommercialRouteRepository;
import com.cafedered.midban.service.repositories.ConfigurationRepository;
import com.cafedered.midban.service.repositories.OrderLineRepository;
import com.cafedered.midban.service.repositories.OrderRepository;
import com.cafedered.midban.service.repositories.PartnerRepository;
import com.cafedered.midban.service.repositories.PaymentModeRepository;
import com.cafedered.midban.service.repositories.ProductRepository;
import com.cafedered.midban.service.repositories.ProductUomRepository;
import com.cafedered.midban.service.repositories.ShopRepository;
import com.cafedered.midban.service.repositories.TaxRepository;
import com.cafedered.midban.utils.DateUtil;
import com.cafedered.midban.utils.LoggerUtil;
import com.cafedered.midban.utils.MessagesForUser;
import com.cafedered.midban.utils.SessionFactory;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;
import com.cafedered.midban.view.activities.LastSalesActivity;
import com.cafedered.midban.view.activities.PortadaActivity;
import com.cafedered.midban.view.activities.ProductCardActivity;
import com.cafedered.midban.view.adapter.OrderLinesNewDispositionAdapter;
import com.cafedered.midban.view.adapter.ProductOrderItemAdapter;
import com.cafedered.midban.view.base.BaseSupportActivity;
import com.cafedered.midban.view.base.BaseSupportFragment;
import com.cafedered.midban.view.dialogs.OneFieldEditionDialog;
import com.debortoliwines.openerp.api.FilterCollection;
import com.debortoliwines.openerp.api.ObjectAdapter;
import com.debortoliwines.openerp.api.OpenERPCommand;
import com.debortoliwines.openerp.api.Row;
import com.debortoliwines.openerp.api.RowCollection;
import com.debortoliwines.openerp.api.Session;

import org.droidparts.widget.ClearableEditText;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * Created by nacho on 18/10/15.
 */
@Fragment(R.layout.fragment_order_new_disposition)
public class OrderNewDispositionFragment extends BaseSupportFragment implements CancelAsyncTaskListener,
        ProductOrderItemAdapter.IProductSelectable, OrderLinesNewDispositionAdapter.OrderLineUnitChangedListener, OrderLinesNewDispositionAdapter.OrderLineSelected {

    private boolean mostrarFavoritos = true;

    @Wire(view = R.id.fragment_order_shop_tv)
    private TextView shopView;
    @Wire(view = R.id.fragment_order_new_disposition_button_shop)
    private FloatingActionButton fragment_order_new_disposition_button_shop;
    @Wire(view = R.id.fragment_order_ref_et)
    private ClearableEditText refView;
    @Wire(view = R.id.fragment_order_delivery_date_et)
    private ClearableEditText deliveryDateView;
    @Wire(view = R.id.fragment_order_new_disposition_amount_total)
    private TextView amountTotalView;
    @Wire(view = R.id.fragment_order_new_disposition_amount_untaxed)
    private TextView amountUntaxedView;
    @Wire(view = R.id.fragment_order_new_disposition_credit_available)
    private TextView creditAvailableView;
    @Wire(view = R.id.fragment_order_new_disposition_margin)
    private TextView marginView;
    @Wire(view = R.id.fragment_order_new_disposition_number_of_lines)
    private TextView numberOfLinesView;
    @Wire(view = R.id.fragment_order_new_disposition_product_search_field)
    private ClearableEditText productSearchField;
    @Wire(view = R.id.fragment_order_new_disposition_favourites_toggle)
    private TextView favouritesToggle;
    @Wire(view = R.id.fragment_order_new_disposition_product_favourites)
    private ListView favouriteProductsListView;
    @Wire(view = R.id.fragment_order_new_disposition_all_catalog_toggle)
    private TextView allCatalogToggle;
    @Wire(view = R.id.fragment_order_new_disposition_product_catalog)
    private ListView allCatalogListView;
    @Wire(view = R.id.fragment_order_notes)
    private TextView orderNotesView;
    @Wire(view = R.id.fragment_order_new_disposition_lines_lv)
    private ListView orderLinesListView;
    @Wire(view = R.id.fragment_order_new_disposition_button_cancel)
    private FloatingActionButton buttonCancel;
    @Wire(view = R.id.fragment_order_new_disposition_button_ok)
    private FloatingActionButton buttonOk;
    @Wire(view = R.id.fragment_order_new_disposition_button_repeat)
    private FloatingActionButton buttonRepeat;
    @Wire(view = R.id.fragment_order_new_disposition_payment_mode)
    private TextView paymentMode;
    @Wire(view = R.id.fragment_order_new_disposition_payment_term)
    private TextView paymentTerm;
    @Wire(view = R.id.fragment_order_new_disposition_textview_loading_items)
    private TextView loadingItems;

    private Float availableDebitOnline;

    private boolean readOnlyMode = false;

    private Partner partner;
    Product product;
    List<Product> favouriteProducts;
    List<Product> allProducts;
    List<Product> currentFavouriteProducts = new ArrayList<Product>();
    List<Product> currentAllProducts = new ArrayList<Product>();
    SearchTask task;
    Long lastTimeSearch = 0L;
    ProductOrderItemAdapter adapterAll;
    ProductOrderItemAdapter adapterFavourite;
    LinearLayout footerView;
    LinearLayout footerOrderLinesView;
    OrderLinesNewDispositionAdapter adapterLines;
    boolean reloadProducts = true;
    private int numberOfFooterViewOpened = 0;
    int indexListAllCatalog = -1;
    int topListAllCatalog = -1;
    List<OrderLine> oldLines;

    String _tarifaActual = "";

    static boolean flag = false;

    void setTarifaActual(String t){
        _tarifaActual = t;
        OrderRepository.getCurrentOrder().setPricelistId(Long.parseLong(_tarifaActual));
        MidbanApplication.putValueInContext(ContextAttributes.ACTUAL_TARIFF, _tarifaActual);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = super.onCreateView(inflater, container,
                savedInstanceState);


        if (!mostrarFavoritos) {
            favouritesToggle.setVisibility(View.GONE);
            favouriteProductsListView.setVisibility(View.GONE);
        } else {
            // hacemos que los favoritos no aparezcan desplegados, sino lo queremos así sobra el else
            favouriteProductsListView.setVisibility(View.GONE);
            favouritesToggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MidbanApplication.getContext(), R.drawable.general_flecha_abajo), null);
        }

        setHasOptionsMenu(true);
        productSearchField.setOnClickListener(new View.OnClickListener() {
            int i = 0;
            @Override
            public void onClick(View v) {
                i++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        i = 0;
                    }
                };
                if (i == 1) {
                    handler.postDelayed(r, 250);
                } else if (i == 2) {
                    if (flag) {
                        productSearchField.setInputType(InputType.TYPE_CLASS_TEXT);
                        flag = false;
                    } else {
                        productSearchField.setInputType(InputType.TYPE_CLASS_NUMBER);
                        flag = true;
                    }
                    i = 0;
                }
            }
        });
        productSearchField.addTextChangedListener(new SearchTextChangedListener());
        // oculto la casilla de búsqueda al abrir la ventana, se mostrará al cargar datos
        productSearchField.setVisibility(View.GONE);
        footerView = (LinearLayout) rootView.findViewById(R.id.list_footer);
        ImageView imgWheel = (ImageView) rootView.findViewById(R.id.img_animated_wheel);
        AnimationDrawable frameAnimation = (AnimationDrawable) imgWheel.getDrawable();
        frameAnimation.start();
        imgWheel.setImageDrawable(frameAnimation);
        footerOrderLinesView = (LinearLayout) rootView.findViewById(R.id.list_footer_order_lines);
        ImageView imgWheelLines = (ImageView) rootView.findViewById(R.id.img_animated_wheel_order_lines);
        AnimationDrawable frameAnimationLines = (AnimationDrawable) imgWheelLines.getDrawable();
        frameAnimationLines.start();
        imgWheel.setImageDrawable(frameAnimation);
        if (MidbanApplication
                .getValueFromContext(ContextAttributes.READ_ONLY_ORDER_MODE) != null
                && (Boolean) MidbanApplication.getValueFromContext(
                ContextAttributes.READ_ONLY_ORDER_MODE)) {
            partner = OrderRepository.getCurrentOrder().getPartnerShipping();
            readOnlyMode = true;
        } else if (OrderRepository.getInstance().isOrderInitialized()
                && OrderRepository.getCurrentOrder().getPartnerId() != null)
            partner = OrderRepository.getCurrentOrder().getPartner();
        if (partner == null || partner.getName() == null /* || partner.getRef() == null*/)
            partner = (Partner) MidbanApplication
                    .getValueFromContext(ContextAttributes.PARTNER_TO_ORDER);
        if (partner == null || partner.getName() == null /*|| partner.getRef() == null*/) // it came from partner detail
            partner = (Partner) MidbanApplication
                    .getValueFromContext(ContextAttributes.PARTNER_TO_DETAIL);
        if (partner == null) {
            getActivity().finish();
        } else {
            String title = partner.getName();
            ((BaseSupportActivity) getActivity()).getSupportActionBar().setTitle(title.length() > 58 ? title.substring(0, 20) + "..." + title.substring(title.length() - 38) : title);
            calculateRiskLimit(partner);
            OrderRepository.getCurrentOrder().setPartnerId(partner.getId());
            paymentMode.setText(getString(R.string.fragment_partner_detail_tab_card_payment_mode));
            if (partner.getCustomerPaymentMode() != null){
                try {
                    PaymentMode pm = PaymentModeRepository.getInstance().getById(partner.getCustomerPaymentMode().longValue());
                    if (pm != null){
                        paymentMode.setText(getString(R.string.fragment_partner_detail_tab_card_payment_mode) + " " + pm.getName());
                    }
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }
            paymentTerm.setText(getString(R.string.fragment_partner_detail_tab_card_account_payment_term));
            if (partner.getPropertyPaymentTerm() != null){
                try {
                    AccountPaymentTerm pm = AccountPaymentTermRepository.getInstance().getById(partner.getPropertyPaymentTerm().longValue());
                    if (pm != null){
                        paymentTerm.setText(getString(R.string.fragment_partner_detail_tab_card_account_payment_term) + " " + pm.getName());
                    }
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }

            if (OrderRepository.getCurrentOrder().getShopId() == null
                    && OrderRepository.getCurrentOrder().getId() == 0L) {
                try {
                    // Si se trata de un pedido nuevo y sólo hay una tienda disponible, la seleccionamos automáticamente
                    // Experimental -> 25-11-2021 Pedro Gómez
                    Shop shop = new Shop();
                    shop.setInApp(true);
                    shop.setActive(true);
                    List<Shop> shopList = ShopRepository.getInstance().getByExample(shop, Restriction.AND, true, 0, 2);
                    if (shopList.size() == 1) {
                        Long id = shopList.get(0).getId();
                        OrderRepository.getCurrentOrder().setShopId(id);
                        favouriteProducts = null;
                    }
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }

            if (OrderRepository.getCurrentOrder().getShopId() != null) {
                try {
                    Shop shop = ShopRepository.getInstance().getById(OrderRepository.getCurrentOrder().getShopId().longValue());
                    if (shop != null) {
                        shopView.setText(shop.getName());
                        reloadProducts = true;
                        loadOnResume();
                    }
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }

            oldLines = new ArrayList<OrderLine>();
            oldLines.addAll(OrderRepository.getCurrentOrder().getLines());
            adapterLines = new OrderLinesNewDispositionAdapter(getActivity(),
                    OrderRepository.getCurrentOrder().getLines(), this);
            orderLinesListView.setAdapter(adapterLines);
            numberOfLinesView.setText("Núm. Líneas: "
                    + OrderRepository.getCurrentOrder().getLines().size());
            if (OrderRepository.getCurrentOrder().getAmountTotal() != null)
                amountTotalView.setText("Total: " + OrderRepository.getCurrentOrder()
                        .getAmountTotal().toString()
                        + " " + getResources().getString(R.string.currency_symbol));
            if (OrderRepository.getCurrentOrder().getAmountUntaxed() != null)
                amountUntaxedView.setText("Base: " + OrderRepository.getCurrentOrder()
                        .getAmountUntaxed().toString()
                        + " " + getResources().getString(R.string.currency_symbol));
            if (OrderRepository.getCurrentOrder().getMargin() != null)
                marginView.setText("Margen: " + OrderRepository.getCurrentOrder()
                        .getMargin().toString()
                        + " " + getResources().getString(R.string.currency_symbol)
                        + " (" + OrderRepository.getCurrentOrder().getMarginPerc().toString() + "%)");
            refView.setText(OrderRepository.getCurrentOrder().getClientOrderRef());
            if (OrderRepository.getCurrentOrder().getRequestedDate() != null) {
                try {
                    deliveryDateView.setText(DateUtil.toFormattedString(DateUtil.parseDate(OrderRepository.getCurrentOrder().getRequestedDate()), "dd.MM.yyyy"));
                } catch (ParseException e) {
                    //do nothing
                }
            }
            if (readOnlyMode) {
                buttonOk.setVisibility(View.GONE);
                buttonRepeat.setVisibility(View.VISIBLE);
            } else {
                buttonOk.setVisibility(View.VISIBLE);
                buttonRepeat.setVisibility(View.GONE);
            }
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
        if (readOnlyMode) {
            buttonOk.setVisibility(View.GONE);
            buttonRepeat.setVisibility(View.VISIBLE);
        } else {
            buttonOk.setVisibility(View.VISIBLE);
            buttonRepeat.setVisibility(View.GONE);
        }

        reloadProducts =
                (OrderRepository.getCurrentOrder() != null) &&
                (OrderRepository.getCurrentOrder().getShopId() != null) &&
                ((currentAllProducts == null) || ((currentAllProducts != null) && (currentAllProducts.size() == 0)));
        loadOnResume();
    }

    public void loadOnResume() {
        loadingItems.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    if (OrderRepository.getCurrentOrder().getRequestedDate() != null) {
                        try {
                            deliveryDateView.setText(DateUtil.toFormattedString(DateUtil.parseDate(OrderRepository.getCurrentOrder().getRequestedDate()), "dd.MM.yyyy"));
                        } catch (ParseException e) {
                            //do nothing
                        }
                    }
                    numberOfLinesView.setText("Núm. Líneas: "
                            + OrderRepository.getCurrentOrder().getLines().size());
                    adapterLines.notifyDataSetChanged();
                    calculateAmounts();
                    amountTotalView.setText("Total: " + OrderRepository.getCurrentOrder()
                            .getAmountTotal().toString()
                            + " " + getResources().getString(R.string.currency_symbol));
                    if (OrderRepository.getCurrentOrder().getAmountUntaxed() != null)
                        amountUntaxedView.setText("Base: " + OrderRepository.getCurrentOrder()
                                .getAmountUntaxed().toString()
                                + " " + getResources().getString(R.string.currency_symbol));
                    if (OrderRepository.getCurrentOrder().getMargin() != null)
                        marginView.setText("Margen: " + OrderRepository.getCurrentOrder()
                                .getMargin().toString()
                                + " " + getResources().getString(R.string.currency_symbol)
                                + " (" + OrderRepository.getCurrentOrder().getMarginPerc().toString() + "%)");
                    if (readOnlyMode) {
                        deliveryDateView.setEnabled(false);
                    } else {
                        deliveryDateView.setEnabled(true);
                    }
                    if (reloadProducts)
                        obtainProductsForPartner(partner);
                    calculateRiskLimit(partner);
                }

                loadingItems.setVisibility(View.GONE);
            }
        }, 200 ); // time in miliseconds
    }

    public boolean isEditable() {
        return !readOnlyMode;
    }

    private void obtainProductsForPartner(Partner partner) {
        // si recargo tengo que limpiar la lista anterior
        if (allProducts != null) {
            allProducts.clear();
            allProducts = null;
            indexListAllCatalog = -1;
        }
        if (allProducts == null && indexListAllCatalog == -1) {
            boolean loaded = false;

            allProducts = new ArrayList<Product>();

            String info = "";
/*
(En Odoo, los campos property_product_pricelist y property_product_pricelist_indirect_invoicing son commercial_fields
y por tanto al escribir un valor en el commercial_partner_id se propaga a las direcciones)

- Se selecciona tienda:
 - Si indirect_invoicing = True
    - En primer lugar busca la tarifa de indirectos en el campo del cliente "property_product_pricelist_indirect_invoicing"
    - Si no hay nada en ese campo:
       - Si en la tienda está informado "pricelist_id", cargaria productos y precios de esta tarifa
       - Si no está informado "pricelist_id", caería en el caso de indirect_invoicing = False

  - Si indirect_invoicing = False
    - Carga la tarifa de venta del cliente (productos y precios)
    - Si no hay nada en ese campo:
       - Si en la tienda está informado "pricelist_id", cargaria productos y precios de esta tarifa
*/
            if (OrderRepository.getCurrentOrder().getShopId() != null) {
                try {
                    Shop shop = ShopRepository.getInstance().getById(OrderRepository.getCurrentOrder().getShopId().longValue());
                    if (shop != null) {
                        Partner p = PartnerRepository.getInstance().getById(partner.getId().longValue());
                        // - Si indirect_invoicing = True
                        if (shop.getIndirectInvoicing()) {
                            // - En primer lugar busca la tarifa de indirectos en el campo del cliente "property_product_pricelist_indirect_invoicing"
                            if ((p != null) && (p.getPricelistIndirectId() != null) && (p.getPricelistIndirectId().longValue() > 0)) {
                                if (p.getIsCompany() == true) {
                                    info = "Dirección principal. Carga indirecta. \nCliente " + p.getName();
                                } else {
                                    info = "Dirección de entrega. Carga indirecta. \nCliente " + p.getName();
                                }
                                loaded = true;
                                setTarifaActual(p.getPricelistIndirectId().toString());
                            }
                            // - Si en la tienda está informado "pricelist_id", cargaria productos y precios de esta tarifa
                            if (!loaded) {
                                if ((shop.getPricelistId() != null) && (shop.getPricelistId().longValue() > 0)) {
                                    info = "Carga indirecta tienda " + shop.getName();
                                    loaded = true;
                                    setTarifaActual(shop.getPricelistId().toString());
                                }
                            }
                        }
                        // - Carga la tarifa de venta del cliente (productos y precios)
                        if (!loaded) {
                            if ((p != null) && (p.getPricelistId() != null) && (p.getPricelistId().longValue() > 0)) {
                                if (p.getIsCompany() == true) {
                                    info = "Dirección principal. Carga directa. \nCliente " + p.getName();
                                } else {
                                    info = "Dirección de entrega. Carga directa. \nCliente " + p.getName();
                                }
                                loaded = true;
                                setTarifaActual(p.getPricelistId().toString());
                            }
                        }
                        // - Si en la tienda está informado "pricelist_id", cargaria productos y precios de esta tarifa
                        if (!loaded) {
                            if ((shop.getPricelistId() != null) && (shop.getPricelistId().longValue() > 0)) {
                                info = "Carga directa tienda " + shop.getName();
                                loaded = true;
                                setTarifaActual(shop.getPricelistId().toString());
                            }
                        }
                    }
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }

            info = info + "\nTarifa: " + _tarifaActual;
            Toast.makeText(getContext(), info, Toast.LENGTH_LONG).show();

            currentAllProducts.addAll(allProducts);
            adapterAll = new ProductOrderItemAdapter(this, currentAllProducts);
            allCatalogListView.setAdapter(adapterAll);

            //onSearchTextChanged();
            productSearchField.setText("");

            // vuelvo a mostrar la opción de búsqueda
            productSearchField.setVisibility(View.VISIBLE);

        } else {
            if (indexListAllCatalog != -1 && topListAllCatalog != -1) {
                allCatalogListView.setSelectionFromTop(indexListAllCatalog, topListAllCatalog);
            }
        }
        if (mostrarFavoritos) {
            if (favouriteProducts != null) {
                favouriteProducts.clear();
                favouriteProducts = null;
            }
            if (favouriteProducts == null) {
                Long shop = null;
                if (OrderRepository.getCurrentOrder().getShopId() != null) {
                    shop = OrderRepository.getCurrentOrder().getShopId().longValue();
                }
                favouriteProducts = OrderRepository.getInstance().
                        getProductFavouritesForPartner(partner.getId(), shop);
                currentFavouriteProducts.addAll(favouriteProducts);
                adapterFavourite = new ProductOrderItemAdapter(this, currentFavouriteProducts);
                favouriteProductsListView.setAdapter(adapterFavourite);

                // onSearchTextChanged();
                productSearchField.setText("");

                // vuelvo a mostrar la opción de búsqueda
                productSearchField.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onSelect(final Product product) {
        if (!readOnlyMode) {
            int countEqualLines = 0;
            ProductUom uomOtherLineEqualProduct = null;
            ProductUom uosOtherLineEqualProduct = null;
            for (OrderLine aLine : OrderRepository.getCurrentOrder().getLines()) {
                if (aLine.getProduct().getId().equals(product.getId())) {
                    countEqualLines++;
                    try {
                        uomOtherLineEqualProduct = ProductUomRepository.getInstance().getById(aLine.getProductUom().longValue());
                        uosOtherLineEqualProduct = ProductUomRepository.getInstance().getById(aLine.getProductUos().longValue());
                    } catch (ConfigurationException e) {
                        e.printStackTrace();
                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }

                    if (countEqualLines == 2) {
                        MessagesForUser.showMessage(getView(), getResources().getString(R.string.product_was_already_added), Toast.LENGTH_SHORT, Level.WARNING);
                        return;
                    }
                }
            }

            if (countEqualLines == 1) {
                OrderLine line = new OrderLine();
                line.setPriceSubtotal(0.0F);
                line.setPriceUnit(0.0F);
                line.setProductId(product.getId());
                line.setProductUos(uosOtherLineEqualProduct.getId());
                line.setProductUom(uomOtherLineEqualProduct.getId());
                line.setProductUomQuantity(1.0);
                line.setProductUosQuantity(1);
                line.setDiscount(0.0F);
                line.setDiscount1(0.0F);
                line.setDiscount2(0.0F);
                line.setDiscount3(0.0F);
                line.setDiscountType("-1");
                Number taxId = product.getProductTemplate().getTaxesId();
                if (taxId == null) {
                    MessagesForUser.showMessage(getView(), getResources().getString(R.string.error_al_anadir_producto), Toast.LENGTH_SHORT, Level.SEVERE);
                    return;
                }
                line.setTaxesId(new Number[]{taxId});
                OrderRepository.getCurrentOrder().getLines().add(line);
                MessagesForUser.showMessage(getView(), getResources().getString(R.string.product_free_of_charge_added), Toast.LENGTH_LONG, Level.WARNING);
                reloadProducts = false;
                loadOnResume();
                return;
            }

            // Obtener precio del producto
            new AsyncTask<Void, Void, BigDecimal>() {
                OrderLine line = new OrderLine();

                int visibilityOfOkButton = buttonOk.getVisibility();

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    buttonOk.setVisibility(View.INVISIBLE);

                    line.setProductId(product.getId());
                    line.setDiscount(0);
                    line.setDiscount1(product.getDiscount1());
                    line.setDiscount2(product.getDiscount2());
                    line.setDiscount3(product.getDiscount3());
                    line.setDiscountType("0");
                    line.setOrderPartnerId(partner.getId());
                    line.setPriceUnit(0.0000000001);
                    ProductUom uom = product.getUom();
                    ProductUom uos = product.getUos();
                    line.setProductUom(uom.getId());
                    line.setProductUos(uos.getId());
                    line.setProductUosQuantity(1);
                    line.setPriceSubtotal(1 * line.getPriceUdv().floatValue());
                    Number taxId = product.getProductTemplate().getTaxesId();
                    if (taxId == null) {
                        MessagesForUser.showMessage(getView(), getResources().getString(R.string.error_al_anadir_producto), Toast.LENGTH_SHORT, Level.SEVERE);
                        return;
                    }
                    line.setTaxesId(new Number[]{taxId});
                    OrderRepository.getCurrentOrder().getLines().add(line);
                    reloadProducts = false;
                    loadOnResume();
                    footerOrderLinesView.setVisibility(View.VISIBLE);
                    numberOfFooterViewOpened++;
                }

                @Override
                protected BigDecimal doInBackground(Void... params) {
                    return ProductRepository
                            .getInstance()
                            .getCalculatedPrice(
                                    product,
                                    partner,
                                    _tarifaActual,
                                    ((User) MidbanApplication
                                            .getValueFromContext(ContextAttributes.LOGGED_USER))
                                            .getLogin(),
                                    ((User) MidbanApplication
                                            .getValueFromContext(ContextAttributes.LOGGED_USER))
                                            .getPasswd());
                }

                @Override
                protected void onPostExecute(BigDecimal result) {
                    super.onPostExecute(result);
                    buttonOk.setVisibility(visibilityOfOkButton);
                    for (OrderLine aLine : OrderRepository.getCurrentOrder().getLines()) {
                        if (aLine.getProduct().getId().equals(product.getId())) {
                            line = aLine;
                        }
                    }
                    line.setPriceUnit(result.floatValue());
                    line.setPriceSubtotal(line.getPriceUnit().floatValue() * line.getProductUomQuantity().floatValue());
                    reloadProducts = false;
                    loadOnResume();
                    numberOfFooterViewOpened--;
                    if (numberOfFooterViewOpened == 0)
                        footerOrderLinesView.setVisibility(View.GONE);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            MessagesForUser.showMessage(getView(), getResources().getString(R.string.units_cannot_be_modified_when_read_only_mode), Toast.LENGTH_LONG, Level.SEVERE);
        }
    }

    @Click(view = R.id.fragment_order_new_disposition_button_shop)
    public void clickSelectShop(){

        try {
            if (OrderRepository.getCurrentOrder().getLines().size() > 0) {
                MessagesForUser.showMessage(getView(), getResources().getString(R.string.order_shop_no_permitido_cambio_tienda), Toast.LENGTH_SHORT, Level.SEVERE);
                return;
            }
            List<Shop> shops = ShopRepository.getInstance().getAll(0, 100000);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
            builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle("Tienda:");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice); //  select_dialog_singlechoice
            for (Shop shop: shops) {
                if (shop.getInApp() && shop.getActive()) {
                    arrayAdapter.add(shop.getName());
                }
            }

            builderSingle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String strName = arrayAdapter.getItem(which);
                    Shop shop = new Shop();
                    shop.setName(strName);
                    try {
                        List<Shop> shopList = ShopRepository.getInstance().getByExample(shop, Restriction.AND, true, 0, 1);

                        if (shopList.size() == 1) {
                            Long id = shopList.get(0).getId();
                            OrderRepository.getCurrentOrder().setShopId(id);
                            shopView.setText(strName);
                            favouriteProducts = null;
                            reloadProducts = true;
                            loadOnResume();
                        }

                    } catch (ServiceException e) {
                        e.printStackTrace();
                    }
                }
            });
            builderSingle.show();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Click(view = R.id.fragment_order_new_disposition_favourites_toggle)
    public void clickFavourites() {
        if (favouriteProductsListView.getVisibility() == View.GONE) {
            favouriteProductsListView.setVisibility(View.VISIBLE);
            favouritesToggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MidbanApplication.getContext(), R.drawable.general_flecha_arriba), null);
        } else {
            favouriteProductsListView.setVisibility(View.GONE);
            favouritesToggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MidbanApplication.getContext(), R.drawable.general_flecha_abajo), null);
        }
    }

    @Click(view = R.id.fragment_order_new_disposition_all_catalog_toggle)
    public void clickAllCatalog() {
        if (allCatalogListView.getVisibility() == View.GONE) {
            allCatalogListView.setVisibility(View.VISIBLE);
            allCatalogToggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MidbanApplication.getContext(), R.drawable.general_flecha_arriba), null);
        } else {
            allCatalogListView.setVisibility(View.GONE);
            allCatalogToggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MidbanApplication.getContext(), R.drawable.general_flecha_abajo), null);
        }
    }

    @Click(view = R.id.fragment_order_new_disposition_button_cancel)
    public void onCancelButtonPressed() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.cancelacion_pedido)
                .setMessage(R.string.realmente_desea_cancelar_pedido)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearContext();
                        getActivity().finish();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Click(view = R.id.fragment_order_new_disposition_button_ok)
    public void onConfirmButtonPressed() {
        if (!readOnlyMode) {
            if (validateOrder()) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.confirmacion_pedido)
                        .setMessage(R.string.realmente_desea_confirmar_pedido)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final User user = (User) MidbanApplication
                                        .getValueFromContext(ContextAttributes.LOGGED_USER);
                                try {
                                    OrderRepository.getCurrentOrder().setCreateDate(
                                            DateUtil.toFormattedString(new Date()));
                                    OrderRepository.getCurrentOrder().setDateOrder(DateUtil.convertToUTC(DateUtil.toFormattedString(new Date())));
                                    OrderRepository.getCurrentOrder().setMargin(0.0);
                                    OrderRepository.getCurrentOrder().setNote(orderNotesView.getText().toString());
                                    OrderRepository.getCurrentOrder().setPartnerId(partner.getId());
                                    OrderRepository.getCurrentOrder().setPartnerInvoiceId(partner.getId());
                                    OrderRepository.getCurrentOrder().setPartnerShippingId(partner.getId());
                                    OrderRepository.getCurrentOrder().setState("draft");
                                    calculateAmounts();
                                    OrderRepository.getCurrentOrder().setPricelistId(Long.parseLong(_tarifaActual));
                                    OrderRepository.getCurrentOrder().setClientOrderRef(refView.getText().toString());
                                    if (OrderRepository.getCurrentOrder().getName() == null)
                                        OrderRepository.getCurrentOrder().setName("/");
                                    OrderRepository.getCurrentOrder().setChannel("tablet");
                                    numberOfLinesView.setText("Núm. Lineas: "
                                            + OrderRepository.getCurrentOrder().getLines().size());
                                    amountTotalView.setText("Total: " + OrderRepository.getCurrentOrder()
                                            .getAmountTotal().toString()
                                            + " " + getResources().getString(R.string.currency_symbol));
                                    if (OrderRepository.getCurrentOrder().getAmountUntaxed() != null)
                                        amountUntaxedView.setText("Base: " + OrderRepository.getCurrentOrder()
                                                .getAmountUntaxed().toString()
                                                + " " + getResources().getString(R.string.currency_symbol));
                                    if (OrderRepository.getCurrentOrder().getMargin() != null)
                                        marginView.setText("Margen: " + OrderRepository.getCurrentOrder()
                                                .getMargin().toString()
                                                + " " + getResources().getString(R.string.currency_symbol)
                                                + " (" + OrderRepository.getCurrentOrder().getMarginPerc().toString() + "%)");
                                    orderLinesListView.setAdapter(new OrderLinesNewDispositionAdapter(OrderNewDispositionFragment.this.getActivity(),
                                            OrderRepository.getCurrentOrder().getLines(), OrderNewDispositionFragment.this));
                                } catch (Exception e) {
                                    if (LoggerUtil.isDebugEnabled())
                                        e.printStackTrace();
                                }

                                confirmarPedido(user, OrderRepository.getCurrentOrder(), true);

                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        } else {
            MessagesForUser.showMessage(getView(), "El pedido ya ha sido confirmado anteriormente", 4000, Level.SEVERE);
        }
    }

    private HashMap<String, Object> fillOrder(Order confirmOrder, boolean sendLines) throws ServiceException, ConfigurationException {
        HashMap<String, Object> orderOdoo = new HashMap<String, Object>();

        // if (confirmOrder.getId().intValue() == OrderRepository.getInstance().getNextIdNumber()) {
        if (confirmOrder.getId() == 0L) {
            List<Object> linesOrderOdoo = new ArrayList<Object>();
            if (sendLines) {
                fillLineParamsWithOrderLines(linesOrderOdoo, confirmOrder.getLines(), false);
            }
            // DAVID - vamos a enviar el company_id ya que en el contexto no les llega a servidor
            orderOdoo.put("app_company_id", MidbanApplication.activeCompany);
            // DAVID - por petición del integrador campo "delay" con valor a 5 en cabecera
            // orderOdoo.put("delay", 5);
            if (confirmOrder.getShopId() != null)
                orderOdoo.put("shop_id", confirmOrder.getShopId().intValue());
            orderOdoo.put("client_order_ref", confirmOrder.getClientOrderRef());
            orderOdoo.put("partner_id", confirmOrder.getPartnerId().intValue());
            orderOdoo.put("order_line", linesOrderOdoo.toArray());
            orderOdoo.put("warehouse_id", ConfigurationRepository.getInstance().getConfiguration().getWarehouseId().intValue());
            if (confirmOrder.getPricelistId() != null)
                orderOdoo.put("pricelist_id", confirmOrder.getPricelistId().intValue());
            if (confirmOrder.getAmountTax() != null)
                orderOdoo.put("amount_tax", confirmOrder.getAmountTax().doubleValue());
            if (confirmOrder.getMargin() != null)
                orderOdoo.put("margin", confirmOrder.getMargin().doubleValue());
            if (confirmOrder.getAmountUntaxed() != null)
                orderOdoo.put("amount_untaxed", confirmOrder.getAmountUntaxed().doubleValue());
            if (confirmOrder.getAmountTotal() != null)
                orderOdoo.put("amount_total", confirmOrder.getAmountTotal().doubleValue());
            if (confirmOrder.getPartnerShippingId() != null)
                orderOdoo.put("partner_shipping_id", confirmOrder.getPartnerShippingId().intValue());
            if (confirmOrder.getCreateDate() != null)
                orderOdoo.put("create_date", confirmOrder.getCreateDate());
            orderOdoo.put("channel", "tablet");
            if (confirmOrder.getNote() != null && confirmOrder.getNote().length() > 0)
                orderOdoo.put("note", confirmOrder.getNote());
            if (confirmOrder.getName() != null)
                orderOdoo.put("name", confirmOrder.getName());
            if (confirmOrder.getPartnerInvoiceId() != null)
                orderOdoo.put("partner_invoice_id", confirmOrder.getPartnerInvoiceId().intValue());
            if (confirmOrder.getState() != null)
                orderOdoo.put("state", confirmOrder.getState());
            if (confirmOrder.getDateOrder() != null)
                orderOdoo.put("date_order", confirmOrder.getDateOrder());
            if (confirmOrder.getRequestedDate() != null)
                orderOdoo.put("requested_date", confirmOrder.getRequestedDate());
        } else {
            // Estamos guardando la edición del pedido
            List<Object> linesOrderOdoo = new ArrayList<Object>();
            if (sendLines) {
                fillLineParamsWithOrderLines(linesOrderOdoo, confirmOrder.getLines(), true);
                fillRemoveOfOldLines(linesOrderOdoo, confirmOrder.getLines());
            }
            // DAVID - vamos a enviar el company_id ya que en el contexto no les llega a servidor
            orderOdoo.put("app_company_id", MidbanApplication.activeCompany);
            // DAVID - por petición del integrador campo "delay" con valor a 5 en cabecera
            // orderOdoo.put("delay", 5);
            orderOdoo.put("id", confirmOrder.getId().intValue());
            if (confirmOrder.getShopId() != null)
                orderOdoo.put("shop_id", confirmOrder.getShopId().intValue());
            orderOdoo.put("client_order_ref", confirmOrder.getClientOrderRef());
            orderOdoo.put("partner_id", confirmOrder.getPartnerId().intValue());
            orderOdoo.put("order_line", linesOrderOdoo.toArray());
            orderOdoo.put("warehouse_id", ConfigurationRepository.getInstance().getConfiguration().getWarehouseId().intValue());
            if (confirmOrder.getPricelistId() != null)
                orderOdoo.put("pricelist_id", confirmOrder.getPricelistId().intValue());
            if (confirmOrder.getAmountTax() != null)
                orderOdoo.put("amount_tax", confirmOrder.getAmountTax().doubleValue());
            if (confirmOrder.getMargin() != null)
                orderOdoo.put("margin", confirmOrder.getMargin().doubleValue());
            if (confirmOrder.getAmountUntaxed() != null)
                orderOdoo.put("amount_untaxed", confirmOrder.getAmountUntaxed().doubleValue());
            if (confirmOrder.getAmountTotal() != null)
                orderOdoo.put("amount_total", confirmOrder.getAmountTotal().doubleValue());
            if (confirmOrder.getPartnerShippingId() != null)
                orderOdoo.put("partner_shipping_id", confirmOrder.getPartnerShippingId().intValue());
            if (confirmOrder.getCreateDate() != null)
                orderOdoo.put("create_date", confirmOrder.getCreateDate());
            orderOdoo.put("channel", "tablet");
            if (confirmOrder.getNote() != null)
                orderOdoo.put("note", confirmOrder.getNote());
            if (confirmOrder.getName() != null)
                orderOdoo.put("name", confirmOrder.getName());
            if (confirmOrder.getPartnerInvoiceId() != null)
                orderOdoo.put("partner_invoice_id", confirmOrder.getPartnerInvoiceId().intValue());
            if (confirmOrder.getState() != null)
                orderOdoo.put("state", confirmOrder.getState());
            if (confirmOrder.getDateOrder() != null)
                orderOdoo.put("date_order", confirmOrder.getDateOrder());
            if (confirmOrder.getRequestedDate() != null)
                orderOdoo.put("requested_date", confirmOrder.getRequestedDate());
        }
        return orderOdoo;
    }

    private void confirmarPedido(final User user, final Order pedidoAConfirmar, final boolean sendLines) {
        new AsyncTask<User, Void, String>() {

            ProgressDialog progress;

            protected void onPreExecute() {
                progress = ProgressDialog.show(OrderNewDispositionFragment.this.getActivity(), "Sincronizando...",
                        "Enviando pedido", true);
            }

            @Override
            protected String doInBackground(User... user) {
                Integer id = null;
                HashMap<String, Object> orderOdoo = new HashMap<String, Object>();
                try {
                    orderOdoo = fillOrder(pedidoAConfirmar, sendLines);
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                    try {
                        appendLog("ERROR en la creación del pedido: " + e.getMessage() + ". Datos incoherentes en tablet para el pedido.");
                        appendLog("Tamaño de la lista de pendientes: " + OrderRepository.getUnsynchronizedOrders().size());
                    } catch (Exception ex) {
                        //do nothing
                    }
                    return "IRRECUPERABLE";
                } catch (ConfigurationException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                    try {
                        appendLog("ERROR en la creación del pedido: " + e.getMessage() + ". Datos incoherentes en tablet para el pedido.");
                        appendLog("Tamaño de la lista de pendientes: " + OrderRepository.getUnsynchronizedOrders().size());
                    } catch (Exception ex) {
                        //do nothing
                    }
                    return "IRRECUPERABLE";
                }
                try {
                    if (!orderOdoo.containsKey("id")) {
                        OpenERPCommand command = new OpenERPCommand(SessionFactory
                                .getInstance(user[0].getLogin(),
                                        user[0].getPasswd()).getSession());
                        try {
                            if (LoggerUtil.isDebugEnabled())
                                System.out.println(new JSONObject(orderOdoo));
                            appendLog("Enviando create_and_confirm (partner_id = " + orderOdoo.get("partner_id") + ") el pedido: " + new JSONObject(orderOdoo));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // lo cambio por esto
                        SessionFactory
                                .getInstance(user[0].getLogin(),
                                        user[0].getPasswd()).getSession().executeCommand("sale.order", "create_and_confirm", new Object[]{orderOdoo});

                       // command.callObjectFunction("sale.order", "create_and_confirm", new Object[]{orderOdoo});
                    } else {
                        //Estamos guardando la edición del pedido
                        id = (Integer) orderOdoo.get("id");
                        orderOdoo.remove("id");
                        OpenERPCommand command = new OpenERPCommand(SessionFactory
                                .getInstance(user[0].getLogin(),
                                        user[0].getPasswd()).getSession());
                        try {
                            if (LoggerUtil.isDebugEnabled())
                                System.out.println(new JSONObject(orderOdoo));
                            appendLog("Enviando easy_modification (partner_id = " + orderOdoo.get("partner_id") + ") el pedido: " + new JSONObject(orderOdoo));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        command.callObjectFunction("sale.order", "easy_modification", new Object[]{id, orderOdoo});
                        doRemoveOfOldLines(OrderRepository.getInstance().getById(id.longValue()).getLinesPersisted());
                    }
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                    if (id != null)
                        orderOdoo.put("id", id);
                    try {
                        appendLog("ERROR en la creación del pedido: " + e.getMessage() + ". Datos incoherentes en tablet para el pedido: " + new JSONObject(orderOdoo));
                        appendLog("Tamaño de la lista de pendientes: " + OrderRepository.getUnsynchronizedOrders().size());
                    } catch (Exception ex) {
                        //do nothing
                    }
                    return "IRRECUPERABLE";
                } catch (Exception e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                    if (id != null)
                        orderOdoo.put("id", id);
                    OrderRepository.getUnsynchronizedOrders().add(
                            orderOdoo);
                    if (LoggerUtil.isDebugEnabled())
                        System.out.println("Pending size: " + OrderRepository.getUnsynchronizedOrders().size());
                    try {
                        appendLog("ERROR en el envío: " + e.getMessage() + ". Para el pedido: " + new JSONObject(orderOdoo));
                        appendLog("Tamaño de la lista de pendientes: " + OrderRepository.getUnsynchronizedOrders().size());
                    } catch (Exception ex) {
                        //do nothing
                    }
                    return "PENDIENTE";
                }
                return "OK";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progress.dismiss();
                switch (result) {
                    case "IRRECUPERABLE":{
                        MessagesForUser.showMessage(getActivity(), R.string.error_irrecuperable_en_pedido,
                                Toast.LENGTH_LONG, Level.SEVERE);
                        break;
                    }
                    case "PENDIENTE": {
                        MessagesForUser.showMessage(getActivity(), R.string.error_recuperable_en_pedido,
                                Toast.LENGTH_LONG, Level.WARNING);
                        break;
                    }
                    case "OK": {
                        MessagesForUser.showMessage(getActivity(), R.string.saved,
                                Toast.LENGTH_LONG, Level.INFO);
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                if (!OrderSynchronizationService.anotherThreadSynchronizing) {
                                    try {
                                        OrderSynchronizationService.anotherThreadSynchronizing = true;
                                        try {
                                            OrderRepository.getInstance().getRemoteObjects(new Order(), user.getLogin(),
                                                    user.getPasswd(), false, false);
                                            OrderLineRepository.getInstance().getRemoteObjects(new OrderLine(), user.getLogin(),
                                                    user.getPasswd(), false, false);
                                        } catch (ConfigurationException e) {
                                            e.printStackTrace();
                                        }
                                    } finally {
                                        OrderSynchronizationService.anotherThreadSynchronizing = false;
                                    }
                                }
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    }
                }
                clearContext();
                getActivity().finish();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user);
    }

    private void doRemoveOfOldLines(List<OrderLine> lines) {
        for (OrderLine oldLine : oldLines) {
            boolean shouldBeRemoved = true;
            for (OrderLine line : lines) {
                if (line.getProductId().equals(oldLine.getProductId()))
                    shouldBeRemoved = false;
            }
            if (shouldBeRemoved) {
                try {
                    OrderLineRepository.getInstance().delete(oldLine.getId());
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fillRemoveOfOldLines(List<Object> dictionaries, List<OrderLine> lines) {
        for (OrderLine oldLine : oldLines) {
            boolean shouldBeRemoved = true;
            for (OrderLine line : lines) {
                if (line.getProductId().equals(oldLine.getProductId()))
                    shouldBeRemoved = false;
            }
            if (shouldBeRemoved) {
                Object[] dictionary = new Object[3];
                dictionary[0] = 2;
                dictionary[1] = oldLine.getId().intValue();
                dictionary[2] = 0;
                dictionaries.add(dictionary);
            }
        }
    }

    private void fillLineParamsWithOrderLines(List<Object> params, List<OrderLine> lines, boolean updating) throws ServiceException, ConfigurationException {
        for (int i = 0; i < lines.size(); i++) {
            Object[] dictionary = new Object[3];
            if (!updating || lines.get(i).getId() == null) {
                dictionary[0] = 0;
                dictionary[1] = 0;
            } else {
                if (lines.get(i).getId() != null) {
                    dictionary[0] = 1;
                    dictionary[1] = lines.get(i).getId().intValue();
                }
            }
            Map lineValues = new HashMap();
            lineValues.put("product_id", lines.get(i).getProductId().intValue());
            if (lines.get(i).getName() != null) {
                lineValues.put("description", lines.get(i).getName());
            }
            lineValues.put("product_uom_qty", lines.get(i).getProductUomQuantity().doubleValue());
            lineValues.put("product_uos_qty", lines.get(i).getProductUosQuantity().doubleValue());
            Product product = ProductRepository.getInstance().getById(lines.get(i).getProductId().longValue());
            if (product != null) {
                lineValues.put("product_uom", product.getUom().getId().intValue());
                lineValues.put("product_uos", product.getUos().getId().intValue());
                lineValues.put("name", product.getNameTemplate());
            }
            lineValues.put("price_unit", lines.get(i).getPriceUnit().doubleValue());
            Number taxId = product.getProductTemplate().getTaxesId();
            if (taxId == null) {
                MessagesForUser.showMessage(getView(), getResources().getString(R.string.error_al_anadir_producto), Toast.LENGTH_SHORT, Level.SEVERE);
                return;
            }
            lineValues.put("tax_id", new Object[]{new Object[]{6,0,new Number[]{taxId}}});
            lineValues.put("discount", lines.get(i).getDiscount().doubleValue());
            lineValues.put("discount_type", lines.get(i).getDiscountType());
            dictionary[2] = lineValues;
            params.add(dictionary);
        }
    }

    private void clearContext() {
        MidbanApplication
                .removeValueInContext(ContextAttributes.PARTNER_TO_ORDER);
        // restablecemos la tarifa actual al valor por defecto
        MidbanApplication.putValueInContext(ContextAttributes.ACTUAL_TARIFF, "");
        OrderRepository.clearCurrentOrder();
    }

    private boolean validateOrder() {
        boolean validated = true;
        StringBuilder validationMessages = new StringBuilder("");
        if (numberOfFooterViewOpened > 0) {
            validated = false;
            validationMessages.append(getResources().getString(R.string.cannot_confirm_while_calculating_prices));
            validationMessages.append("\n");
        }
        if (OrderRepository.getCurrentOrder().getLines() == null || OrderRepository.getCurrentOrder().getLines().size() == 0) {
            validated = false;
            validationMessages.append(getResources().getString(R.string.order_must_have_items));
            validationMessages.append("\n");
        }
        // no se quiere que sea obligatoria
        // https://bitbucket.org/noroestesoluciones/odoo-app/issues/103/correcciones-en-la-operativa-de-la-fecha
        /*
        if (deliveryDateView.getText() == null || deliveryDateView.getText().toString().length() == 0) {
            validated = false;
            validationMessages.append(getResources().getString(R.string.order_must_have_a_date));
            validationMessages.append("\n");
        }
        hasta aquí */
        if ((shopView.getText() == null) || ("".equals(shopView.getText().toString()))){
            validated = false;
            validationMessages.append(getResources().getString(R.string.order_must_have_associated_shop));
            validationMessages.append("\n");
        }
        if (validationMessages.length() > 0)
            MessagesForUser.showMessage(getView(), validationMessages.toString(), Toast.LENGTH_LONG, Level.SEVERE);
        return validated;
    }

//    @TextChanged(view = R.id.fragment_order_new_disposition_product_search_field)

    public class SearchTextChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onSearchTextChanged();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void onSearchTextChanged() {
        if (task != null)
            task.cancel(true);
        task = new SearchTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, productSearchField.getText().toString());
    }

    @Override
    public void onUnitChanged(OrderLine line) {
        if (!readOnlyMode) {
            ProductUom uom = new ProductUom();
            Product selectedProduct = line.getProduct();
            try {
                uom = getNextUomAvailable(selectedProduct, line);
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            }
            try {
                line.setPriceSubtotal(line.getProductUosQuantity().floatValue()
                        * line.getPriceUdv().floatValue()
                        - (line.getProductUosQuantity().floatValue()
                        * line.getPriceUdv().floatValue()
                        * line.getDiscount().floatValue() / 100F));
                line.setProductUos(uom.getId());
                reloadProducts = false;
                loadOnResume();
            } catch (Exception e) {
                e.printStackTrace();
                //do nothing
            }
        } else {
            MessagesForUser.showMessage(getView(), getResources().getString(R.string.units_cannot_be_modified_when_read_only_mode), Toast.LENGTH_LONG, Level.SEVERE);
        }
    }

    private ProductUom getNextUomAvailable(Product selectedProduct, OrderLine line) throws ServiceException, ConfigurationException {

        return ProductUomRepository.getInstance().getById(line.getProductUos().longValue());
    }

    class SearchTask extends AsyncTask<String, Map<String, List<Product>>, Map<String, List<Product>>> {

        Product productSearchScroll;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("PRE-SEARCH");
            footerView.setVisibility(View.VISIBLE);
            productSearchScroll = new Product();
            if (productSearchField.getText().length() > 1) {
                productSearchScroll.setNameTemplate(productSearchField.getText().toString());
                productSearchScroll.setCode(productSearchField.getText().toString());
                try {
                    productSearchScroll.setId(Long
                            .parseLong(productSearchField.getText().toString()));
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }
            System.out.println("PRE-SEARCH-END");
        }

        @Override
        protected Map<String, List<Product>> doInBackground(String... params) {
            System.out.println("DO-SEARCH");
            Map<String, List<Product>> result = new HashMap<String, List<Product>>();
            if (!isCancelled()) {
                Product productSearch = new Product();
                String productSearchF = params[0];
                if (productSearchF.length() > 1) {
                    productSearch.setNameTemplate(productSearchF);
                    productSearch.setCode(productSearchF);
                    try {
                        productSearch.setId(Long
                                .parseLong(productSearchF));
                    } catch (NumberFormatException e) {
                        // do nothing
                    }
                }
                List<Product> asyncFavourites = new ArrayList<Product>();
                List<Product> asyncAll = new ArrayList<Product>();
                if (favouriteProducts != null && favouriteProducts.size() > 0)
                    try {
                        for (Product product : favouriteProducts) {
                            if (satisfyCriteria(product, productSearch))
                                asyncFavourites.add(product);
                        }
                    } catch (Exception e) {
                        // En ocasiones favouriteProducts se pone a null después de entrar en el for
                        // Probablemente por alguna tarea asíncrona
                        e.printStackTrace();
                    }
                try {
                    asyncAll.addAll(ProductRepository.getInstance().getByExample(productSearch, Restriction.OR, false, 15, 0, true, false, _tarifaActual));
                } catch (ServiceException e) {
                    e.printStackTrace();
                }
                result.put("favoritos", asyncFavourites);
                result.put("todos", asyncAll);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Map<String, List<Product>> result) {
            super.onPostExecute(result);
            System.out.println("POST-SEARCH");
            currentFavouriteProducts.clear();
            currentAllProducts.clear();
            if (mostrarFavoritos) {
                currentFavouriteProducts.addAll(result.get("favoritos"));
            }
            currentAllProducts.addAll(result.get("todos"));
            footerView.setVisibility(View.GONE);
            if (mostrarFavoritos && adapterFavourite != null) {
                adapterFavourite.notifyDataSetChanged();
            }
            if (adapterAll != null) {
                adapterAll.notifyDataSetChanged();
            }
            allCatalogListView.setOnScrollListener(new InfiniteScrollListener(15) {
                @Override
                public void loadMore(final int page, final int totalItemsCount) {
                    new AsyncTask<Void, List<Product>, List<Product>>() {

                        @Override
                        protected List<Product> doInBackground(Void... params) {
                            try {
                                List<Product> a = ProductRepository.getInstance().getByExample(productSearchScroll, Restriction.OR, false, 15, (page - 1) * 15, true, false, _tarifaActual);
                                return a;
                            } catch (ServiceException e) {
                                e.printStackTrace();
                                return new ArrayList<Product>();
                            }
                        }

                        @Override
                        protected void onPostExecute(List<Product> products) {
                            super.onPostExecute(products);
                            currentAllProducts.addAll(products);
                            adapterAll.notifyDataSetChanged();
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            });
            System.out.println("POST-SEARCH-END");
        }
    }

    private boolean satisfyCriteria(Product product, Product productSearch) {
        try {
            return ((product != null && product.getNameTemplate() != null && productSearch != null
                    && productSearch.getNameTemplate() != null
                    && product.getNameTemplate().toUpperCase().contains(productSearch.getNameTemplate().toUpperCase()))
                    || (product != null && product.getCode() != null
                    && productSearch != null && productSearch.getCode() != null
                    && product.getCode().toUpperCase().contains(productSearch.getCode().toUpperCase()))
                    || (productSearch.getNameTemplate() == null && productSearch.getCode() == null)
                    || (productSearch.getNameTemplate().length() == 0 && productSearch.getCode().length() == 0));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void cancelAllAsyncs() {
        if (task != null)
            task.cancel(true);
    }

    public void clickNotes() {
        if (!readOnlyMode)
            new OneFieldEditionDialog(orderNotesView)
                    .openDialogForUniqueTextField(orderNotesView.getTag().toString(), "", null, null, false, true);
    }

    private void calculateAmounts() {
        double amountUntaxed = 0.0d;
        double amountTax = 0.0d;
        double margin = 0.0d;
        double marginPerc = 0.0d;
//        productSearchField.setText("");
        for (OrderLine line : OrderRepository.getCurrentOrder().getLines()) {
            if (line.getDiscount() != null) {
                float priceUdv = 0F;
                // DAVID - MODIFICACION, NO USAMOS LA UNIDAD DE VENTA SINO LA UNIDAD DEL ARTICULO
                if (line.getPriceUnit() != null) {
                    priceUdv = line.getPriceUdv().floatValue();
                }
                line.setPriceSubtotal((line.getProductUosQuantity().floatValue()
                        * priceUdv)
                        - ((line.getProductUosQuantity().floatValue()
                        * priceUdv
                        * line.getDiscount().floatValue()) / 100F));
            }
            amountUntaxed += line.getPriceSubtotal().doubleValue();
            Tax tax = null;
            try {
                if (line.getTaxesId() != null && line.getTaxesId().length > 0) {
                    for (Number taxId : line.getTaxesId()) {
                        if (taxId != null) {
                            tax = TaxRepository.getInstance().getById(taxId.longValue());
                            if (tax != null && tax.getAmount() != null)
                                amountTax += line.getPriceSubtotal().doubleValue()
                                        * tax.getAmount().doubleValue();

                        }
                    }
                }
                if (tax == null) {
                    if (line != null && line.getProduct() != null &&
                            line.getProduct().getProductTemplate() != null &&
                            line.getProduct().getProductTemplate().getTaxesId() != null)
                        tax = TaxRepository.getInstance().getById(
                                line.getProduct().getProductTemplate().getTaxesId()
                                        .longValue());
                    if (tax != null) {
                        tax.getType().equals("percent");
                        amountTax += line.getPriceSubtotal().doubleValue()
                                * tax.getAmount().doubleValue();
                    }
                }
            } catch (ConfigurationException e) {
            } catch (ServiceException e) {
            }
            if (line.getId() != null && line.getMargin() != null) {
                // Si tiene id usamos el valor de margen en lugar de calcularlo (no es un pedido nuevo)
                margin += line.getMargin().doubleValue();
            } else {
                // Si no tiene id se trata de un pedido nuevo y calculamos el margen
                margin += line.getPriceSubtotal().doubleValue() -
                        line.getProductUomQuantity().doubleValue()
                                * line.getProduct().getStandardPrice().doubleValue();
            }
        }
        if (amountUntaxed != 0) {
            marginPerc = 100 * margin / amountUntaxed;
        }
        OrderRepository.getCurrentOrder()
                .setAmountUntaxed(
                        new BigDecimal(amountUntaxed).setScale(2,
                                RoundingMode.HALF_UP));
        OrderRepository.getCurrentOrder().setAmountTax(
                new BigDecimal(amountTax).setScale(2, RoundingMode.HALF_UP));
        OrderRepository.getCurrentOrder().setAmountTotal(
                new BigDecimal(amountUntaxed + amountTax).setScale(2,
                        RoundingMode.HALF_UP));
        OrderRepository.getCurrentOrder().setMargin(
                new BigDecimal(margin).setScale(2, RoundingMode.HALF_UP));
        OrderRepository.getCurrentOrder().setMarginPerc(
                new BigDecimal(marginPerc).setScale(2, RoundingMode.HALF_UP));
    }

    public void onLineSelected(final OrderLine line) {
        if (!readOnlyMode) {
            final String items[] = {
                    getResources().getString(
                            R.string.fragment_order_last_sold_option),
                    getResources().getString(
                            R.string.fragment_order_view_product_card_option)};
            final AlertDialog.Builder dialog = new AlertDialog.Builder(
                    getActivity());
            dialog.setItems(items, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface d, int choice) {
                    if (choice == 0) {
                        MidbanApplication.putValueInContext(
                                ContextAttributes.PRODUCT_TO_LAST_SALES,
                                line.getProduct());
                        startActivityForResult(
                                getNextIntent(new Bundle(),
                                        OrderNewDispositionFragment.this.getView(),
                                        LastSalesActivity.class), 0);
                    } else if (choice == 1) {
                        line.getProduct().setDiscount1(line.getDiscount1());
                        line.getProduct().setDiscount2(line.getDiscount2());
                        line.getProduct().setDiscount3(line.getDiscount3());
                        MidbanApplication.putValueInContext(
                                ContextAttributes.PRODUCT_TO_DETAIL,
                                line.getProduct());
                        startActivityForResult(
                                getNextIntent(new Bundle(),
                                        OrderNewDispositionFragment.this.getView(),
                                        ProductCardActivity.class), 0);
                    }
                }
            });
            dialog.show();
        }
    }


    private String getDeliveryDate() {
        return DateUtil.toFormattedString(Calendar.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss");
    }

    private void calculateRiskLimit(final Partner partner) {
        try {
            Float totalDebt = partner.getDebit().floatValue();
            final Number creditLimit = partner.getCreditLimit();
            if (availableDebitOnline != null)
                totalDebt = availableDebitOnline;
            final Float availableCredit;
            if (creditLimit != null && totalDebt != null && OrderRepository.getCurrentOrder().getAmountTotal() != null)
                availableCredit = creditLimit.floatValue() - totalDebt - OrderRepository.getCurrentOrder()
                        .getAmountTotal().floatValue();
            else {
                availableCredit = 0F;
            }
            if (creditLimit != null && totalDebt != null) {
                if (availableCredit < 0) {
                    creditAvailableView.setTextColor(MidbanApplication.getContext().getResources().getColor(R.color.red));
                } else {
                    creditAvailableView.setTextColor(MidbanApplication.getContext().getResources().getColor(R.color.midban_text_color));
                }
                creditAvailableView.setText("Crédito disponible: " + new BigDecimal(availableCredit.floatValue()).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue() + " €");
            }
            if (availableDebitOnline == null) {
                new AsyncTask<Void, Void, Float>() {
                    @Override
                    protected Float doInBackground(Void... params) {
                        Long initDate = new Date().getTime();
                        Session openERPSession = null;
                        try {
                            User user = (User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER);
                            openERPSession = SessionFactory.getInstance(user.getLogin(), user.getPasswd()).getSession();
                        } catch (Exception e) {
                            // do nothing, openERPSession will be null
                        }
                        if (openERPSession != null) {
                            ObjectAdapter adapter;
                            try {
                                adapter = openERPSession.getObjectAdapter("res.partner");
                                FilterCollection filters = new FilterCollection();
                                filters.add("id", "=", partner.getId());
                                RowCollection entities;
                                String[] fieldsRemote = {"id", "total_debt"};
                                entities = adapter.searchAndReadObject(filters, fieldsRemote, null);
                                for (Row row : entities) {
                                    return ((Double) row.get("total_debt")).floatValue();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Float aFloat) {
                        super.onPostExecute(aFloat);
                        try {
                            if (aFloat != null) {
                                availableDebitOnline = aFloat;
                                Float disponible = creditLimit.floatValue() - aFloat;
                                if (isEditable())
                                    disponible = disponible - OrderRepository.getCurrentOrder()
                                            .getAmountTotal().floatValue();
                                if (disponible < 0) {
                                    creditAvailableView.setTextColor(MidbanApplication.getContext().getResources().getColor(R.color.red));
                                } else {
                                    creditAvailableView.setTextColor(MidbanApplication.getContext().getResources().getColor(R.color.midban_text_color));
                                }
                                creditAvailableView.setText("Crédito disponible: " + new BigDecimal(disponible.floatValue()).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue() + " €");
                                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                                creditAvailableView.startAnimation(fadeIn);
                                fadeIn.setDuration(1500);
                                fadeIn.setFillAfter(true);

                            }
                        } catch (Exception e) {
                            //do nothing
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //do nothing
        }
    }

    public class OrderProductCatalogScrollListener implements
            AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView arg0, int arg1) {

        }

        private int previousTotalItemCount = 0;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, final int totalItemCount) {
            if (totalItemCount > 0 && totalItemCount > previousTotalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if (lastInScreen >= totalItemCount - 15 && totalItemCount > lastInScreen) {
                    new AsyncTask<Integer, Void, Void>() {

                        Product productSearchScroll;

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            footerView.setVisibility(View.VISIBLE);
                            productSearchScroll = new Product();
                            if (productSearchField.getText().length() > 1) {
                                productSearchScroll.setNameTemplate(productSearchField.getText().toString());
                                productSearchScroll.setCode(productSearchField.getText().toString());
                                try {
                                    productSearchScroll.setId(Long
                                            .parseLong(productSearchField.getText().toString()));
                                } catch (NumberFormatException e) {
                                    // do nothing
                                }
                            }
                        }

                        @Override
                        protected Void doInBackground(Integer... params) {
                            if (previousTotalItemCount <= params[0]) {
                                previousTotalItemCount = params[0];
                                try {
                                    currentAllProducts.addAll(ProductRepository.getInstance().getByExample(productSearchScroll, Restriction.OR, false, 15, params[0], true, false, _tarifaActual));
                                } catch (ServiceException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            footerView.setVisibility(View.GONE);
                            adapterAll.notifyDataSetChanged();
                            indexListAllCatalog = allCatalogListView.getFirstVisiblePosition();
                            View v = allCatalogListView.getChildAt(0);
                            topListAllCatalog = (v == null) ? 0 : (v.getTop() - allCatalogListView.getPaddingTop());
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount);

                }
            }
        }
    }

    public abstract class InfiniteScrollListener implements AbsListView.OnScrollListener {
        private int bufferItemCount = 10;
        private int currentPage = 0;
        private int itemCount = 0;
        private boolean isLoading = true;

        public InfiniteScrollListener(int bufferItemCount) {
            this.bufferItemCount = bufferItemCount;
        }

        public abstract void loadMore(int page, int totalItemsCount);

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Do Nothing
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount < itemCount) {
                this.itemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.isLoading = true;
                }
            }

            if (isLoading && (totalItemCount > itemCount)) {
                isLoading = false;
                itemCount = totalItemCount;
                currentPage++;
            }

            if (!isLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + bufferItemCount)) {
                loadMore(currentPage + 1, totalItemCount);
                isLoading = true;
            }
        }
    }

    @Click(view = R.id.fragment_order_delivery_date_et)
    public void orderDeliveryDateClicked() {
        if (!readOnlyMode)
            DatePickerFragment.create(deliveryDateView).show(getFragmentManager(),
                    "datePicker");
    }

    public static class DatePickerFragment extends android.support.v4.app.DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        public static EditText editText;
        DatePicker dpResult;

        public DatePickerFragment() {
            super();
        }

        public static DatePickerFragment create(EditText edition) {
            DatePickerFragment result = new DatePickerFragment();
            editText = edition;
            return result;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            editText.setText(String.format("%02d", day) + "."
                    + String.format("%02d", (month + 1)) + "."
                    + String.valueOf(year));
            try {
                OrderRepository.getCurrentOrder().setRequestedDate(
                        DateUtil.toFormattedString(DateUtil.parseDate(
                                editText.getText().toString(),
                                "dd.MM.yyyy")));
            } catch (ParseException e) {
                //do nothing
            }
            this.dismiss();
        }
    }

    @Click(view = R.id.fragment_order_new_disposition_button_repeat)
    public void repeatOrder() {
        // De momento no vamos a permitir repetir pedidos
        // Este paso hay que analizarlo ya que por ejemplo los precios deberian actualizarse siempre
        // con los actuales (desconozco si lo hace) (14/08/2020) Pedro Gómez
        // MidbanApplication.putValueInContext(ContextAttributes.READ_ONLY_ORDER_MODE, Boolean.FALSE);
        // readOnlyMode = false;
        // Para que lo tome como nuevo pedido ponemos el ID a 0
        // OrderRepository.getCurrentOrder().setId(0L);
        this.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.home_item:
                startActivityForResult(
                        getNextIntent(new Bundle(), getView(),
                                PortadaActivity.class), 0);
                return true;
            case R.id.notes_item:
                clickNotes();
                return true;
            case R.id.info_item:
                String mensaje = "\n";
                mensaje += "Dirección entrega: \n" + partner.getContactAddress();
                String commercialRouteName = "";
                try {
                    CommercialRoute commercialRoute = CommercialRouteRepository.getInstance().getById(
                            partner.getCommercialRouteId().longValue());
                    if (commercialRoute != null) {
                        commercialRouteName = commercialRoute.getName();
                    } else {
                        commercialRouteName = "No definida";
                    }
                } catch (ConfigurationException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                } catch (ServiceException e) {
                    if (LoggerUtil.isDebugEnabled())
                        e.printStackTrace();
                }
                mensaje += "\n\n" + "Ruta comercial: \n" + commercialRouteName;
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Información")
                        .setMessage(mensaje)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // no hago nada
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void appendLog(String text) {
        text = new Date().toString() + "\n" + text;
        File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + "midban.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
