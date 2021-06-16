package teamwork.agents.wrappers;

import java.util.Arrays;
import java.util.List;

public class GodRegionWrapper extends GodWrapper {
    private GodWrapper god;
    private List<RegionWrapper> regions;
    public GodRegionWrapper() {
    }
    public GodRegionWrapper(GodWrapper gd, RegionWrapper[] regions) {
        this.god = gd;
        if (regions != null) {
            this.regions = Arrays.asList(regions.clone());
        }
    }

    public GodWrapper getGod() {
        return god;
    }
    public List<RegionWrapper> getRegions() {
        return regions;
    }

}
