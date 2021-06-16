package teamwork.agents;

import jade.core.AID;
import org.javatuples.Pair;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.enums.ElementType;
import teamwork.agents.utility.Common;
import teamwork.agents.wrappers.GodRegionWrapper;
import teamwork.agents.wrappers.RegionWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SuperGod extends God {
    private GodRegionWrapper[] settings;
    private AID[] gods;
    private List<RegionWrapper> regions;

    @Override
    protected void setup() {
        settings = (GodRegionWrapper[])getArguments()[0];
        gods = (AID[])getArguments()[1];
        for (var s : settings) {
            for (var r : s.getRegions()) {
                regions.add(r);
            }
        }
        Common.registerAgentInDf(this);
        addBehaviour(processMessage);
    }
    int fire = 0, water = 0, light = 0, darkness = 0, earth = 0, air = 0,
            knowledge = 0, amusement =0, love = 0, restraint = 0;

    GodAction ProcessSuperGodTurn() {
        int min_population = 10000;
        RegionWrapper regionName = new RegionWrapper();
        for (var element : regions){
            if (element.getPopulation() < min_population){
                regionName = element;
                min_population = element.getPopulation();
            }
        }
        Random rnd = new Random();
        if (min_population == 10000) //if all regions don't have any knownRegions
        {
            //int regionIndex = rnd.nextInt(settings[godIndex].getKnownRegions().size());
            regionName = regions.get(rnd.nextInt(regions.size()));
        }
        for(var element : settings) {
            fire += element.getFireSkill();
            water += element.getWaterSkill();
            light += element.getLightSkill();
            darkness += element.getDarknessSkill();
            earth += element.getEarthSkill();
            air += element.getAirSkill();
            knowledge += element.getKnowledgeSkill();
            amusement += element.getAmusementSkill();
            love += element.getLoveSkill();
            restraint += element.getRestraintSkill();
        }
        Pair<ElementType, Integer> pair = new Pair<>(ElementType.FIRE, fire);

        return new GodInfluenceRegionAction(getLocalName(), regionName.getName(), Collections.singletonList(pair.getValue0()), Collections.singletonList(pair.getValue1()));
    }

    /*@Override
    private void processTurn(ACLMessage message) {
        Gson _gson = new GsonBuilder().create();
        GodAction action;

        switch(message.getOntology()) {
            case "Your Turn (God)":
                RegionWrapper[] knownRegions = _gson.fromJson(message.getContent(), RegionWrapper[].class);
                action = ProcessGodTurn(knownRegions);
                break;
            case "Your Turn (Ask to Join)":
                action = ProcessAskTurn();
                break;
            case "Your Turn (Reply to Join Request)":
                action = ProcessReplyTurn();
                break;
            default:
                action = new GodDoNothingAction(getLocalName());
                break;
        }

        ACLMessage response = message.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology(action.actionType());
        response.setContent(_gson.toJson(action));
        send(response);
    }*/
}
