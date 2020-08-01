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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cafedered.cafedroidlitedao.exceptions.DatabaseException;
import com.cafedered.cafedroidlitedao.extractor.Restriction;
import com.cafedered.midban.dao.StockMoveDAO;
import com.cafedered.midban.entities.StockMove;
import com.cafedered.midban.utils.LoggerUtil;

public class StockMoveRepository extends
        BaseRepository<StockMove, StockMoveDAO> {

    private static StockMoveRepository instance = null;

    public static StockMoveRepository getInstance() {
        if (instance == null)
            instance = new StockMoveRepository();
        return instance;
    }

    private StockMoveRepository() {
        dao = StockMoveDAO.getInstance();
    }

    public List<StockMove> getByStockPickingOutId(Long id) {
        StockMove line = new StockMove();
        line.setPickingId(id);
        try {
            List<StockMove> result = dao.getByExample(line,
                    Restriction.AND, true, 0, 100000);
            return result;
        } catch (DatabaseException e) {
            if (LoggerUtil.isDebugEnabled())
                e.printStackTrace();
            return new ArrayList<StockMove>();
        }
    }
}
