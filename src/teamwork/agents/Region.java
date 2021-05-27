package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import teamwork.agents.jsonwrappers.RegionWrapper;
import teamwork.agents.utility.GodInfluenceRegionAction;

import java.util.List;

public class Region extends Agent {
    private RegionWrapper settings;

    @Override
    protected void setup() {
        settings = (RegionWrapper)getArguments()[0];
        Common.RegisterAgentInDf(this);
        addBehaviour(processMessage);
    }

    /**
     * Recalculates resources using actions done by gods
     */
    private void recalculateResourcesFromGodActions(GodInfluenceRegionAction[] actions) {
        for (GodInfluenceRegionAction action : actions) {
            for (int j = 0; j < action.getElement().length; j++) {
                int value;
                switch (action.getElement()[j]) {
                    case AIR:
                        value = settings.getAirResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setAirResource(value);
                        break;
                    case FIRE:
                        value = settings.getHeatResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setHeatResource(value);
                        break;
                    case LOVE:
                        value = settings.getLoveResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setLoveResource(value);
                        break;
                    case EARTH:
                        value = settings.getEarthResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setEarthResource(value);
                        break;
                    case LIGHT:
                        value = settings.getLightResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setLightResource(value);
                        break;
                    case WATER:
                        value = settings.getWaterResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setWaterResource(value);
                        break;
                    case DARKNESS:
                        value = settings.getDarknessResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setDarknessResource(value);
                        break;
                    case AMUSEMENT:
                        value = settings.getAmusementResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setAmusementResource(value);
                        break;
                    case KNOWLEDGE:
                        value = settings.getKnowledgeResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setKnowledgeResource(value);
                        break;
                    case RESTRAINT:
                        value = settings.getRestraintResource();
                        value += action.getValue()[j];
                        value = Common.clamp(value, 0, 1000);
                        settings.setRestraintResource(value);
                        break;
                }
            }
        }
    }

    /**
     * Takes a value and uses formula to change it into population multiplier
     */
    private double getPopulationMultiplierFromValue(int value) {
        double diff = Math.abs(500 - (double)value);
        if(diff <= 200)
            return 1.1 - diff * 0.0005;
        return 1.0 - (diff - 200) * 0.003;
    }

    /**
     * Recalculates population based on actual values in settings
     */
    private void recalculateNewPopulation() {
        double multiplier = 1.0;

        multiplier *= getPopulationMultiplierFromValue(settings.getAirResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getRestraintResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getKnowledgeResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getAmusementResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getDarknessResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getWaterResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getLightResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getEarthResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getHeatResource());
        multiplier *= getPopulationMultiplierFromValue(settings.getLoveResource());

        int newPopulation = (int)((double)settings.getPopulation() *  multiplier);
        settings.setPopulation(newPopulation);
    }

    /**
     * Calculates decrease of resources caused by population size and decreases resources
     */
    private void influenceResourcesWithPopulation() {
        int decrease = (int)(350.0 - 350.0 * Math.exp((-(double)settings.getPopulation())/10000.0));

        settings.setRestraintResource(Common.clamp((settings.getRestraintResource() - decrease), 0, 1000));
        settings.setKnowledgeResource(Common.clamp((settings.getKnowledgeResource() - decrease), 0, 1000));
        settings.setAirResource(Common.clamp((settings.getAirResource() - decrease), 0, 1000));
        settings.setAmusementResource(Common.clamp((settings.getAmusementResource() - decrease), 0, 1000));
        settings.setDarknessResource(Common.clamp((settings.getDarknessResource() - decrease), 0, 1000));
        settings.setWaterResource(Common.clamp((settings.getWaterResource() - decrease), 0, 1000));
        settings.setLightResource(Common.clamp((settings.getLightResource() - decrease), 0, 1000));
        settings.setEarthResource(Common.clamp((settings.getEarthResource() - decrease), 0, 1000));
        settings.setHeatResource(Common.clamp((settings.getHeatResource() - decrease), 0, 1000));
        settings.setLoveResource(Common.clamp((settings.getLoveResource() - decrease), 0, 1000));
    }

    /**
     * Recalculates state of the region based on message with actions and responds to the Time
     */
    private void recalculateRegion(ACLMessage message) {
        Gson _gson = new GsonBuilder().create();
        GodInfluenceRegionAction[] actions = _gson.fromJson(message.getContent(), GodInfluenceRegionAction[].class);

        //Step 1: Influence region by gods
        recalculateResourcesFromGodActions(actions);

        //Step 2: Recalculate population
        recalculateNewPopulation();

        //Step 3: Recalculate resource decrease caused by population
        influenceResourcesWithPopulation();

        ACLMessage response = message.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology("Recalculated");
        send(response);
    }

    CyclicBehaviour processMessage = new CyclicBehaviour(this) {
        @Override
        public void action() {
            ACLMessage msg = receive();

            if(msg != null) {
                switch(msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        if(msg.getOntology().equals("Initial Information"))
                            Common.ResponseWithInformationAbout(getAgent(), settings, msg);
                        break;
                    case ACLMessage.INFORM:
                        if(msg.getOntology().equals("Recalculate"))
                            recalculateRegion(msg);
                        break;
                    default:
                        break;
                }
            }

            block();
        }
    };

}
