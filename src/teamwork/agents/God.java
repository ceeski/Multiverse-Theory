package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import teamwork.agents.enums.ElementType;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.RegionWrapper;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodDoNothingAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.utility.Common;

public class God extends Agent {
    private GodWrapper settings;

    @Override
    protected void setup() {
        settings = (GodWrapper)getArguments()[0];
        Common.RegisterAgentInDf(this);
        addBehaviour(processMessage);
    }

    /**
     * Processes turn of god that knows their regions state from the beginning of the turn (creator, Destructor, neutral)
     */
    GodAction ProcessGodTurn(RegionWrapper[] knownRegions) {
        return new GodInfluenceRegionAction(getLocalName(), "Low", new ElementType[]{ElementType.WATER}, new int[]{50});
    }

    /**
     * Processes turn of god that knows nothing (chaotic)
     */
    GodAction ProcessChaoticTurn() {
        return new GodDoNothingAction(getLocalName());
    }

    /**
     * Process turn of god that knows their regions state from the beginning of the turn and all previous gods influences in those regions in this turn (protector)
     */
    GodAction ProcessProtectorTurn(ProtectorTurnInfoWrapper info) {
        return new GodDoNothingAction(getLocalName());
    }

    /**
     * Interprets what kind of information god got, calls appropriate function and responds
     */
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
                        if(msg.getOntology().startsWith("Your Turn"))
                            processTurn(msg);
                        break;
                    default:
                        break;
                }
            }

            block();
        }
    };

}
