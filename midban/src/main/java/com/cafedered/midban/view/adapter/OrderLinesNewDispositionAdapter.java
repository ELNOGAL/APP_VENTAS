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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cafedered.midban.R;
import com.cafedered.midban.entities.OrderLine;
import com.cafedered.midban.service.repositories.OrderRepository;
import com.cafedered.midban.service.repositories.ProductUomRepository;
import com.cafedered.midban.utils.MessagesForUser;
import com.cafedered.midban.view.fragments.OrderNewDispositionFragment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Level;

public class OrderLinesNewDispositionAdapter extends BaseAdapter {

    private List<OrderLine> lines;
    private static LayoutInflater inflater = null;
    private OrderNewDispositionFragment fragment;

    public OrderLinesNewDispositionAdapter(Context context, List<OrderLine> lines, OrderNewDispositionFragment fragment) {
        this.lines = lines;
        this.fragment = fragment;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return lines.size();
    }

    public Object getItem(int position) {
        return lines.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public ImageView image;
        public TextView code;
        public TextView name;
        public TextView quantityUom;
        public TextView unitUom;
        public TextView quantityUos;
        public TextView unitUos;
        public TextView price;
        public TextView priceNet;
        public TextView total;
        public TextView discount;
        public TextView discountType;
        public ImageView deleteIcon;
    }

    public View getView(int position, final View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.order_line_list_item_new_disposition, null);
            holder = new ViewHolder();
            holder.image = (ImageView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_product_image);
            holder.name = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_product_name);
            holder.code = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_product_code);
            holder.quantityUom = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_quantity_uom);
            holder.unitUom = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_unit_uom);
            holder.quantityUos = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_quantity_uos);
            holder.unitUos = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_unit_uos);
            holder.price = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_price);
            holder.priceNet = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_price_net);
            holder.discountType = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_discount_type);
            holder.total = (TextView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_total);
            holder.deleteIcon = (ImageView) vi
                    .findViewById(R.id.order_line_list_item_new_disposition_delete_icon);
            holder.discount = (TextView) vi.findViewById(R.id.order_line_list_item_new_disposition_discount);
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();
        final OrderLine line = lines.get(position);
        holder.name.setText(line.getProduct().getNameTemplate());
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onLineSelected(line);
            }
        });
        holder.code.setText(line.getProduct().getCode());
//        if (!ImageCache.getInstance().exists(
//                line.getProduct().getClass().getName()
//                        + line.getProduct().getId()))
//            ImageCache.getInstance().putInCache(
//                    line.getProduct().getClass().getName()
//                            + line.getProduct().getId(),
//                    ImageUtil.byteArrayToBitmap(line.getProduct()
//                            .getImageMedium()));
//        holder.image.setImageBitmap(ImageCache.getInstance().getFromCache(
//                line.getProduct().getClass().getName()
//                        + line.getProduct().getId()));
        holder.image.setVisibility(View.GONE);
        try {
            if (line.getProductUosQuantity() != null) {
                String quantityUos = line.getProductUosQuantity().toString();
                if (quantityUos != null && quantityUos.endsWith(".00"))
                    quantityUos = quantityUos.replace(".00", "");
                if (quantityUos != null && quantityUos.endsWith(".0"))
                    quantityUos = quantityUos.replace(".0", "");
                holder.quantityUos.setText(quantityUos);
            }
            if (line.getProductUomQuantity() != null) {
                String quantityUom = line.getProductUomQuantity().toString();
                if (quantityUom != null && quantityUom.endsWith(".00"))
                    quantityUom = quantityUom.replace(".00", "");
                if (quantityUom != null && quantityUom.endsWith(".0"))
                    quantityUom = quantityUom.replace(".0", "");
                holder.quantityUom.setText(quantityUom);
            }
            if (line.getProductUos() != null)
                holder.unitUos.setText(ProductUomRepository.getInstance().getById(line.getProductUos().longValue()).getName());
            if (line.getProductUom() != null)
                holder.unitUom.setText(ProductUomRepository.getInstance().getById(line.getProductUom().longValue()).getName());
        } catch (Exception e) {
            //do nothing
        }
        if (line.getDiscount() != null) {
            holder.discount.setText("" + line.getDiscount());
            if (line.getDiscountType() != null) {
                if (line.getDiscountType() == "0" && line.getDiscount().floatValue() > 0) {
                    holder.discountType.setText("[D]");
                } else if (line.getDiscountType() == "-1") {
                    holder.discountType.setText("[P]");
                } else {
                    holder.discountType.setText("[" + line.getDiscountType() + "]");
                }
            }
            if (line.getDiscount() != null) {
                String discount = line.getDiscount().toString();
                if (discount != null && discount.endsWith(".00"))
                    discount = discount.replace(".00", "");
                if (discount != null && discount.endsWith(".0"))
                    discount = discount.replace(".0", "");
                holder.discount.setText(discount);
            }
        }
        if (line.getPriceUnit() != null && line.getPriceUnit().floatValue() != 0.0000000001) {
            // DAVID - CAMBIÉ getPriceUdv por getPriceUnit
            // cambio a 3 decimales
            holder.price.setText(new BigDecimal(line.getPriceUnit()
                    .doubleValue()).setScale(3, RoundingMode.HALF_UP) + "");
            holder.priceNet.setText(
                    new BigDecimal(
                            line.getPriceUnit().floatValue()
                                - line.getPriceUnit().floatValue()
                                * line.getDiscount().floatValue() / 100F)
                            .setScale(3, RoundingMode.HALF_UP) + "");
        } else {
            holder.price.setText("Cargando...");
            holder.priceNet.setText("Cargando...");
        }
        if (line.getPriceSubtotal() != null)
            holder.total.setText(new BigDecimal(line.getPriceSubtotal()
                    .doubleValue()).setScale(2, RoundingMode.HALF_UP) + "");
        boolean showDeleteIcon = fragment.isEditable();
        if (showDeleteIcon) {
            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OrderRepository.getCurrentOrder().getLines().remove(line);
                    notifyDataSetChanged();
                    fragment.loadOnResume();
                }
            });
        } else {
            holder.deleteIcon.setVisibility(View.INVISIBLE);
        }

        if (fragment != null && showDeleteIcon) {
            View.OnClickListener dialogDiscount = new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                    alert.setTitle("DESCUENTO:");
                    String mensaje =
                              "Tipo 1-> " + line.getDiscount1().toString() + "%, "
                            + "Tipo 2-> " + line.getDiscount2().toString() + "%, "
                            + "Tipo 3-> " + line.getDiscount3().toString() + "%";
                    alert.setMessage(mensaje);
                    final EditText input = new EditText(v.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setText(holder.discount.getText());
                    input.setImeOptions(EditorInfo.IME_ACTION_NONE);
                    input.setGravity(Gravity.CENTER);
                    input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                    input.selectAll();
                    alert.setView(input);
                    alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            try {
                                float descuento = Float.parseFloat(value);
                                String tipoDescuento = "0";
                                if (descuento < 0 || descuento > line.getProduct().getMaxDiscount())
                                    throw new Exception("Descuento incorrecto");
                                if (descuento == 3) {
                                    tipoDescuento = Integer.toString((int) descuento);
                                    descuento = line.getDiscount3().floatValue();
                                    if (descuento == 0.0) { // Si el descuento es 0 bajamos un nivel
                                        descuento = 2;
                                    }
                                }
                                if (descuento == 2) {
                                    tipoDescuento = Integer.toString((int) descuento);
                                    descuento = line.getDiscount2().floatValue();
                                    if (descuento == 0.0) { // Si el descuento es 0 bajamos un nivel
                                        descuento = 1;
                                    }
                                }
                                if (descuento == 1) {
                                    tipoDescuento = Integer.toString((int) descuento);
                                    descuento = line.getDiscount1().floatValue();
                                    if (descuento == 0.0) { // Si el descuento es 0 bajamos un nivel
                                        descuento = 0;
                                    }
                                }
                                if (descuento == 0) {
                                    tipoDescuento = Integer.toString((int) descuento);
                                    descuento = 0;
                                }
                                if (line.getDiscountType() != "-1") { // -1 significa que se ha modificado el precio
                                    line.setDiscountType(tipoDescuento);
                                }
                                line.setDiscount(descuento);
                                fragment.loadOnResume();
                            } catch (Exception e) {
                                MessagesForUser.showMessage(OrderLinesNewDispositionAdapter.this.fragment.getView(), "Descuento ha de ser un valor entre 0.0 y " + line.getProduct().getMaxDiscount().toString(), 3000, Level.SEVERE);
                            }
                        }
                    });
                    alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            };
            View.OnClickListener dialogQuantityUom = new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                    alert.setTitle("CANTIDAD:");
                    alert.setMessage(holder.unitUom.getText());
                    final EditText input = new EditText(v.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setText(holder.quantityUom.getText());
                    input.setImeOptions(EditorInfo.IME_ACTION_NONE);
                    input.setGravity(Gravity.CENTER);
                    input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                    input.selectAll();
                    alert.setView(input);
                    alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            try {
                                Float qty = Float.parseFloat(value);
                                if (qty < 1) {
                                    qty = 1F;
                                }
                                line.setProductUomQuantity(qty);
                            } catch (Exception e) {
                                dialog.cancel();
                            }
                            if (line.getProductUosQuantity() != null) {
                                String quantityUos = line.getProductUosQuantity().toString();
                                if (quantityUos != null && quantityUos.endsWith(".00"))
                                    quantityUos = quantityUos.replace(".00", "");
                                if (quantityUos != null && quantityUos.endsWith(".0"))
                                    quantityUos = quantityUos.replace(".0", "");
                                holder.quantityUos.setText(quantityUos);
                            }
                            line.setPriceSubtotal(line.getProductUomQuantity().floatValue()
                                    * line.getPriceUnit().floatValue()
                                    - (line.getProductUomQuantity().floatValue()
                                    * line.getPriceUnit().floatValue()
                                    * line.getDiscount().floatValue() / 100F));
                            if (line.getPriceSubtotal() != null)
                                holder.total.setText(new BigDecimal(line.getPriceSubtotal()
                                        .doubleValue()).setScale(2, RoundingMode.HALF_UP) + "");
                            fragment.loadOnResume();
                        }
                    });
                    alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            };
            View.OnClickListener dialogQuantityUos = new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                    alert.setTitle("CANTIDAD:");
                    alert.setMessage(holder.unitUos.getText());
                    final EditText input = new EditText(v.getContext());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    input.setText(holder.quantityUos.getText());
                    input.setImeOptions(EditorInfo.IME_ACTION_NONE);
                    input.setGravity(Gravity.CENTER);
                    input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
                    input.selectAll();
                    alert.setView(input);
                    alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            try {
                                Float qty = Float.parseFloat(value);
                                if (qty < 1) {
                                    qty = 1F;
                                }
                                line.setProductUosQuantity(qty);
                            } catch (Exception e) {
                                dialog.cancel();
                            }
                            if (line.getProductUomQuantity() != null) {
                                String quantityUom = line.getProductUomQuantity().toString();
                                if (quantityUom != null && quantityUom.endsWith(".00"))
                                    quantityUom = quantityUom.replace(".00", "");
                                if (quantityUom != null && quantityUom.endsWith(".0"))
                                    quantityUom = quantityUom.replace(".0", "");
                                holder.quantityUom.setText(quantityUom);
                            }
                            line.setPriceSubtotal(line.getProductUomQuantity().floatValue()
                                    * line.getPriceUnit().floatValue()
                                    - (line.getProductUomQuantity().floatValue()
                                    * line.getPriceUnit().floatValue()
                                    * line.getDiscount().floatValue() / 100F));
                            if (line.getPriceSubtotal() != null)
                                holder.total.setText(new BigDecimal(line.getPriceSubtotal()
                                        .doubleValue()).setScale(2, RoundingMode.HALF_UP) + "");
                            fragment.loadOnResume();
                        }
                    });
                    alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            };
            View.OnClickListener dialogMargin = new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    double margin = 0.0d;
                    double marginPerc = 0.0d;
                    AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                    alert.setTitle("Información:");
                    if (line.getId() != null && line.getMargin() != null) {
                        // Si tiene id usamos el valor de margen en lugar de calcularlo (no es un pedido nuevo)
                        margin += line.getMargin().doubleValue();
                    } else {
                        // Si no tiene id se trata de un pedido nuevo y calculamos el margen
                        margin += line.getPriceSubtotal().doubleValue() -
                                line.getProductUomQuantity().doubleValue()
                                        * line.getProduct().getStandardPrice().doubleValue();
                    }
                    if (line.getPriceSubtotal().doubleValue() != 0) {
                        marginPerc = 100 * margin / line.getPriceSubtotal().doubleValue();
                    }
                    String mensaje =
                            "Importe linea: " + new BigDecimal(line.getPriceSubtotal().doubleValue()).setScale(2, RoundingMode.HALF_UP) + " €" + "\n" +
                            "Margen: " + new BigDecimal(margin).setScale(2, RoundingMode.HALF_UP) + " €"
                            + " (" + new BigDecimal(marginPerc).setScale(2, RoundingMode.HALF_UP) + "%)";
                    alert.setMessage(mensaje);
                    alert.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            };
            holder.discount.setOnClickListener(dialogDiscount);
            holder.unitUom.setOnClickListener(dialogQuantityUom);
            holder.unitUos.setOnClickListener(dialogQuantityUos);
            holder.quantityUom.setOnClickListener(dialogQuantityUom);
            holder.quantityUos.setOnClickListener(dialogQuantityUos);
            holder.total.setOnClickListener(dialogMargin);
            }
        return vi;
    }

    public interface OrderLineUnitChangedListener {
        public void onUnitChanged(OrderLine line);
    }

    public interface OrderLineSelected {
        public void onLineSelected(OrderLine line);
    }

}