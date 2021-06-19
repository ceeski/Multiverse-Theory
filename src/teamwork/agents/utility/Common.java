package teamwork.agents.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class Common {
    /**
     * Registers agent in the DF
     * @param agent Agent to register
     */
    public static void registerAgentInDf(Agent agent) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( agent.getAID() );

        ServiceDescription sd = new ServiceDescription();
        sd.setName( agent.getLocalName() );
        sd.setType( agent.getClass().getCanonicalName() );
        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    public static void registerAgentInDf(Agent agent, String input) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( agent.getAID() );
        dfd.addLanguages(input);
        ServiceDescription sd = new ServiceDescription();
        sd.setName( agent.getLocalName() );
        sd.setType( agent.getClass().getCanonicalName() );
        dfd.addServices(sd);
        try {
            DFService.register(agent, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes agent from DF
     * @param agent Agent to be removed
     */
    public static void removeAgentFromDf(Agent agent) {
        try {
            DFService.deregister(agent);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets agent descriptors from DF by type
     * @param agent Agent asking DF
     * @param agentClass Class of agents that we are looking for
     * @return Array of found descriptors
     */
    public static DFAgentDescription[] findAgentsInDf(Agent agent, Class<?> agentClass) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType(agentClass.getCanonicalName());
        dfd.addServices(sd);

        DFAgentDescription[] foundAgents = new DFAgentDescription[1];

        try {
            foundAgents = DFService.search(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return foundAgents;
    }

    /**
     * Responds for message using as INFORM with ontology "Information" with settings as json in the content. Used by gods and regions to inform time about their state
     * @param agent Agent responding
     * @param settings Settings that will be in the content
     * @param msg Message to respond to
     * @param <T> Type of settings
     */
    public static <T> void responseWithInformationAbout(Agent agent, T settings, ACLMessage msg) {
        Gson _gson = new GsonBuilder().create();
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.INFORM);
        response.setOntology("Information");
        response.setContent(_gson.toJson(settings));
        agent.send(response);
    }
    public static <T> void responseWithInformationAbout(Agent agent, T settings, ACLMessage msg, String goodness) {
        Gson _gson = new GsonBuilder().create();
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.INFORM);
        response.setOntology("Information");
        response.setLanguage(goodness);
        response.setContent(_gson.toJson(settings));
        agent.send(response);
    }

    /**
     * Clamps value between min and max
     */
    public static int clamp(int value, int min, int max) {
        if(value < min)
            value = min;
        if(value > max)
            value = max;
        return value;
    }
}
