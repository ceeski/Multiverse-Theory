package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.content.lang.sl.SLCodec;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import teamwork.agents.actions.*;
import teamwork.agents.enums.ElementType;
import teamwork.agents.enums.GodType;
import teamwork.agents.utility.Common;
import teamwork.agents.utility.GodHelper;
import teamwork.agents.wrappers.GodRegionWrapper;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.wrappers.RegionWrapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class God extends Agent {
    private GodWrapper settings;
    private static final int BALANCE = 575; //Balance is slightly higher to represent the fact that people will use resources
    private static final int SMALL_CHANGE = 45; //If change is small (below this number) - god might consider learning something
    private static final int MAX = 1000;
    private static final int MIN = 0;
    public static boolean isHeadOfHolon = false;
    public static List<AID> godSubHolons;
    public static List<GodType> godTypes;
    private boolean turned = false;
    private int change = 0;
    private boolean isGood;
    private Agent myAgent;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
        if (getArguments().length == 4) {
            isHeadOfHolon = (boolean)getArguments()[0];
            godSubHolons = Arrays.asList((AID[]) getArguments()[1]);
            godTypes = Arrays.asList((GodType[])getArguments()[2]);
            settings = (GodWrapper) Arrays.asList((GodWrapper[])getArguments()[3]).get(0);
            settings.updateGod((GodWrapper) Arrays.asList((GodWrapper[])getArguments()[3]).get(1));
            settings.setName(getLocalName());
            settings.setSeparate(false);
            this.settings.setType(GodType.SUPERGOD);
            Common.registerAgentInDf(this, "2");
        } else {
            settings = (GodWrapper) getArguments()[0];
            settings.setSeparate(true);
            isHeadOfHolon = false;
            godSubHolons = new ArrayList<>();
            Common.registerAgentInDf(this, "1");
            isGood = settings.getType().equals(GodType.CREATOR);
        }

        addBehaviour(processMessage);
    }

    private GodWrapper CalculateTheBestOption(List<GodRegionWrapper> gods) {
        int index = 0;
        int speed = 0;
        List<Quartet<ElementType, Integer, Integer, Integer>> possibleChanges = new ArrayList<>();
        for (var g : gods) {
            if (g.getGod().getType().equals(GodType.PROTECTOR)) {
                Triplet<ElementType, Integer, Integer> c = CalculateTheBestProtector((GodWrapper) g.getGod());
                possibleChanges.add(new Quartet<>(c.getValue0(), c.getValue1(), c.getValue2(), index));
            } else if (g.getGod().getType().equals(GodType.CHAOTIC)) {
                Triplet<ElementType, Integer, Integer> c = CalculateTheBestChaotic((GodWrapper) g.getGod());
                possibleChanges.add(new Quartet<>(c.getValue0(), c.getValue1(), c.getValue2(), index));
            } else {
                Triplet<ElementType, Integer, Integer> c = CalculateTheBestCreatorDestructor((GodWrapper) g.getGod());
                possibleChanges.add(new Quartet<>(c.getValue0(), c.getValue1(), c.getValue2(), index));
            }
            speed += g.getSpeed();
            index++;
        }
        GodWrapper god  = gods.get(index % gods.size()).getGod();
        god.setSpeed(speed*index);
        return god;
    }

    private Triplet<ElementType, Integer, Integer> CalculateTheBestProtector(GodWrapper god) {
        var possibilities = GodHelper.getPossibleElementChanges(god);
        List<Triplet<ElementType, Integer, Integer>> possibleChanges = new ArrayList<>();

        for (var element : ElementType.AllTypes()) {
            Pair<Integer, Integer> bounds = possibilities.get(element);
            if (bounds.getValue0().equals(bounds.getValue1()) && bounds.getValue0() == 0)
                continue;
            possibleChanges.add(new Triplet<>(element, bounds.getValue0(), bounds.getValue1()));
        }
        return possibleChanges.get(0);
    }

    private Triplet<ElementType, Integer, Integer> CalculateTheBestChaotic(GodWrapper god) {
        var possibilities = GodHelper.getPossibleElementChanges(god);
        List<Triplet<ElementType, Integer, Integer>> possibleChanges = new ArrayList<>();

        for (var element : ElementType.AllTypes()) {
            Pair<Integer, Integer> bounds = possibilities.get(element);
            if (bounds.getValue0().equals(bounds.getValue1()) && bounds.getValue0() == 0)
                continue;
            possibleChanges.add(new Triplet<>(element, bounds.getValue0(), bounds.getValue1()));
        }
        possibleChanges.sort((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2())));
        return possibleChanges.get(0);
    }

    private Triplet<ElementType, Integer, Integer> CalculateTheBestCreatorDestructor(GodWrapper god) {
        var possibilities = GodHelper.getPossibleElementChanges(god);
        List<Triplet<ElementType, Integer, Integer>> possibleChanges = new ArrayList<>();

        for (var element : ElementType.AllTypes()) {
            Pair<Integer, Integer> bounds = possibilities.get(element);
            if (bounds.getValue0().equals(bounds.getValue1()) && bounds.getValue0() == 0)
                continue;
            possibleChanges.add(new Triplet<>(element, bounds.getValue0(), bounds.getValue1()));
        }
        possibleChanges.sort((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2())));
        return possibleChanges.get(0);
    }


    /**
     * Function that checks all requirements for learning (free skillpoint slots, have friends) and checking chance for learning action and eventually, learns something
     *
     * @return GodLearnAction if learning was successful, null otherwise,
     */
    GodLearnAction ConsiderLearningSomething() {
        if (!GodHelper.hasFreeSkillpoints(settings))
            return null;
        if (settings.getKnownGods().isEmpty())
            return null;

        Random rnd = new Random();
        if (rnd.nextInt() % 100 >= settings.getChanceToShareKnowledgePercent())
            return null;

        //At this point we want to learn something, we start with picking a teacher
        List<DFAgentDescription> godDescriptors2 = Arrays.asList(Common.findAgentsInDf(this, God.class));
        int i = 0;
        List<DFAgentDescription> godDescriptors = new ArrayList<>();
        for (var d : godDescriptors2) {
            if (d.getAllLanguages().hasNext()) {
                if (settings.getKnownGods().contains(d.getName().getLocalName()) && d.getAllLanguages().next().toString().equals("1")) {
                    godDescriptors.add(d);
                }
            }

        }


        if (godDescriptors.size() > 0) {
            DFAgentDescription teacherName = godDescriptors.get(rnd.nextInt(godDescriptors.size()));


            Gson _gson = new GsonBuilder().create();

            ACLMessage message = new ACLMessage(ACLMessage.INFORM_REF);
            message.setLanguage("Cyclic");
            message.addReceiver(teacherName.getName());
            message.setOntology("Teach");

            send(message);

            MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
            MessageTemplate ontology = MessageTemplate.MatchOntology("Teach");
            MessageTemplate template = MessageTemplate.and(performative, ontology);
            ACLMessage response = blockingReceive(template);
            if (response == null)
                return null;

            ElementType learnedElement = _gson.fromJson(response.getContent(), ElementType.class);
            settings.learnAbout(learnedElement);

            return new GodLearnAction(settings.getName(), learnedElement);
        } else {
            return null;
        }
    }

    /**
     * Function that responds when God is asked about teaching
     */
    void Teach(ACLMessage message) {
        if (message != null){
        Gson _gson = new GsonBuilder().create();
        ACLMessage response = message.createReply();
        response.setPerformative(ACLMessage.SUBSCRIBE);
        response.setOntology("Teach");
        response.setContent(_gson.toJson(settings.getElementToTeach()));
        send(response);}
    }

    /**
     * Processes turn of god that knows their regions state from the beginning of the turn (creator, Destructor, neutral)
     */
    GodAction ProcessGodTurn(RegionWrapper[] knownRegions) {
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
                    if (maxChange >= BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue1();

                } else { //Actual value > balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if (maxChange <= BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue0();
                }
                return new Triplet<>(entry.getValue0(), element, change);
            }).sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue2()), Math.abs(o1.getValue2()))).collect(Collectors.toList());

            int maxPossibleChange = Math.abs(possibleActions.get(0).getValue2());

            //Consider learning something instead of taking small action
            if (maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningSomething();
                if (learningAction != null)
                    return learningAction;
            }

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            change = finalChange;
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

            //Consider learning something instead of taking small action
            if (maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningSomething();
                if (learningAction != null)
                    return learningAction;
            }

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            change = finalChange;
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

            //Consider learning something instead of taking small action
            if (maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningSomething();
                if (learningAction != null)
                    return learningAction;
            }

            //We want to limit possible actions to only the ones with maximal (the same) change:
            possibleActions = possibleActions.stream().takeWhile(entry -> Math.abs(entry.getValue2()) == maxPossibleChange).collect(Collectors.toList());

            //Now we will take random one of proposed ones
            Random rand = new Random();
            Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
            int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
            change = finalChange;
            return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
        }
        return new GodDoNothingAction(getLocalName());
    }

    void GetSubHolonsSettings(){
        Gson _gson = new GsonBuilder().create();
        //gods = null;
        int goodness = 0;
        for(var godAID : godSubHolons) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setOntology("Initial Information");
            message.addReceiver(godAID);
            message.setLanguage("Cyclic");

            send(message);

            MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate ontology = MessageTemplate.MatchOntology("Information");
            MessageTemplate template = MessageTemplate.and(performative, ontology);

            ACLMessage response = blockingReceive(template);
            if (response.getLanguage().contains("good")) goodness++;
            else if (response.getLanguage().contains("bad")) goodness--;
            this.settings.updateGod(_gson.fromJson(response.getContent(), GodRegionWrapper.class));
        }
        this.settings.setType(GodType.SUPERGOD);
        isGood = goodness >= 0;
    }

    /**
     * Processes turn of god that is a supergod
     */
    void ProcessSubGodTurn(ProtectorTurnInfoWrapper supergodInfo, ACLMessage msg) {
        Gson _gson = new GsonBuilder().create();
        GodAction action = new GodDoNothingAction(getLocalName());
        switch(settings.getType()) {
            case PROTECTOR:
                action = ProcessProtectorTurn(supergodInfo);
                break;
            case CHAOTIC:
                action = ProcessChaoticTurn();
                break;
            default:
                RegionWrapper[] knownRegions = supergodInfo.getRegions().toArray(new RegionWrapper[0]);
                action = ProcessGodTurn(knownRegions);
                break;
        }
        String better = "";
        int value = _gson.fromJson(msg.getProtocol(), Integer.class);
        if (action.actionType().contains("GodInfluence")){
            GodInfluenceRegionAction action1 = (GodInfluenceRegionAction) action;
            List<RegionWrapper> list =  supergodInfo.getRegions().stream().filter(o->o.getName()==action1.getRegionName()).collect(Collectors.toList());
            better = Math.abs(1.2*list.get(0).getElementOfType(action1.getElements().get(0).getType())-575)>value ? "worse":"better";
        }
        if (better.contains("better")){
            Common.removeAgentFromDf(this);
            settings.setSeparate(true);
            Common.registerAgentInDf(this, "1");
            System.out.println(this.getLocalName()+" Leaving supergod");
        }
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology("Subgod");
        response.setContent(better);
        turned = true;
        send(response);
    }


    GodAction CreateSuperGodAction(ProtectorTurnInfoWrapper supergodInfo) throws ClassNotFoundException {
        Gson _gson = new GsonBuilder().create();
        GodAction action = new GodDoNothingAction(getLocalName());
        if (GodHelper.getMaxGodType(godTypes).equals(GodType.PROTECTOR)){
            action = ProcessProtectorTurn(supergodInfo);
        }
        else if (GodHelper.getMaxGodType(godTypes).equals(GodType.CHAOTIC)){
            action = ProcessChaoticTurn();
        }
        else {
            settings.setType(isGood ? GodType.CREATOR : GodType.DESTRUCTOR);
            action = ProcessGodTurn(supergodInfo.getRegions().toArray(new RegionWrapper[0]));
            settings.setType(GodType.SUPERGOD);
        }
        return action;
    }
    /**
     * Processes turn of god that is a supergod
     */
    GodAction ProcessSuperGodTurn(ProtectorTurnInfoWrapper supergodInfo) throws ClassNotFoundException {
        Gson _gson = new GsonBuilder().create();
        int differenceFromBalance = 0;
        GodAction action = CreateSuperGodAction(supergodInfo);
        if (action.actionType().contains("GodInfluenceRegionAction")){
            GodInfluenceRegionAction action1 = (GodInfluenceRegionAction) action;
            List<RegionWrapper> list =  supergodInfo.getRegions().stream().filter(o->o.getName()==action1.getRegionName()).collect(Collectors.toList());
            differenceFromBalance = Math.abs(list.get(0).getElementOfType(action1.getElements().get(0).getType())-575);
        }
        int i = 0;
        while(i < godSubHolons.size()) {
                   AID godAID = godSubHolons.get(i);
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.setOntology("Your Turn (Subgod)");
                    message.addReceiver(godAID);
                    message.setLanguage("Cyclic");
                    message.setContent(_gson.toJson(supergodInfo));
                    message.setProtocol(_gson.toJson(differenceFromBalance));

                    send(message);

                    MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
                    MessageTemplate ontology = MessageTemplate.MatchOntology("Subgod");
                    MessageTemplate template = MessageTemplate.and(performative, ontology);

                    ACLMessage response = blockingReceive(template);
                    i++;
                    if (response.getContent().contains("better")) {
                        List<AID> temp = godSubHolons;
                        List<GodType> temp2 = godTypes;
                        godTypes = new ArrayList<>();
                        godSubHolons = new ArrayList<>();
                        int j = 0;
                        for (var a : temp) {
                            if (a != godAID) {
                                godSubHolons.add(a);
                                godTypes.add(temp2.get(j));
                            }
                            else {
                                message.setOntology("Separated");
                                message.addReceiver(godAID);
                                send(message);
                            }
                            j++;
                        }

                        if (godSubHolons.size() < 1){
                            System.out.println("God killed itself");
                            Common.removeAgentFromDf(this);
                            return new GodDeleteAction();
                        }
                        GetSubHolonsSettings();
                        action = CreateSuperGodAction(supergodInfo);
                        i = 0;
                    }
        }
        return action;
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

        //50% (out of remaining 66, so 33 overall) that god will attempt to learn something
        if (rnd.nextInt() % 100 < 50) {
            var learningAction = ConsiderLearningSomething();
            if (learningAction != null)
                return learningAction;
        }

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
        change = finalValue;
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
            return new GodDoNothingAction(getLocalName());

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

        //Consider learning something instead of taking small action, but he is less likely to do it
        if (maxPossibleChange <= SMALL_CHANGE / 2) {
            var learningAction = ConsiderLearningSomething();
            if (learningAction != null)
                return learningAction;
        }

        //Now we will take random one of proposed ones
        Random rand = new Random();
        Triplet<String, ElementType, Integer> action = possibleActions.get(rand.nextInt(possibleActions.size()));
        int finalChange = GodHelper.finalElementChange(action.getValue2(), action.getValue1(), settings);
        change = finalChange;
        //isGood = GodHelper.checkBalance(finalChange,action.getValue1(), settings);
        return new GodInfluenceRegionAction(getLocalName(), action.getValue0(), Collections.singletonList(action.getValue1()), Collections.singletonList(finalChange));
    }


    /**
     * Reply to question from other god
     */
    public void ProcessQuery(ACLMessage msg) {
        if (msg != null) {
            ACLMessage response = msg.createReply();
            response.addReceiver(msg.getSender());
            response.setSender(this.getAID());
            if (!turned && msg.getOntology().contains(settings.getType().toString()) || msg.getOntology().contains("Protector") || msg.getOntology().contains("Chaotic")) {
                Gson _gson = new GsonBuilder().create();
                this.settings.setSeparate(false);
                response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                response.setOntology("ProcessQuery");
                isHeadOfHolon = false;
                response.setContent(_gson.toJson(settings));
                Common.removeAgentFromDf(this);
                Common.registerAgentInDf(this, "0");
            } else {
                response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                this.settings.setSeparate(true);
                response.setOntology("ProcessQuery");
            }
            send(response);
        }
    }

    /**
     * Reply to accept or reject from other god
     */
    public void ProcessReplyTurn(ACLMessage msg) throws StaleProxyException, IOException {
        this.settings.setSeparate(false);
        if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            Gson _gson = new GsonBuilder().create();
            Common.removeAgentFromDf(this);
            Common.registerAgentInDf(this, "0");
            GodWrapper tmp = (_gson.fromJson(msg.getContent(), GodWrapper.class));
            String name = settings.getName() +" and "+ tmp.getName();
            isHeadOfHolon = false;
            try {
                Random rand = new Random();
                var god1 = this.getContainerController().createNewAgent("Supergod"+Math.abs(rand.nextInt()), "teamwork.agents.God",
                       new Object[]{true, new AID[]{this.getAID(), msg.getSender(),},
                               new GodType[]{this.settings.getType(), tmp.getType()},
                       new GodWrapper[]{this.settings, tmp}});
                this.turned = true;
                god1.start();
            } catch (Exception e) {
                System.err.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
                System.err.println("Couldn't start God agents");
                return;
            }
            System.out.println(settings.getName()+" CREATED SUPERGOD from " +name);
        } else {
            Common.removeAgentFromDf(this);
            Common.registerAgentInDf(this, "1");
        }
    }


    /**
     * Reply to accept or reject from other god (as a supergod)
     */
    public void ProcessReplyTurnSuperGod(ACLMessage msg) throws StaleProxyException, IOException {
        if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
               Gson _gson = new GsonBuilder().create();
               GodRegionWrapper tmp = (_gson.fromJson(msg.getContent(), GodRegionWrapper.class));
               godSubHolons.add(msg.getSender());
               System.out.println(settings.getName()+" UPDATED SUPERGOD:" + settings.getName());
        }
    }


    /**
     * Interprets what kind of information god got, calls appropriate function and responds
     */
    private void processTurn(ACLMessage msg) throws ClassNotFoundException {
        Gson _gson = new GsonBuilder().create();
        GodAction action = new GodDoNothingAction(getLocalName());
        boolean sep = false;

        if (settings.getChanceToCooperatePercent() > 50) {
            ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF);
            message.setLanguage("Cyclic");
            message.setOntology(settings.getType().toString());
            List<DFAgentDescription> godDescriptors2 = Arrays.asList(Common.findAgentsInDf(this, God.class));
            int i = 0;
            List<DFAgentDescription> godDescriptors = new ArrayList<>();
            for (var d : godDescriptors2) {
                if (d.getAllLanguages().hasNext()) {
                    if (settings.getKnownGods().contains(d.getName().getLocalName()) && d.getAllLanguages().next().toString().equals("1")) {
                        godDescriptors.add(d);
                    }
                }

            }


            if (godDescriptors.size() > 0) {
                Random rnd = new Random();
                message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
                send(message);
                MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                MessageTemplate ontology = MessageTemplate.MatchOntology("ProcessQuery");

                ACLMessage msg3 = blockingReceive(ontology);

                if (msg3 != null) {
                    if (msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        try {
                            if (!settings.getType().equals(GodType.SUPERGOD)){
                            ProcessReplyTurn(msg3);
                            isHeadOfHolon = false;}
                            else {
                                ProcessReplyTurnSuperGod(msg3);
                            }
                        } catch (StaleProxyException | IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sep = true;
                        isHeadOfHolon = false;
                    }

                }
            }
        }
        if (settings.getSeparate() || (settings.getType().equals(GodType.SUPERGOD))) {
            switch (msg.getOntology()) {
                case "Your Turn (God)":
                    RegionWrapper[] knownRegions = _gson.fromJson(msg.getContent(), RegionWrapper[].class);
                    action = ProcessGodTurn(knownRegions);
                    break;
                case "Your Turn (Chaotic)":
                    action = ProcessChaoticTurn();
                    break;
                case "Your Turn (Protector)":
                    ProtectorTurnInfoWrapper protectorInfo = _gson.fromJson(msg.getContent(), ProtectorTurnInfoWrapper.class);
                    action = ProcessProtectorTurn(protectorInfo);
                    break;
                case "Your Turn (Supergod)":
                    ProtectorTurnInfoWrapper supergodInfo = _gson.fromJson(msg.getContent(), ProtectorTurnInfoWrapper.class);
                    action = ProcessSuperGodTurn(supergodInfo);
                    break;
                default:
                    action = new GodDoNothingAction(getLocalName());
                    break;
            }
        }
        if(sep) settings.setSeparate(sep);
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology(action.actionType());
        response.setContent(_gson.toJson(action));
        turned = true;
        send(response);
    }

    CyclicBehaviour processMessage = new CyclicBehaviour(this) {
        @Override
        public void action() {
            Gson _gson = new GsonBuilder().create();
            MessageTemplate tmp = MessageTemplate.MatchLanguage("Cyclic");
            ACLMessage msg = blockingReceive(tmp);
            Random rnd = new Random();
            if (msg != null) {
                //If message is not from time and god doesn't know the sender, add sender to known gods
                if (!msg.getSender().getLocalName().equals("Time")) {
                    if (settings.getKnownGods().stream().noneMatch(name -> name.equals(msg.getSender().getLocalName()))) {
                        settings.getKnownGods().add(msg.getSender().getLocalName());
                    }

                }
                switch (msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        if (msg.getOntology().equals("Initial Information"))//&& settings.getSeparate())
                        {
                            turned = false;
                            if (isHeadOfHolon && !settings.getSeparate()) {
                                GetSubHolonsSettings();
                            }
                            if (!settings.getType().equals(GodType.CREATOR) && !settings.getType().equals(GodType.DESTRUCTOR)) {
                                isGood = rnd.nextDouble() < 0.5;
                            }
                            Common.responseWithInformationAbout(getAgent(), settings, msg, isGood ? "good" : "bad");
                        }
                        break;
                    case ACLMessage.INFORM_REF:
                        if (msg.getOntology().equals("Teach"))
                            Teach(msg);
                        break;
                    case ACLMessage.DISCONFIRM:
                        if (msg.getOntology().contains("Your Turn")) {
                            Common.removeAgentFromDf(this.myAgent);
                        }
                        break;
                    case ACLMessage.INFORM:
                        if (msg.getOntology().startsWith("Your Turn")) {
                            if (msg.getOntology().contains("Subgod"))
                            {
                                ProtectorTurnInfoWrapper subgodInfo = _gson.fromJson(msg.getContent(), ProtectorTurnInfoWrapper.class);
                                ProcessSubGodTurn(subgodInfo, msg);
                            }
                            else{
                                try {
                                    processTurn(msg);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (msg.getOntology().contains("Separated")) {
                                settings.setSeparate(true);
                                Common.removeAgentFromDf(this.myAgent);
                                Common.registerAgentInDf(this.myAgent, "1");
                        }
                        break;
                    case ACLMessage.QUERY_IF:
                        if (settings.getSeparate() && !msg.getSender().getLocalName().equals("Time")) {
                            ProcessQuery(msg);
                        }
                        break;
                    default:
                        break;
                }
            }

            block();
        }
    };


}
