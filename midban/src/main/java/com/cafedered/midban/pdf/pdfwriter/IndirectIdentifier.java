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
//
//  Android PDF Writer
//  http://coderesearchlabs.com/androidpdfwriter
//
//  by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package com.cafedered.midban.pdf.pdfwriter;

public class IndirectIdentifier extends Base {

    private int mNumber;
    private int mGeneration;

    public IndirectIdentifier() {
        clear();
    }

    public void setNumber(int Number) {
        this.mNumber = Number;
    }

    public int getNumber() {
        return this.mNumber;
    }

    public void setGeneration(int Generation) {
        this.mGeneration = Generation;
    }

    public int getGeneration() {
        return this.mGeneration;
    }

    @Override
    public void clear() {
        this.mNumber = 0;
        this.mGeneration = 0;
    }

    @Override
    public String toPDFString() {
        return Integer.toString(this.mNumber) + " " + Integer.toString(this.mGeneration);
    }
}
