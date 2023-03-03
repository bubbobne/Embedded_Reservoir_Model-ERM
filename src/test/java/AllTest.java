import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import it.geoframe.blogspot.canopy.TestCanopy;
import it.geoframe.blogspot.groundwater.TestGroundWater;
import it.geoframe.blogspot.rootzone.TestRootZone;
import it.geoframe.blogspot.simplebucket.TestRunoff;

@RunWith(Suite.class)
@Suite.SuiteClasses({ TestRunoff.class, TestCanopy.class, TestRootZone.class, TestGroundWater.class })
public class AllTest {
}
