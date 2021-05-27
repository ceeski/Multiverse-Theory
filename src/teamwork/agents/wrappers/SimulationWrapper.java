package teamwork.agents.wrappers;

import java.util.List;

public class SimulationWrapper {
    private TimeWrapper time;
    private List<GodWrapper> gods;
    private List<RegionWrapper> regions;

    public SimulationWrapper() {
    }

    public SimulationWrapper(TimeWrapper time, List<GodWrapper> gods, List<RegionWrapper> regions) {
        this.time = time;
        this.gods = gods;
        this.regions = regions;
    }

    public TimeWrapper getTime() {
        return time;
    }

    public void setTime(TimeWrapper time) {
        this.time = time;
    }

    public List<GodWrapper> getGods() {
        return gods;
    }

    public void setGods(List<GodWrapper> gods) {
        this.gods = gods;
    }

    public List<RegionWrapper> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionWrapper> regions) {
        this.regions = regions;
    }
}
