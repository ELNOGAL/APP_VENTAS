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
package com.cafedered.midban.service.repositories;

import com.cafedered.cafedroidlitedao.exceptions.DatabaseException;
import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.conf.ContextAttributes;
import com.cafedered.midban.conf.MidbanApplication;
import com.cafedered.midban.dao.ProductDAO;
import com.cafedered.midban.entities.Partner;
import com.cafedered.midban.entities.PricelistPrices;
import com.cafedered.midban.entities.Product;
import com.cafedered.midban.entities.User;
import com.cafedered.midban.utils.exceptions.ConfigurationException;
import com.cafedered.midban.utils.exceptions.ServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductRepository extends BaseRepository<Product, ProductDAO> {

    private static ProductRepository instance = null;

    public static ProductRepository getInstance() {
        if (instance == null)
            instance = new ProductRepository();
        return instance;
    }

    private ProductRepository() {
        dao = ProductDAO.getInstance();
    }

    public BigDecimal getCalculatedPrice(Product instance, Partner partner, String tariff,
                                         String login, String passwd) {
        Long initTime = new Date().getTime();

        // si tengo tarifa me voy directamente a ella
        if ((instance != null) && (tariff != null) && (!"".equals(tariff))) {
            PricelistPrices pl = new PricelistPrices();
            pl.setPricelistId(Long.parseLong(tariff));
            pl.setProductId(instance.getId().longValue());
            try {
                List<PricelistPrices> list = PricelistPricesRepository.getInstance().getByExample(pl, Restriction.AND, true, 0, 1);
                if (list != null && list.size() == 1) {
                    return new BigDecimal(list.get(0).getPrice().doubleValue()).setScale(3, RoundingMode.HALF_UP);
                }
            } catch (ServiceException e1) {
                e1.printStackTrace();
            }
        }

        // sino voy a mirar en la tarifa del partner
        if ((instance != null) && (partner != null)) {
            PricelistPrices pl = new PricelistPrices();
            pl.setPricelistId(partner.getPricelistId());
            pl.setProductId(instance.getId().longValue());
            try {
                List<PricelistPrices> list = PricelistPricesRepository.getInstance().getByExample(pl, Restriction.AND, true, 0, 1);
                if (list != null && list.size() == 1) {
                    return new BigDecimal(list.get(0).getPrice().doubleValue()).setScale(3, RoundingMode.HALF_UP);
                }
            } catch (ServiceException e1) {
                e1.printStackTrace();
            }
        }

        // sino pues la de la empresa
        tariff = MidbanApplication.priceListIdActualCompany();
        if ((instance != null) && (tariff != null) && (!"".equals(tariff))) {
            PricelistPrices pl = new PricelistPrices();
            pl.setPricelistId(Long.parseLong(tariff));
            pl.setProductId(instance.getId().longValue());
            try {
                List<PricelistPrices> list = PricelistPricesRepository.getInstance().getByExample(pl, Restriction.AND, true, 0, 1);
                if (list != null && list.size() == 1) {
                    return new BigDecimal(list.get(0).getPrice().doubleValue()).setScale(3, RoundingMode.HALF_UP);
                }
            } catch (ServiceException e1) {
                e1.printStackTrace();
            }
        }

        System.out.println("Time bdd: " + (new Date().getTime() - initTime) + " ms.");

        BigDecimal result = null;

        System.out.println("Time BDD result: " + (new Date().getTime() - initTime) + " ms.");
        if (result != null) {
            // cambio a 3 decimales
            return result.setScale(3, RoundingMode.HALF_UP);
        }
        else
            return BigDecimal.ZERO;
    }

    public List<Product> getAllForPartner(Long id, Integer offset,
                                          Integer numElements, boolean ordenarPorCategoria, boolean ordenarAlfabeticamente) {
        User user = (User) MidbanApplication
                .getValueFromContext(ContextAttributes.LOGGED_USER);
            try {
                Partner partner = null;
                try {
                    partner = PartnerRepository.getInstance().getById(id);
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                }
                if (partner != null){
                    return getByExample(new Product(), Restriction.OR, false, numElements, offset, ordenarPorCategoria, ordenarAlfabeticamente, partner.getPricelistId().toString());
                }
                else{
                    return getByExample(new Product(), Restriction.OR, false, numElements, offset, ordenarPorCategoria, ordenarAlfabeticamente, "");
                }
            } catch (ServiceException e1) {
                e1.printStackTrace();
            }
//        }
        return new ArrayList<Product>();
    }

    @Override
    public List<Product> getByExample(Product entity, Restriction restriction,
                                      boolean exactMatching, Integer numElements, Integer offset)
            throws ServiceException {
        try {
            User user =
                    ((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER));
            List<Product> products = dao.getByExample(entity, restriction, exactMatching,
                    numElements, offset);
            List<Product> result = new ArrayList<Product>();
            for (Product product : products) {
                if (product.getSaleOk())
                    result.add(product);
            }
            return result;
        } catch (DatabaseException e) {
            throw new ServiceException("Cannot retrieve objects", e);
        }
    }

    @Override
    public List<Product> getAll(Integer numElements, Integer offset) throws ConfigurationException, ServiceException {
        return getAll(numElements, offset, true, false, "");
    }

    public List<Product> getAll(Integer numElements, Integer offset, boolean ordenarPorCategoria, boolean ordenarAlfabeticamente, String tarifaPorLaQueFiltrar)
            throws ConfigurationException, ServiceException {
        try {
            User user =
                ((User) MidbanApplication.getValueFromContext(ContextAttributes.LOGGED_USER));
            Product entity = new Product();
            entity.setSaleOk(true);
            entity.setActive(true);
            return dao.getByExample(entity, Restriction.OR, true,
                    numElements, offset, ordenarPorCategoria, ordenarAlfabeticamente, tarifaPorLaQueFiltrar);
        } catch (DatabaseException e) {
            throw new ServiceException("Cannot retrieve all objects", e);
        }
    }

    public List<Product> getByExample(Product productSearch, Restriction restriction, boolean exactMatching, int offset, int limit, boolean ordenarPorCategoria, boolean ordenarAlfabeticamente, String tarifaPorLaQueFiltrar) throws ServiceException {
        try {
            return dao.getByExample(productSearch, restriction, exactMatching, limit, offset, ordenarPorCategoria, ordenarAlfabeticamente, tarifaPorLaQueFiltrar);
        } catch (DatabaseException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<Integer> getDiferentProductTmplIds() {
        return dao.getDiferentProductTmplIds();
    }

}
