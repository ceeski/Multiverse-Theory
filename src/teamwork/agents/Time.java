package teamwork.agents;

import jade.core.Agent;
import teamwork.agents.jsonwrappers.TimeWrapper;

public class Time extends Agent {

    @Override
    protected void setup() {
        TimeWrapper initialSettings = (TimeWrapper)getArguments()[0];
    }

}
