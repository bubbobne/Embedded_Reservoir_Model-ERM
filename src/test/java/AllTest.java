import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import it.geoframe.blogspot.canopy.TestCanopy;
import it.geoframe.blogspot.groundwater.TestGroundWater;
import it.geoframe.blogspot.rootzone.TestRootZone;
import it.geoframe.blogspot.runkekutta.TestRK;
import it.geoframe.blogspot.simplebucket.TestRunoff;

/**
 * 
 * Run all packages test:
 * <ul>
 * <li>Canopy</li>
 * <li>Root zone</li>
 * <li>Runoff</li>
 * <li>Ground water</li>
 * </ul>
 * 
 * @author Daniele Andreis
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestRK.class,  TestRunoff.class, TestCanopy.class, TestRootZone.class, TestGroundWater.class })
public class AllTest {
}
