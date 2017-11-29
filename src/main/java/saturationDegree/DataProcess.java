/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
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
package saturationDegree;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import java.util.Set;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;


import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;


@Description("Calculate evapotraspiration based on the Priestley Taylor model")
@Author(name = "Marialaura Bancheri")


public class DataProcess extends JGTModel {


	@Description("")
	@In
	@Out
	public   HashMap<Integer, double[]> inHMStorage;	

	@Description("")
	@In
	public Map<Integer, double[]> inHMStorageFromAboveVert1;

	@Description("")
	@In
	public Map<Integer, double[]> inHMStorageFromAboveVert2;

	@Description("")
	@In
	public int ID1;

	@Description("")
	@In
	public int ID2;





	@Execute
	public void process() throws Exception {



		Iterator<Map.Entry<Integer, double[]>> iter1 = inHMStorageFromAboveVert1.entrySet().iterator();
		


		while(iter1.hasNext()) {

			Map.Entry<Integer, double[]> e1 = iter1.next();
			Integer key1 = e1.getKey();
			double[] val1 = e1.getValue();

			/**Input data reading*/
			if( inHMStorageFromAboveVert2!=null){
				Iterator<Map.Entry<Integer, double[]>> iter2 = inHMStorageFromAboveVert2.entrySet().iterator();
				Map.Entry<Integer, double[]> e2 = iter2.next();
				Integer key2 = e2.getKey();
				double[] val2 = e2.getValue();
				inHMStorage.put(key2, new double[]{ val2[0]});
			}

			inHMStorage.put(key1, new double[]{ val1[0]});

		}


	}

}