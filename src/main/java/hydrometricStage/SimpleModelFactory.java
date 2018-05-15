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
package hydrometricStage;

// TODO: Auto-generated Javadoc
/**
 * A simple design factory for creating Model objects.
 */
public class SimpleModelFactory {


	public static Model createModel(String type,double discharge, double a, double b, double c, 
			double d,double H0){
		Model model=null;
		
		    /**Flow1: Q=a*(h+H0)^b;*/
		if (type.equals("FRC_Qh")){
			model=new Flow_1(discharge,a,b,H0);
			
			/**Flow2: Q=(a*(h+H0)^b)*(c*(h+H0)+d)*/
		}else if (type.equals("FRC_VA")){
			model=new Flow_2(discharge,a,b,c,d,H0);

			/**Flow3: no FRC is available*/
		}else if (type.equals("NO_FRC")){
			model=new Flow_3();

		}

		return model;

	}

}
