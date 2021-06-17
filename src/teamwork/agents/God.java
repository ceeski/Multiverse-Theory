package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import teamwork.agents.actions.*;
import teamwork.agents.enums.ElementType;
import teamwork.agents.enums.GodType;
import teamwork.agents.utility.GodHelper;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.RegionWrapper;
import teamwork.agents.wrappers.ProtectorTurnInfoWrapper;
import teamwork.agents.utility.Common;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class God extends Agent {
    private GodWrapper settings;
    private static final int BALANCE = 575; //Balance is slightly higher to represent the fact that people will use resources
    private static final int SMALL_CHANGE = 45; //If change is small (below this number) - god might consider learning something
    private static final int MAX = 1000;
    private static final int MIN = 0;

    //Those are meeting states
    private ArrayList<String> meetingAttendants;
    private int meetingResponses;
    private String meetingOriginator;

    @Override
    protected void setup() {
        settings = (GodWrapper)getArguments()[0];
        Common.registerAgentInDf(this);
        addBehaviour(processMessage);
    }

    /**
     * If God has a spare time, they will prefer meeting over doing something. If no one would come to the meeting - they ask about learning (if they can - they have free skill points).
     * @return GodAction if action was performed, null otherwise
     */
    GodAction ConsiderLearningAndMeeting() {
        Random rnd = new Random();
        Gson _gson = new GsonBuilder().create();

        ACLMessage message;
        MessageTemplate performative;
        MessageTemplate ontology;
        MessageTemplate template;
        ACLMessage response;

        GodAction finalAction = null;

        if(settings.getKnownGods().isEmpty())
            return null;

        if(settings.getKnownGods().size() == 1 && settings.getKnownGods().get(0).equals(settings.getName()))
            return null;

        String usedFriend;
        do {
            usedFriend = settings.getKnownGods().get(rnd.nextInt(settings.getKnownGods().size()));
        } while(usedFriend.equals(settings.getName()));

        //At first we ask about meeting - it is preferred over learning
        message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(usedFriend, AID.ISLOCALNAME));
        message.setOntology("Organize Meeting");

        send(message);

        performative = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        ontology = MessageTemplate.MatchOntology("Organize Meeting");
        template = MessageTemplate.and(performative, ontology);
        response = blockingReceive(template);
        if(response == null)
            return null;

        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> attendants = _gson.fromJson(response.getContent(), listType);

        if(attendants == null)
            return null;

        finalAction = new GodMeetOthersAction(settings.getName(), usedFriend, attendants);

        if(attendants.size() > 0)
            return finalAction;

        //If no other god came - we can ask about learning in private (from the same god!)
        if(rnd.nextInt() % 100 <= settings.getChanceToShareKnowledgePercent())
            return finalAction; //If we do not learn - we return meeting

        if(!GodHelper.hasFreeSkillpoints(settings))
            return finalAction; //If we cannot learn - we also return a meeting

        //At this point we want to learn something
        message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(usedFriend, AID.ISLOCALNAME));
        message.setOntology("Teach");

        send(message);

        performative = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        ontology = MessageTemplate.MatchOntology("Teach");
        template = MessageTemplate.and(performative, ontology);
        response = blockingReceive(template);
        if(response == null)
            return null;

        ElementType learnedElement = _gson.fromJson(response.getContent(), ElementType.class);
        settings.learnAbout(learnedElement);

        return new GodLearnAction(settings.getName(), learnedElement);
    }

    /**
     * Function that responds when God is asked about teaching
     */
    void Teach(ACLMessage message) {
        Gson _gson = new GsonBuilder().create();

        ACLMessage response = message.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology("Teach");
        response.setContent(_gson.toJson(settings.getElementToTeach()));
        send(response);
    }

    /**
     * When god is asked to organize the meeting - this function sends invitations
     */
    void OrganizeMeeting(ACLMessage message) {
        meetingAttendants = new ArrayList<String>();
        meetingResponses = 0;
        meetingOriginator = message.getSender().getLocalName();


        ACLMessage proposeMessage = new ACLMessage(ACLMessage.PROPOSE);
        proposeMessage.setOntology("Attend Meeting");
        proposeMessage.setContent(meetingOriginator);
        for(var godName : settings.getKnownGods()) {
            if(godName.equals(meetingOriginator))
                continue; //We do not ask the originator
            proposeMessage.addReceiver(new AID(godName, AID.ISLOCALNAME));
        }
        send(proposeMessage);
    }

    /**
     * This functions handles responses for meeting invitations and eventually send response to God that asked to organize meeting
     */
    void meetingAttendanceResponse(ACLMessage message, boolean willCome) {
        meetingResponses++;

        if(willCome)
            meetingAttendants.add(message.getSender().getLocalName());

        if(meetingResponses == settings.getKnownGods().size() - 1) { //we exclude the originator hence - 1
            Gson _gson = new GsonBuilder().create();
            ACLMessage originatorResponse = new ACLMessage(ACLMessage.CONFIRM);
            originatorResponse.addReceiver(new AID(meetingOriginator, AID.ISLOCALNAME));
            originatorResponse.setOntology("Organize Meeting");
            originatorResponse.setContent(_gson.toJson(meetingAttendants));
            send(originatorResponse);
        }
    }

    /**
     * Function that responds when God is asked about attending a meeting
     */
    void AttendMeetingResponse(ACLMessage message) {
        String originator = message.getContent();

        ACLMessage response = message.createReply();
        response.setOntology("Attend Meeting");

        Random rnd = new Random();
        if(rnd.nextInt() % 100 <= settings.getChanceToShareKnowledgePercent()) {
            response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            //If we accept the meeting and we do not know the originator, we will know them
            if(settings.getKnownGods().stream().noneMatch(godName -> godName.equals(originator)))
                settings.getKnownGods().add(originator);
        }
        else
            response.setPerformative(ACLMessage.REJECT_PROPOSAL);

        send(response);
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

            //Consider learning something instead of taking small action
            if(maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningAndMeeting();
                if(learningAction != null)
                    return learningAction;
            }

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

            //Consider learning something instead of taking small action
            if(maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningAndMeeting();
                if(learningAction != null)
                    return learningAction;
            }

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

            //Consider learning something instead of taking small action
            if(maxPossibleChange <= SMALL_CHANGE) {
                var learningAction = ConsiderLearningAndMeeting();
                if(learningAction != null)
                    return learningAction;
            }

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

        //50% (out of remaining 66, so 33 overall) that god will attempt to learn something
        if(rnd.nextInt() % 100 < 50) {
            var learningAction = ConsiderLearningAndMeeting();
            if(learningAction != null)
                return learningAction;
        }

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

        if(changes.size() == 0)
            return new GodDoNothingAction();

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

        //Consider learning something instead of taking small action, but he is less likely to do it
        if(maxPossibleChange <= SMALL_CHANGE / 2) {
            var learningAction = ConsiderLearningAndMeeting();
            if(learningAction != null)
                return learningAction;
        }

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
                        switch (msg.getOntology()) {
                            case "Initial Information":
                                Common.responseWithInformationAbout(getAgent(), settings, msg);
                                break;
                            case "Teach":
                                Teach(msg);
                                break;
                            case "Organize Meeting":
                                OrganizeMeeting(msg);
                                break;
                        }
                        break;
                    case ACLMessage.INFORM:
                        if(msg.getOntology().startsWith("Your Turn"))
                            processTurn(msg);
                        break;
                    case ACLMessage.PROPOSE:
                        if(msg.getOntology().startsWith("Attend Meeting"))
                            AttendMeetingResponse(msg);
                        break;
                    case ACLMessage.ACCEPT_PROPOSAL:
                    case ACLMessage.REJECT_PROPOSAL:
                        if(msg.getOntology().startsWith("Attend Meeting"))
                            meetingAttendanceResponse(msg, msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL);
                        break;
                    default:
                        break;
                }
            }

        }
    };

}
