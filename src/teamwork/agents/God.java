package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import teamwork.agents.enums.ElementType;
import teamwork.agents.enums.GodType;
import teamwork.agents.utility.GodHelper;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.RegionWrapper;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodDoNothingAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.utility.Common;

import java.util.*;
import java.util.stream.Collectors;

public class God extends Agent {
    private GodWrapper settings;
    private static final int BALANCE = 575; //Balance is slightly higher to represent the fact that people will use resources
    private static final int MAX = 1000;
    private static final int MIN = 0;

    @Override
    protected void setup() {
        settings = (GodWrapper)getArguments()[0];
        Common.registerAgentInDf(this);
        addBehaviour(processMessage);
    }

    /**
     * Processes turn of god that knows their regions state from the beginning of the turn (creator, Destructor, neutral)
     */
    GodAction ProcessGodTurn(RegionWrapper[] knownRegions) {
        var possibilities = GodHelper.getPossibleElementChanges(settings);

        //Get list of all regions and resources that can be influenced in a way
        //Triplet: (region name, element, actual resource value)
        List< Triplet<String, ElementType, Integer> > regionsElementsScores = new ArrayList<>();

        for(var region : knownRegions) {
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

        if(settings.getType().equals(GodType.CREATOR)){
            //Creator calculates all possible positive changes: if value < balance it tries to add as much as they can to reach balance
            // if value > balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List< Triplet<String, ElementType, Integer> > possibleActions = regionsElementsScores.stream().map(entry -> {
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
        else if(settings.getType().equals(GodType.DESTRUCTOR)){
            //Destructor calculates all possible negative changes: if value > balance it tries to add as much as they can to reach balance
            // if value < balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List< Triplet<String, ElementType, Integer> > possibleActions = regionsElementsScores.stream().map(entry -> {
                ElementType element = entry.getValue1();
                int resourceValue = entry.getValue2();
                int change = 0;

                if(resourceValue <= BALANCE) { //Actual value <= balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if(maxChange < MIN)
                        change = MIN - resourceValue;
                    else
                        change = possibilities.get(element).getValue0();
                } else { //Actual value > balance, we want to add
                    int maxChange = resourceValue + possibilities.get(element).getValue1();
                    if(maxChange > MAX)
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
        }
        else if(settings.getType().equals(GodType.NEUTRAL)) {
            //NEUTRAL works exactly the same way as creator (code is repeated so it can be easier to change separately), but all possibilities of change are divided by 2, so instead of [-180, 250] it would be [-90, 125]
            for(var element : (ElementType[])possibilities.keySet().toArray()) {
                possibilities.get(element).setAt0(possibilities.get(element).getValue0()/2);
                possibilities.get(element).setAt1(possibilities.get(element).getValue1()/2);
            }

            //Creator calculates all possible positive changes: if value < balance it tries to add as much as they can to reach balance
            // if value > balance, it tries to subtract

            //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
            List< Triplet<String, ElementType, Integer> > possibleActions = regionsElementsScores.stream().map(entry -> {
                ElementType element = entry.getValue1();
                int resourceValue = entry.getValue2();
                int change = 0;

                if(resourceValue <= BALANCE) { //Actual value <= balance, we want to add
                    int maxChange = resourceValue + possibilities.get(element).getValue1();
                    if(maxChange > BALANCE)
                        change = BALANCE - resourceValue;
                    else
                        change = possibilities.get(element).getValue1();
                } else { //Actual value > balance, we want to subtract
                    int maxChange = resourceValue + possibilities.get(element).getValue0(); //Note - this number will be negative
                    if(maxChange < BALANCE)
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
        if(willSkipTurn)
            return new GodDoNothingAction(getLocalName());

        //Get all possible region influences
        var possibilities = GodHelper.getPossibleElementChanges(settings);
        List<Triplet<ElementType, Integer, Integer>> possibleChanges = new ArrayList<>();

        for(var element : ElementType.AllTypes()) {
            Pair<Integer, Integer> bounds = possibilities.get(element);
            if(bounds.getValue0().equals(bounds.getValue1()) && bounds.getValue0() == 0)
                continue;
            possibleChanges.add(new Triplet<>(element, bounds.getValue0(), bounds.getValue1()));
        }

        if(possibleChanges.size() == 0)
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
        List< Triplet<String, ElementType, Integer> > changes = new ArrayList<>();
        for(var change : info.getPreviousActions())
            for(int i = 0; i < change.getElements().size(); i++)
                changes.add(new Triplet<>(change.getRegionName(), change.getElements().get(i), change.getValues().get(i)));

        //Triplet: (region name, element, change) - sorted by change with higher change at the beginning
        List< Triplet<String, ElementType, Integer> > possibleActions = changes.stream().map(entry -> {
            ElementType element = entry.getValue1();
            int resourceChange = entry.getValue2();
            int change = 0;

            if(resourceChange <= 0)  { //Negative change, so we want to add
                if(possibilities.get(element).getValue1() + resourceChange < 0)
                    change = possibilities.get(element).getValue1();
                else
                    change = -resourceChange;
            } else { //Positive value, so we want to subtract
                if(possibilities.get(element).getValue0() + resourceChange > 0)
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
                //If message is not from time and god don't know the sender, add sender to known gods
                if(!msg.getSender().getLocalName().equals("Time")) {
                    if(settings.getKnownGods().stream().noneMatch(name -> name.equals(msg.getSender().getLocalName()))) {
                        settings.getKnownGods().add(msg.getSender().getLocalName());
                    }
                }

                switch(msg.getPerformative()) {
                    case ACLMessage.REQUEST:
                        if(msg.getOntology().equals("Initial Information"))
                            Common.responseWithInformationAbout(getAgent(), settings, msg);
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
