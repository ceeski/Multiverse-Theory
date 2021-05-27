package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import teamwork.agents.enums.ElementType;
import teamwork.agents.wrappers.RegionWrapper;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.utility.Common;

public class Region extends Agent {
    private RegionWrapper settings;

    @Override
    protected void setup() {
        settings = (RegionWrapper)getArguments()[0];
        Common.registerAgentInDf(this);
        addBehaviour(processMessage);
    }

    /**
     * Modifies value by opposite element modifier (less impact if opposite above 900, more if opposite under 100)
     * @param type Type of the element that is used
     * @param value Value of that element
     * @return New value after applying modifier
     */
    private int applyOppositeElementModifier(ElementType type, int value) {
        switch(type) {
            case WATER:
                if(value > 0 && settings.getHeatResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getHeatResource())/100.0));
                if(value < 0 && settings.getHeatResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getHeatResource())/100.0));
                break;
            case FIRE:
                if(value > 0 && settings.getWaterResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getWaterResource())/100.0));
                if(value < 0 && settings.getWaterResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getWaterResource())/100.0));
                break;
            case LIGHT:
                if(value > 0 && settings.getDarknessResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getDarknessResource())/100.0));
                if(value < 0 && settings.getDarknessResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getDarknessResource())/100.0));
                break;
            case DARKNESS:
                if(value > 0 && settings.getLightResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getLightResource())/100.0));
                if(value < 0 && settings.getLightResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getLightResource())/100.0));
                break;
            case EARTH:
                if(value > 0 && settings.getAirResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getAirResource())/100.0));
                if(value < 0 && settings.getAirResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getAirResource())/100.0));
                break;
            case AIR:
                if(value > 0 && settings.getEarthResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getEarthResource())/100.0));
                if(value < 0 && settings.getEarthResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getEarthResource())/100.0));
                break;
            case KNOWLEDGE:
                if(value > 0 && settings.getAmusementResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getAmusementResource())/100.0));
                if(value < 0 && settings.getAmusementResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getAmusementResource())/100.0));
                break;
            case AMUSEMENT:
                if(value > 0 && settings.getKnowledgeResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getKnowledgeResource())/100.0));
                if(value < 0 && settings.getKnowledgeResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getKnowledgeResource())/100.0));
                break;
            case LOVE:
                if(value > 0 && settings.getRestraintResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getRestraintResource())/100.0));
                if(value < 0 && settings.getRestraintResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getRestraintResource())/100.0));
                break;
            case RESTRAINT:
                if(value > 0 && settings.getLoveResource() < 100)
                    value = (int)((double)value * (1.0 + (100.0 - (double)settings.getLoveResource())/100.0));
                if(value < 0 && settings.getLoveResource() > 900)
                    value = (int)((double)value * (1.0 - (900.0 - (double)settings.getLoveResource())/100.0));
                break;
        }

        return value;
    }

    /**
     * Recalculates resources using actions done by gods
     */
    private void recalculateResourcesFromGodActions(GodInfluenceRegionAction[] actions) {
        for (GodInfluenceRegionAction action : actions) {
            for (int j = 0; j < action.getElements().size(); j++) {
                int value;
                switch (action.getElements().get(j)) {
                    case AIR:
                        value = settings.getAirResource();
                        value += applyOppositeElementModifier(ElementType.AIR, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setAirResource(value);
                        break;
                    case FIRE:
                        value = settings.getHeatResource();
                        value += applyOppositeElementModifier(ElementType.FIRE, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setHeatResource(value);
                        break;
                    case LOVE:
                        value = settings.getLoveResource();
                        value += applyOppositeElementModifier(ElementType.LOVE, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setLoveResource(value);
                        break;
                    case EARTH:
                        value = settings.getEarthResource();
                        value += applyOppositeElementModifier(ElementType.EARTH, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setEarthResource(value);
                        break;
                    case LIGHT:
                        value = settings.getLightResource();
                        value += applyOppositeElementModifier(ElementType.LIGHT, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setLightResource(value);
                        break;
                    case WATER:
                        value = settings.getWaterResource();
                        value += applyOppositeElementModifier(ElementType.WATER, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setWaterResource(value);
                        break;
                    case DARKNESS:
                        value = settings.getDarknessResource();
                        value += applyOppositeElementModifier(ElementType.DARKNESS, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setDarknessResource(value);
                        break;
                    case AMUSEMENT:
                        value = settings.getAmusementResource();
                        value += applyOppositeElementModifier(ElementType.AMUSEMENT, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setAmusementResource(value);
                        break;
                    case KNOWLEDGE:
                        value = settings.getKnowledgeResource();
                        value += applyOppositeElementModifier(ElementType.KNOWLEDGE, action.getValues().get(j));
                        value = Common.clamp(value, 0, 1000);
                        settings.setKnowledgeResource(value);
                        break;
                    case RESTRAINT:
                        value = settings.getRestraintResource();
                        value += applyOppositeElementModifier(ElementType.RESTRAINT, action.getValues().get(j));
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
                            Common.responseWithInformationAbout(getAgent(), settings, msg);
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
