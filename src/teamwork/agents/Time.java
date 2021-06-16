package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.javatuples.Pair;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.RegionWrapper;
import teamwork.agents.wrappers.TimeWrapper;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.utility.Common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Time extends Agent {
    TimeWrapper settings;
    List<GodWrapper> gods = new ArrayList<>();
    List<RegionWrapper> regions = new ArrayList<>();
    List<GodAction> turnActions = new ArrayList<>();
    List<AID> godsAsked = new ArrayList<>();
    List<AID> godsAsking = new ArrayList<>();
    int round = 0;
    boolean endMessageShown = false;

    @Override
    protected void setup() {
        settings = (TimeWrapper)getArguments()[0];
        round = 0;
        endMessageShown = false;
        addBehaviour(timeFlow);
    }

    /**
     * Logs message to the file and to the console
     */
    private void say(String message) {
        logToFile(message);
        System.out.println(message);
    }

    /**
     * Logs string to file. File is opened/closed each time this function is called so it should not be abused
     */
    private void logToFile(String message) {
        try {
            Writer outputFile = new BufferedWriter(new FileWriter(settings.getPathToLogFile(), true));
            outputFile.write(message);
            outputFile.close();
        } catch (IOException e) {
            System.out.println("Could not log to file");
        }
    }

    /**
     * Logs the regions' states to the file and to the console
     */
    private void logRegionsState() {
        StringBuilder sb = new StringBuilder();
        for(var region : regions) {
            sb.append(region);
        }

        say(sb.toString());
    }

    /**
     * Logs all saved actions to the file and to the console
     */
    private void logActions() {
        StringBuilder sb = new StringBuilder();
        for(var action : turnActions) {
            sb.append(action);
        }

        say(sb.toString());
    }

    /**
     * Loads all gods and regions currently registered in DF to lists (as wrappers)
     */
    private void getGodsAndRegionsInfo() {
        gods = new ArrayList<>();
        regions = new ArrayList<>();
        turnActions = new ArrayList<>();
        godsAsked = new ArrayList<>();
        godsAsking = new ArrayList<>();

        Gson _gson = new GsonBuilder().create();
        DFAgentDescription[] godDescriptors = Common.findAgentsInDf(this, God.class);
        DFAgentDescription[] regionDescriptors = Common.findAgentsInDf(this, Region.class);

        for(var godDescriptor : godDescriptors) {
            var godAID = godDescriptor.getName();
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setOntology("Initial Information");
            message.addReceiver(godAID);

            send(message);

            MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate ontology = MessageTemplate.MatchOntology("Information");
            MessageTemplate template = MessageTemplate.and(performative, ontology);

            ACLMessage response = blockingReceive(template);
            if(response == null) {
                say("Time haven't got the response from god " + godAID.getLocalName());
                return;
            }
            gods.add(_gson.fromJson(response.getContent(), GodWrapper.class));
        }

        for(var regionDescriptor : regionDescriptors) {
            var regionAID = regionDescriptor.getName();
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setOntology("Initial Information");
            message.addReceiver(regionAID);

            send(message);

            MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate ontology = MessageTemplate.MatchOntology("Information");
            MessageTemplate template = MessageTemplate.and(performative, ontology);

            ACLMessage response = blockingReceive(template);
            if(response == null) {
                say("Time haven't got the response from region " + regionAID.getLocalName());
                return;
            }
            regions.add(_gson.fromJson(response.getContent(), RegionWrapper.class));
        }
    }

    /**
     * Calculates turn order and return gods in their order as wrappers
     */
    private List<GodWrapper> getGodsTurnOrder() {
        Random rd = new Random();
        List<Pair<GodWrapper, Integer>> godsWithSpeed = new ArrayList<>();

        for(var god : gods) {
            int modifier = (rd.nextInt() % 21) - 10; //Modifier is from -10 to 10
            godsWithSpeed.add(new Pair<>(god, god.getSpeed() + modifier));
        }

        //Reverse (descending) sort by speed with modifier and return list of only names
        return godsWithSpeed.stream().sorted((p1, p2) -> (-1) * p1.getValue1().compareTo(p2.getValue1())).map(Pair::getValue0).collect(Collectors.toList());
    }

    CyclicBehaviour timeFlow = new CyclicBehaviour(this) {
        @Override
        public void action() {
            //Step 0: Check if finished
            if(round >= settings.getNumberOfTurns()) {
                if(!endMessageShown) {
                    endMessageShown = true;
                    say("SIMULATION FINISHED AFTER " + round + " ROUNDS\n");
                    //Log final state of regions
                    getGodsAndRegionsInfo();
                    logRegionsState();
                }
                block();
                return;
            }

            Gson _gson = new GsonBuilder().create();
            turnActions = new ArrayList<>();

            round++;
            say("ROUND NUMBER " + round + "\n");

            //Step 1: Get all gods and regions with their actual state in DF
            getGodsAndRegionsInfo();

            //Log1: Regions at the beginning of the turn
            logRegionsState();
            say("----------\n");

            //Step 2: Get gods order
            var godsOrder = getGodsTurnOrder();
            System.out.println("Gods order:");
            StringBuilder list = new StringBuilder("\t");
            for(var god : godsOrder)
                list.append(god.getName()).append("; ");
            System.out.println(list.toString());

            //Step 3: Ask each god to perform action
            for(var god : godsOrder) {
                //3.1: Build a message based on god's type
                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                String ontology;
                String content;

                List<RegionWrapper> regionsForGod = regions.stream().filter(
                        region -> god.getKnownRegions().stream().anyMatch(
                                knownRegion -> knownRegion.equals(region.getName())
                        )
                ).collect(Collectors.toList());

                List<GodInfluenceRegionAction> previousActionsForKnownRegions = turnActions.stream()
                        .filter(GodInfluenceRegionAction.class::isInstance)
                        .map(GodInfluenceRegionAction.class::cast)
                        .filter(
                                action -> regionsForGod.stream().anyMatch(
                                        region -> region.getName().equals(action.getRegionName())
                                )
                        )
                        .collect(Collectors.toList());

                switch(god.getType()) {
                    case PROTECTOR:
                        ontology = "Your Turn (Protector)";
                        content = _gson.toJson(new ProtectorTurnInfoWrapper(regionsForGod, previousActionsForKnownRegions));
                        break;
                    case CHAOTIC:
                        ontology = "Your Turn (Chaotic)";
                        content = "";
                        break;
                    default:
                        ontology = "Your Turn (God)";
                        content = _gson.toJson(regionsForGod);
                        break;
                }

                message.addReceiver(new AID(god.getName(), AID.ISLOCALNAME));
                message.setOntology(ontology);
                message.setContent(content);

                //3.2: Send and wait for the response
                send(message);

                MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                ACLMessage response = blockingReceive(template);
                if(response == null) {
                    say("Time haven't got the response from god " + god.getName());
                    return;
                }

                //3.3: Save action performed by god
                try {
                    turnActions.add(_gson.fromJson(response.getContent(), (Type) Class.forName(response.getOntology())));
                } catch (ClassNotFoundException e) {
                    say("Action type " + response.getOntology() + " could not be found from god " + god.getName());
                    return;
                }
            }

            //Step 4: Tell each region what happened
            for(var regionName : regions.stream().map(RegionWrapper::getName).collect(Collectors.toList())) {
                //4.1 Build message
                List<GodInfluenceRegionAction> actionsForRegion = turnActions.stream()
                        .filter(GodInfluenceRegionAction.class::isInstance)
                        .map(GodInfluenceRegionAction.class::cast)
                        .filter(
                                action -> regionName.equals(action.getRegionName())
                        )
                        .collect(Collectors.toList());

                ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                message.addReceiver(new AID(regionName, AID.ISLOCALNAME));
                message.setOntology("Recalculate");
                message.setContent(_gson.toJson(actionsForRegion));

                //4.2 Send message and wait for response
                send(message);

                MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                MessageTemplate ontology = MessageTemplate.MatchOntology("Recalculated");
                MessageTemplate template = MessageTemplate.and(performative, ontology);
                ACLMessage response = blockingReceive(template);
                if(response == null) {
                    say("Time haven't got the response from region " + regionName);
                    return;
                }
            }

            //Log 2: Log all actions in this turn
            logActions();
            say("\n\n");
        }
    };

}
