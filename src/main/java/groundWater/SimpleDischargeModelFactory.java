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

package groundWater;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating SimpleDischargeModel objects.
 */
public class SimpleDischargeModelFactory {

	/**
	 * Creates a new SimpleDischargeModel object.
	 *
	 * @param type is the model name
	 * @param a is the coefficient of the non linear reservoir model 
	 * @param S_i is the actual storage
	 * @param b is the exponent of the non linear reservoir model 
	 * @return the discharge model
	 */
	public static DischargeModel createModel(String type,double a, double S_i, double b){
		DischargeModel model=null;
		if (type.equals("NonLinearReservoir")){
			model=new NonLinearReservoir(a,S_i,b);
		}else if (type.equals("Clapp-H")){
			model=new Clapp(a,S_i,b);
		}
			
		return model;
		
	}
}
