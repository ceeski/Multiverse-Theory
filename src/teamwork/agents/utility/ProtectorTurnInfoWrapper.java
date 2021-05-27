package teamwork.agents.utility;

import teamwork.agents.jsonwrappers.RegionWrapper;

import java.util.List;

/**
 * Wrapper for information sent to protector god, not only about regions but also about actions that already happened
 */
public class ProtectorTurnInfoWrapper {
    private List<RegionWrapper> regions;
    private List<GodInfluenceRegionAction> previousActions;

    public ProtectorTurnInfoWrapper() {
    }

    public ProtectorTurnInfoWrapper(List<RegionWrapper> regions, List<GodInfluenceRegionAction> previousActions) {
        this.regions = regions;
        this.previousActions = previousActions;
    }

    public List<RegionWrapper> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionWrapper> regions) {
        this.regions = regions;
    }

    public List<GodInfluenceRegionAction> getPreviousActions() {
        return previousActions;
    }

    public void setPreviousActions(List<GodInfluenceRegionAction> previousActions) {
        this.previousActions = previousActions;
    }
}
