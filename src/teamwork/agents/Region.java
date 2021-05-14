package teamwork.agents;

import jade.core.Agent;
import teamwork.agents.jsonwrappers.RegionWrapper;

public class Region extends Agent {

    @Override
    protected void setup() {
        RegionWrapper initialSettings = (RegionWrapper)getArguments()[0];

    }

}
