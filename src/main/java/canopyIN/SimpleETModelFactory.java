/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package canopyIN;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating SimpleETModel objects.
 */
public class SimpleETModelFactory {

	/**
	 * Creates a new SimpleETModel object.
	 *
	 * @param type is the model name
	 * @param S_i is the storage
	 * @param s_max is the maximum value of the storage
	 * @param k is the parameter of the SCF formulation 
	 * @param LAI is the leaf area index
	 * @return the ET model
	 */
	public static ETModel createModel(String type,double S_i, double s_max, double k, double LAI){
		ETModel model=null;
		if (type.equals("AET")){
			model=new AETmodel(S_i,s_max);
		}else if (type.equals("LAI")){
			model=new LAImodel(S_i,s_max,k,LAI);
		}
			
		return model;
		
	}
}
