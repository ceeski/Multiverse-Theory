package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodDoNothingAction;
import teamwork.agents.utility.Common;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.wrappers.RegionWrapper;

import java.util.Random;

public class SuperGod extends God {
    private GodWrapper[] settings;
    private AID[] gods;
    private RegionWrapper[] regions;

    @Override
    protected void setup() {
        settings = (GodWrapper[])getArguments()[0];
        gods = (AID[])getArguments()[1];
        if (getArguments().length > 1){
            regions = (RegionWrapper[])getArguments()[2];
        }
        Common.registerAgentInDf(this);
        addBehaviour(processMessage);
    }
    int fire = 0, water = 0, light = 0, darkness = 0, earth = 0,
            air = 9, knowledge = 0, amusement =0, love = 0, restraint = 0;
    GodAction ProcessSuperGodTurn() {
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
        int min_population = 10000;
        String regionName = "";
        for (var element : regions){
            if (element.getPopulation() < min_population){
                regionName = element.getName();
                min_population = element.getPopulation();
            }
        }
        Random rnd = new Random();
        if (regionName == "") //if all regions don't have any knownRegions
        {
                int godIndex = rnd.nextInt(settings.length);
                int regionIndex = rnd.nextInt(settings[godIndex].getKnownRegions().size());
                regionName = settings[godIndex].getKnownRegions().get(regionIndex);
        }
        return new GodDoNothingAction(getLocalName());
    }

    private void processTurn(ACLMessage message) {
        Gson _gson = new GsonBuilder().create();
        GodAction action;

        switch(message.getOntology()) {
            case "Your Turn (God)":
                RegionWrapper[] knownRegions = _gson.fromJson(message.getContent(), RegionWrapper[].class);
                action = ProcessGodTurn(knownRegions);
                break;
            case "Your Turn (Chaotic)":
                action = ProcessChaoticTurn();
                break;
            case "Your Turn (Protector)":
                ProtectorTurnInfoWrapper protectorInfo = _gson.fromJson(message.getContent(), ProtectorTurnInfoWrapper.class);
                action = ProcessProtectorTurn(protectorInfo);
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
    }
}
