package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodDoNothingAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.enums.ElementType;
import teamwork.agents.enums.GodType;
import teamwork.agents.utility.Common;
import teamwork.agents.utility.GodHelper;
import teamwork.agents.wrappers.GodRegionWrapper;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.wrappers.RegionWrapper;

import java.util.*;
import java.util.stream.Collectors;


public class God extends Agent {
    private GodWrapper settings;
    RegionWrapper[] knownRegions;
    private static final int BALANCE = 575; //Balance is slightly higher to represent the fact that people will use resources
    private static final int MAX = 1000;
    private static final int MIN = 0;
    public boolean start = true;
    //private ACLMessage sequential;

    @Override
    protected void setup() {
        settings = (GodWrapper) getArguments()[0];
        Common.registerAgentInDf(this, "1");
        addBehaviour(processMessage);

    }

    /**
     * Processes turn of god that knows their regions state from the beginning of the turn (creator, Destructor, neutral)
     */
    public GodAction ProcessGodTurn(RegionWrapper[] knownRegions) {
        var possibilities = GodHelper.getPossibleElementChanges(settings);

        //Get list of all regions and resources that can be influenced in a way
        //Triplet: (region name, element, actual resource value)
        List<Triplet<String, ElementType, Integer>> regionsElementsScores = new ArrayList<>();

        for (var region : knownRegions) {
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.FIRE, region.getHeatResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.WATER, region.getWaterResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.LIGHT, region.getLightResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.DARKNESS, region.getDarknessResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.EARTH, region.getEarthResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.AIR, region.getAirResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.KNOWLEDGE, region.getKnowledgeResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.AMUSEMENT, region.getAmusementResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.LOVE, region.getLoveResource()));
            regionsElementsScores.add(new Triplet<>(region.getName(), ElementType.RESTRAINT, region.getRestraintResource()));
        }

        if (settings.getType().equals(GodType.CREATOR)) {
            //Creator calculates all possible positive changes: if value < balance it tries to add as much as they can to reach balance
            // if value > balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List<Triplet<String, ElementType, Integer>> possibleActions = regionsElementsScores.stream().map(entry -> {
                ElementType element = entry.getValue1();
                int resourceValue = entry.getValue2();
                int change = 0;

                if (resourceValue <= BALANCE) { //Actual value <= balance, we want to add
                    int maxChange = resourceValue + possibilities.get(element).getValue1();
                    if (maxChange > BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue1();
                } else { //Actual value > balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if (maxChange < BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue0();
                }
                return new Triplet<>(entry.getValue0(), element, change);
            }).sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2()))).collect(Collectors.toList());

            int maxPossibleChange = Math.abs(possibleActions.get(0).getValue2());

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
        } else if (settings.getType().equals(GodType.DESTRUCTOR)) {
            //Destructor calculates all possible negative changes: if value > balance it tries to add as much as they can to reach balance
            // if value < balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List<Triplet<String, ElementType, Integer>> possibleActions = regionsElementsScores.stream().map(entry -> {
                ElementType element = entry.getValue1();
                int resourceValue = entry.getValue2();
                int change = 0;

                if (resourceValue <= BALANCE) { //Actual value <= balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if (maxChange < MIN)
                        change = MIN - resourceValue;
                    else
                        change = possibilities.get(element).getValue0();
                } else { //Actual value > balance, we want to add
                    int maxChange = resourceValue + possibilities.get(element).getValue1();
                    if (maxChange > MAX)
                        change = MAX - resourceValue;
                    else
                        change = possibilities.get(element).getValue1();
                }
                return new Triplet<>(entry.getValue0(), element, change);
            }).sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2()))).collect(Collectors.toList());

            int maxPossibleChange = Math.abs(possibleActions.get(0).getValue2());

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
        } else if (settings.getType().equals(GodType.NEUTRAL)) {
            //NEUTRAL works exactly the same way as creator (code is repeated so it can be easier to change separately), but all possibilities of change are divided by 2, so instead of [-180, 250] it would be [-90, 125]
            for (var element : (ElementType[]) possibilities.keySet().toArray()) {
                possibilities.get(element).setAt0(possibilities.get(element).getValue0() / 2);
                possibilities.get(element).setAt1(possibilities.get(element).getValue1() / 2);
            }

            //Creator calculates all possible positive changes: if value < balance it tries to add as much as they can to reach balance
            // if value > balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List<Triplet<String, ElementType, Integer>> possibleActions = regionsElementsScores.stream().map(entry -> {
                ElementType element = entry.getValue1();
                int resourceValue = entry.getValue2();
                int change = 0;

                if (resourceValue <= BALANCE) { //Actual value <= balance, we want to add
                    int maxChange = resourceValue + possibilities.get(element).getValue1();
                    if (maxChange > BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue1();
                } else { //Actual value > balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if (maxChange < BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue0();
                }
                return new Triplet<>(entry.getValue0(), element, change);
            }).sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2()))).collect(Collectors.toList());

            int maxPossibleChange = Math.abs(possibleActions.get(0).getValue2());

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
        }
        return new GodDoNothingAction(getLocalName());
    }

    /**
     * Processes turn of god that knows nothing (chaotic)
     */
    GodAction ProcessChaoticTurn() {
        Random rnd = new Random();

        //Simply 33% chance to skip turn
        boolean willSkipTurn = rnd.nextInt() % 100 < 33;
        if (willSkipTurn)
            return new GodDoNothingAction(getLocalName());

        //Get all possible region influences
        var possibilities = GodHelper.getPossibleElementChanges(settings);
        List<Triplet<ElementType, Integer, Integer>> possibleChanges = new ArrayList<>();

        for (var element : ElementType.AllTypes()) {
            Pair<Integer, Integer> bounds = possibilities.get(element);
            if (bounds.getValue0().equals(bounds.getValue1()) && bounds.getValue0() == 0)
                continue;
            possibleChanges.add(new Triplet<>(element, bounds.getValue0(), bounds.getValue1()));
        }

        if (possibleChanges.size() == 0)
            return new GodDoNothingAction(getLocalName());

        int changeIndex = rnd.nextInt(possibleChanges.size());
        ElementType element = possibleChanges.get(changeIndex).getValue0();
        int value = (rnd.nextInt(possibleChanges.get(changeIndex).getValue2() - possibleChanges.get(changeIndex).getValue1()) + 1) + possibleChanges.get(changeIndex).getValue1();
        int finalValue = GodHelper.finalElementChange(value, element, settings);

        int regionIndex = rnd.nextInt(settings.getKnownRegions().size());
        String regionName = settings.getKnownRegions().get(regionIndex);

        return new GodInfluenceRegionAction(getLocalName(), regionName, Collections.singletonList(element), Collections.singletonList(finalValue));
    }

    /**
     * Process turn of god that knows their regions state from the beginning of the turn and all previous gods influences in those regions in this turn (protector)
     */
    GodAction ProcessProtectorTurn(ProtectorTurnInfoWrapper info) {
        //Basically we look at all previous actions and calculate how much can we block each one of them - and block the one that will be blocked the most
        var possibilities = GodHelper.getPossibleElementChanges(settings);

        //Get list of all regions and resources that can be influenced in a way
        //Triplet: (region name, element, actual resource value)
        List<Triplet<String, ElementType, Integer>> changes = new ArrayList<>();
        for (var change : info.getPreviousActions())
            for (int i = 0; i < change.getElements().size(); i++)
                changes.add(new Triplet<>(change.getRegionName(), change.getElements().get(i), change.getValues().get(i)));

        if (changes.size() == 0)
            return new GodDoNothingAction();

        //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
        List<Triplet<String, ElementType, Integer>> possibleActions = changes.stream().map(entry -> {
            ElementType element = entry.getValue1();
            int resourceChange = entry.getValue2();
            int change = 0;

            if (resourceChange <= 0) { //Negative change, so we want to add
                if (possibilities.get(element).getValue1() + resourceChange < 0)
                    change = possibilities.get(element).getValue1();
                else
                    change = -resourceChange;
            } else { //Positive value, so we want to subtract
                if (possibilities.get(element).getValue0() + resourceChange > 0)
                    change = possibilities.get(element).getValue0();
                else
                    change = -resourceChange;
            }
            return new Triplet<>(entry.getValue0(), element, change);
        }).sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2()))).collect(Collectors.toList());

        int maxPossibleChange = Math.abs(possibleActions.get(0).getValue2());

        //We want to limit possible actions to only the ones with maximal (the same) change:
        possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

        //Now we will take random one of proposed ones
        Random rand = new Random();
        Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
        int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
        return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
    }

    public void ProcessQuery(ACLMessage msg) {
        //System.out.println(String.valueOf(msg != null));
        if (msg != null) {
            //System.out.println(msg.toString());
            ACLMessage response = msg.createReply();
            response.addReceiver(msg.getSender());
            response.setSender(this.getAID());
            if ((msg.getOntology().equals("good") && settings.getType() != GodType.DESTRUCTOR) || (msg.getOntology().equals("bad") && settings.getType() != GodType.CREATOR)) {
                Gson _gson = new GsonBuilder().create();
                this.settings.setSeparate(false);
                response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                response.setOntology("ProcessQuery");
                response.setContent(_gson.toJson(new GodRegionWrapper(settings, knownRegions)));
                Common.removeAgentFromDf(this);
                Common.registerAgentInDf(this, "0");
            } else {
                response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                this.settings.setSeparate(true);
                response.setOntology("ProcessQuery");
            }
            /*if (settings != null) {
                System.out.println("Reply| " + response.getSender().getLocalName() + " to " + msg.getSender().getLocalName() + " "
                        + response.getPerformative() + ": " + response.getOntology().toString());
            }*/
            send(response);
        }
    }

    public void ProcessReplyTurn(ACLMessage msg) throws StaleProxyException {
        if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            Common.removeAgentFromDf(this);
            this.settings.setSeparate(false);
            Common.registerAgentInDf(this, "0");
            Gson _gson = new GsonBuilder().create();
            //System.out.println(msg);
            GodRegionWrapper tmp = (_gson.fromJson(msg.getContent(), GodRegionWrapper.class));
            this.getContainerController().createNewAgent(settings.getName() + tmp.getGod().getName(), "teamwork.agents.SuperGod", new Object[]{tmp, new AID[]{this.getAID(), msg.getSender()}});
            System.out.println("CREATED SUPERGOD:" + settings.getName() + tmp.getGod().getName());

        }
        else {
            Common.removeAgentFromDf(this);
            this.settings.setSeparate(true);
            Common.registerAgentInDf(this, "1");
        }
    }

    /**
     * Interprets what kind of information god got, calls appropriate function and responds
     */
    public void processTurn(ACLMessage msg) throws InterruptedException {
        Gson _gson = new GsonBuilder().create();
        GodAction action = new GodDoNothingAction();
        //System.out.println("This is the " +askToBecomeSupergod.getDataStore().get("end"));

        //sleep(10000);

/*        if (settings != null && settings.getChanceToCooperatePercent() > 50 && settings.getSeparate()) {
            ACLMessage message = new ACLMessage();
            message.setPerformative(ACLMessage.QUERY_IF);
            message.setOntology("good");
            List<DFAgentDescription> godDescriptors = Arrays.asList(Common.findAgentsInDf(this, God.class));//(DFAgentDescription[]) Arrays.stream(Common.findAgentsInDf(this.getAgent(), God.class)).filter(i -> settings.getKnownGods().contains(i.getName())).toArray();

            int i = 0;
            for (var d : godDescriptors) {
                if ((d.getName().equals(settings.getName()) && !settings.getKnownGods().contains(d.getName()))) {
                    godDescriptors.remove(d);
                }
            }
            Random rnd = new Random();
            message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
            send(message);
            ACLMessage msg3 = receive();
            if (msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && settings.getSeparate() && !msg.getSender().getLocalName().equals("Time")){
                try {
                    ProcessReplyTurn(msg3);
                    action = new GodDoNothingAction(getLocalName());
                    open = false;
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
            else {
                open = true;
            }
        }*/


        switch (msg.getOntology()) {
            case "Your Turn (God)":
                if (settings.getSeparate()) {
                    knownRegions = _gson.fromJson(msg.getContent(), RegionWrapper[].class);
                    action = ProcessGodTurn(knownRegions);
                }
                    /*else {
                        action = new GodDoNothingAction(getLocalName())
                    }*/
                break;
            case "Your Turn (Chaotic)":
                if (settings.getSeparate())
                    action = ProcessChaoticTurn();
                break;
            case "Your Turn (Protector)":
                if (settings.getSeparate()) {
                    ProtectorTurnInfoWrapper protectorInfo = _gson.fromJson(msg.getContent(), ProtectorTurnInfoWrapper.class);
                    action = ProcessProtectorTurn(protectorInfo);
                }
                break;
            default:
                action = new GodDoNothingAction(getLocalName());
                break;
        }

        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology(action.actionType());
        response.setContent(_gson.toJson(action));
        System.out.println(settings.getName()+" responded to Time");
        send(response);}



    OneShotBehaviour askToBecomeSupergod = new OneShotBehaviour(this) {
        public DataStore myData;
        public void onStart() {
           // System.out.println("One shot started");
        }
        @Override
        public void action()
        {
            myData = new DataStore();
            myData.put("end", "false");
            System.out.println(settings.getName()+": start");
            if (settings.getChanceToCooperatePercent() > 50) {
                ACLMessage message = new ACLMessage();
                message.setPerformative(ACLMessage.QUERY_IF);
                message.setLanguage("Cyclic");
                message.setOntology("good");
                List<DFAgentDescription> godDescriptors2 = Arrays.asList(Common.findAgentsInDf(this.myAgent, God.class));//(DFAgentDescription[]) Arrays.stream(Common.findAgentsInDf(this.getAgent(), God.class)).filter(i -> settings.getKnownGods().contains(i.getName())).toArray();
                //System.out.println(godDescriptors);
                int i = 0;
                List<DFAgentDescription> godDescriptors = new ArrayList<>();
                for (var d : godDescriptors2) {
                    if (settings.getKnownGods().contains(d.getName().getLocalName()) && !godDescriptors2.getClass().equals(SuperGod.class)) {
                        godDescriptors.add(d);
                        //System.out.println(d.getName().getLocalName() + " " + settings.getName() + "|");
                    }

                }

                //Random rnd = new Random();
                if (godDescriptors.size()>0) {
                    Random rnd = new Random();
                    message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
                    start = false;
                    send(message);
                    MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    MessageTemplate performative2 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
                    MessageTemplate ontology = MessageTemplate.MatchOntology("ProcessQuery"), ontology2 = MessageTemplate.MatchOntology("bad");
                    MessageTemplate template = MessageTemplate.and(performative, ontology);
                    MessageTemplate template2 = MessageTemplate.and(performative, ontology2);

                    ACLMessage msg3 = blockingReceive(ontology);
                    //sleep(3000);

                    if (msg3 != null) {
                        System.out.println(msg3.getSender().getLocalName()+ " to " + settings.getName() + " " + msg3.getPerformative() + ": " + msg3.getOntology().toString());
                        if (msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            try {
                                ProcessReplyTurn(msg3);
                                //System.out.println("Created new god: " + settings.getName() + msg3.getSender().getName());
                                //System.out.println(msg.getSender().getName() + "to " + settings.getName() + " " + msg.getPerformative() + ": " + msg.getOntology().toString());
                                start = true;
                                //action = new GodDoNothingAction(getLocalName());
                                //open = false;
                            } catch (StaleProxyException e) {
                                e.printStackTrace();
                            }
                        }
                        //messageSentAndReceived = true;

                    }
                }
            }
            start = true;
            System.out.println(settings.getName()+": end");
            myData.put("end", "true");
        }

    };



    CyclicBehaviour processMessage = new CyclicBehaviour(this) {
        @Override
        public void action() {
            MessageTemplate tmp = MessageTemplate.MatchLanguage("Cyclic");
            ACLMessage msg = blockingReceive(tmp);
            Random rnd = new Random();
            if (msg != null) {
                //If message is not from time and god don't know the sender, add sender to known gods
                if (!msg.getSender().getLocalName().equals("Time")) {
                    if (settings.getKnownGods().stream().noneMatch(name -> name.equals(msg.getSender().getLocalName()))) {
                        settings.getKnownGods().add(msg.getSender().getLocalName());
                    }

                }
                switch (msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        if (msg.getOntology().equals("Initial Information") )//&& settings.getSeparate())
                        {Common.responseWithInformationAbout(getAgent(), settings, msg);
                            myAgent.removeBehaviour(askToBecomeSupergod);}
                        break;
                    case ACLMessage.INFORM:
                        if (msg.getOntology().startsWith("Your Turn")) {
                            System.out.println(settings.getName()+" got message from Time");
                            try {
                                myAgent.addBehaviour(askToBecomeSupergod);
                                askToBecomeSupergod.action();
                                DataStore ds = new DataStore();
                                ds.put("end","false");
                                askToBecomeSupergod.setDataStore(ds);
                                while(askToBecomeSupergod.getDataStore().get("end").toString().equals("true"))
                                {/*System.out.println("DATA:"+askToBecomeSupergod.getDataStore().get("end").toString());*/};

                                //ds.put("end","false");

                                //askToBecomeSupergod.setDataStore(ds);
                                processTurn(msg);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case ACLMessage.QUERY_IF:
                        if (settings.getSeparate() && !msg.getSender().getLocalName().equals("Time") && (msg.getOntology().contains("good") || msg.getOntology().contains("bad"))) {
                            ProcessQuery(msg);
                        }
                        break;
                    /*case ACLMessage.ACCEPT_PROPOSAL:
                        System.out.println("Supergod "+settings.getName()+msg.getSender().getLocalName()+" is merging");
                        break;
                    case ACLMessage.REJECT_PROPOSAL:
                        System.out.println("Supergod "+settings.getName()+msg.getSender().getLocalName()+" could not be created");
                        break;*/
                        /*if (settings.getSeparate() && !msg.getSender().getLocalName().equals("Time")){
                            try {
                                ProcessReplyTurn(msg);
                            } catch (StaleProxyException e) {
                                e.printStackTrace();
                            }
                        }
                        break;*/
                    default:
                        break;
                }




                /*if (settings != null && settings.getChanceToCooperatePercent() > 50 && settings.getSeparate()) {
                    ACLMessage message = new ACLMessage();
                    message.setPerformative(ACLMessage.QUERY_IF);
                    message.setOntology("good");
                    List<DFAgentDescription> godDescriptors = Arrays.asList(Common.findAgentsInDf(this.getAgent(), God.class));//(DFAgentDescription[]) Arrays.stream(Common.findAgentsInDf(this.getAgent(), God.class)).filter(i -> settings.getKnownGods().contains(i.getName())).toArray();

                    int i = 0;
                    for (var d : godDescriptors) {
                        if ((d.getName().equals(settings.getName()) && !settings.getKnownGods().contains(d.getName()))) {
                            godDescriptors.remove(d);
                        }
                    }
                    message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
                    send(message);
                }*/
            }

            /*if (settings != null && settings.getSeparate()) {
                ACLMessage msg3 = receive();
                if (msg3 != null && msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                    System.out.println("Accept " + msg3.getSender().getName() + "to " + settings.getName() + " " + msg3.getPerformative() + ": " + msg3.getOntology().toString());
                try {
                    ProcessReplyTurn(msg3);
                            if (settings.getSeparate()) {
                                processTurn(msg3);
                            }
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }*/


            block();
        }
    };

}
