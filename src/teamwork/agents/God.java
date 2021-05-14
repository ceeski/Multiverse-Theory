package teamwork.agents;

import jade.core.Agent;
import teamwork.agents.jsonwrappers.GodWrapper;

public class God extends Agent {

    @Override
    protected void setup() {
        GodWrapper initialSettings = (GodWrapper)getArguments()[0];

    }

}
