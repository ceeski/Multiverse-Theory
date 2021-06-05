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

public class God extends Agent {
    private GodWrapper settings;
    private static final int BALANCE = 500;
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
        int numRegions = knownRegions.length;
        //CREATOR wants to bring the most needy resource to BALANCE


        var possibilities = GodHelper.getPossibleElementChanges(settings);
        System.out.println(settings.getName() + ", " + settings.getType());
        if(settings.getType().equals(GodType.CREATOR)){
            //CREATOR wants to bring the most needy resource to BALANCE

            //sort all regions' resource scores in descending order (resource's score is the distance between the resource's value and the BALANCE value, 500)
            List<Triplet<String, ElementType, Integer>> regionsElementsScores = new ArrayList<>();
            for(int i = 0; i < numRegions; i++){
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.FIRE,knownRegions[i].getHeatResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.WATER,knownRegions[i].getWaterResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LIGHT,knownRegions[i].getLightResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.DARKNESS,knownRegions[i].getDarknessResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.EARTH,knownRegions[i].getEarthResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AIR,knownRegions[i].getAirResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.KNOWLEDGE,knownRegions[i].getKnowledgeResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AMUSEMENT,knownRegions[i].getAmusementResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LOVE,knownRegions[i].getLoveResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.RESTRAINT,knownRegions[i].getRestraintResource()));
            }

            //sort all regions' resource scores in descending order (resource's score is the distance between the resource's value and the BALANCE value, 500)
            Collections.sort(regionsElementsScores, new Comparator<Triplet<String, ElementType, Integer>>() {
                @Override
                public int compare(Triplet<String, ElementType, Integer> o1, Triplet<String, ElementType, Integer> o2) {
                    return ((Integer)Math.abs(o2.getValue2() - BALANCE)).compareTo((Integer)Math.abs(o1.getValue2() - BALANCE));
                }
            });

//            for(var t: regionsElementsScores){
//                System.out.println(t.getValue0()+" - " + t.getValue1() + " - " + t.getValue2());
//            }



            //now let's see if the God is capable of bringing that value to BALANCE

            for(var entry: regionsElementsScores){
                var possibleChange = possibilities.get(entry.getValue1());
                System.out.println(entry.getValue1() + ", possible change: [" + possibleChange.getValue0() + ", " + possibleChange.getValue1() + "]");
                //if the God is capable to do so, and if the resource is not balanced already, the god applies the necessary change
                if(entry.getValue2() + possibleChange.getValue0() <= BALANCE && entry.getValue2() + possibleChange.getValue1() >= BALANCE && entry.getValue2() != BALANCE){
                    if(entry.getValue2() < BALANCE){
                        var element = entry.getValue1();
                        int finalValue = GodHelper.finalElementChange(BALANCE - entry.getValue2(), element, settings);
                        return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                    }
                    else {
                        var element = entry.getValue1();
                        int finalValue = GodHelper.finalElementChange(entry.getValue2() - BALANCE, element, settings);
                        return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                    }
                }
//                if(possibleChange.getValue0() <= 0 && possibleChange.getValue1() >= 0 && entry.getValue2() != 0){
//                    var element = entry.getValue1();
//                    int finalValue = GodHelper.finalElementChange(BALANCE, element, settings);
//                    return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
//                }
                //otherwise, god looks at the next needy resource
            }

            //otherwise, the god does nothing. It means that either god is not capable of balancing the resource, or all resources in all regions are balanced already
        }
        else if(settings.getType().equals(GodType.DESTRUCTOR)){
            //DESTRUCTOR wants to unbalance the most balanced resource

            List<Triplet<String, ElementType, Integer>> regionsElementsScores = new ArrayList<>();
            for(int i = 0; i < numRegions; i++){
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.FIRE,knownRegions[i].getHeatResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.WATER,knownRegions[i].getWaterResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LIGHT,knownRegions[i].getLightResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.DARKNESS,knownRegions[i].getDarknessResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.EARTH,knownRegions[i].getEarthResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AIR,knownRegions[i].getAirResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.KNOWLEDGE,knownRegions[i].getKnowledgeResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AMUSEMENT,knownRegions[i].getAmusementResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LOVE,knownRegions[i].getLoveResource()));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.RESTRAINT,knownRegions[i].getRestraintResource()));
            }

            //sort all regions' resource scores in ascending order (resource's score is the distance between the resource's value and the BALANCE value, 500)
            Collections.sort(regionsElementsScores, new Comparator<Triplet<String, ElementType, Integer>>() {
                @Override
                public int compare(Triplet<String, ElementType, Integer> o1, Triplet<String, ElementType, Integer> o2) {
                    return ((Integer)Math.abs(o1.getValue2() - BALANCE)).compareTo((Integer)Math.abs(o2.getValue2() - BALANCE));
                }
            });

//            for(var t: regionsElementsScores){
//                System.out.println(t.getValue0()+" - " + t.getValue1() + " - " + t.getValue2());
//            }

            for(var entry: regionsElementsScores){
                var possibleChange = possibilities.get(entry.getValue1());
                System.out.println(entry.getValue1() + ", possible change: [" + possibleChange.getValue0() + ", " + possibleChange.getValue1() + "]");
                //if the resource is to the right of BALANCE (larger than BALANCE), and god is capable to increase it even more
                if(entry.getValue2() >= BALANCE && possibleChange.getValue1() > 0){
                    var element = entry.getValue1();
                    int finalValue = GodHelper.finalElementChange(possibleChange.getValue1(), element, settings);
                    return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                }
                //if the resource is to the left of BALANCE (smaller than BALANCE), and god is capable to reduce it even more
                else if(entry.getValue2() < BALANCE && possibleChange.getValue0() < 0){
                    var element = entry.getValue1();
                    int finalValue = GodHelper.finalElementChange(possibleChange.getValue0(), element, settings);
                    return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                }
                //if he cannot unbalance the resource, the god looks at the next resource
            }
        }
        else if(settings.getType().equals(GodType.NEUTRAL)){
            //NEUTRAL wants to bring the least needy resource to BALANCE

            //sort all regions' resource scores in ascending order (resource's score is the distance between the resource's value and the BALANCE value, 500)
            List<Triplet<String, ElementType, Integer>> regionsElementsScores = new ArrayList<>();
            for(int i = 0; i < numRegions; i++){
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.FIRE,Math.abs(knownRegions[i].getHeatResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.WATER,Math.abs(knownRegions[i].getWaterResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LIGHT,Math.abs(knownRegions[i].getLightResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.DARKNESS,Math.abs(knownRegions[i].getDarknessResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.EARTH,Math.abs(knownRegions[i].getEarthResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AIR,Math.abs(knownRegions[i].getAirResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.KNOWLEDGE,Math.abs(knownRegions[i].getKnowledgeResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.AMUSEMENT,Math.abs(knownRegions[i].getAmusementResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.LOVE,Math.abs(knownRegions[i].getLoveResource() - BALANCE)));
                regionsElementsScores.add(new Triplet<>(knownRegions[i].getName(),ElementType.RESTRAINT,Math.abs(knownRegions[i].getRestraintResource() - BALANCE)));
            }

            //sort all regions' resource scores in ascending order (resource's score is the distance between the resource's value and the BALANCE value, 500)
            Collections.sort(regionsElementsScores, new Comparator<Triplet<String, ElementType, Integer>>() {
                @Override
                public int compare(Triplet<String, ElementType, Integer> o1, Triplet<String, ElementType, Integer> o2) {
                    return o2.getValue2().compareTo(o1.getValue2());
                }
            });

//            for(var t: regionsElementsScores){
//                System.out.println(t.getValue0()+" - " + t.getValue1() + " - " + t.getValue2());
//            }
            //now let's see if the God is capable of bringing that value to BALANCE

            for(var entry: regionsElementsScores){
                var possibleChange = possibilities.get(entry.getValue1());
                //System.out.println(entry.getValue1() + ", possible change: [" + possibleChange.getValue0() + ", " + possibleChange.getValue1() + "]");

                //if the God is capable to do so, and if the resource is not balanced already, the god applies the necessary change
                if(entry.getValue2() + possibleChange.getValue0() <= BALANCE && entry.getValue2() + possibleChange.getValue1() >= BALANCE && entry.getValue2() != BALANCE){
                    if(entry.getValue2() < BALANCE){
                        var element = entry.getValue1();
                        int finalValue = GodHelper.finalElementChange(BALANCE - entry.getValue2(), element, settings);
                        return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                    }
                    else {
                        var element = entry.getValue1();
                        int finalValue = GodHelper.finalElementChange(entry.getValue2() - BALANCE, element, settings);
                        return new GodInfluenceRegionAction(getLocalName(), entry.getValue0(), Collections.singletonList(element), Collections.singletonList(finalValue));
                    }
                }
                //otherwise, god looks at the next needy resource
            }
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
