package teamwork.agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.StaleProxyException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import teamwork.agents.actions.GodAction;
import teamwork.agents.actions.GodDoNothingAction;
import teamwork.agents.actions.GodInfluenceRegionAction;
import teamwork.agents.actions.GodLearnAction;
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
    public List<RegionWrapper> regionsFromCreatorOrDestroyer;
    public static boolean isHeadOfHolon = false;
    public static List<GodRegionWrapper> gods;
    public static List<AID> godSubHolons;
    private boolean turned = false;
    private Agent myAgent;

    @Override
    protected void setup() {
        if (getArguments().length > 3){
            boolean isHeadOfHolon = (boolean)getArguments()[0];
            List<GodRegionWrapper> gods = (List<GodRegionWrapper> )getArguments()[1];
            List<RegionWrapper> regionsFromCreatorOrDestroyer = new ArrayList<>();
            for (var p : gods)
            { for (var l : p.getRegions()){
                regionsFromCreatorOrDestroyer.add((RegionWrapper)l );}}
            List<AID> godSubHolons = Arrays.asList((AID[])getArguments()[2]);
            settings = gods.get(0).getGod();
            settings.setName(this.getLocalName());
            settings.setSeparate(false);
            //ACLMessage msg = (ACLMessage) getArguments()[3];
            Location location = (Location)getArguments()[3];
            /*try {
                location = (Location)msg.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }*/
            /*if(location != null)
                this.myAgent.doMove(location);*/
        }
        else if (getArguments().length == 1){
            settings = (GodWrapper) getArguments()[0];
            settings.setSeparate(true);
            isHeadOfHolon = false;
            gods = new ArrayList<>();
            regionsFromCreatorOrDestroyer = new ArrayList<>();
            godSubHolons =  new ArrayList<>();
        }
        Common.registerAgentInDf(this, "1");
        addBehaviour(processMessage);
    }


    /**
     * Function that checks all requirements for learning (free skillpoint slots, have friends) and checking chance for learning action and eventually, learns something
     * @return GodLearnAction if learning was successful, null otherwise,
     */
    GodLearnAction ConsiderLearningSomething() {
        if(!GodHelper.hasFreeSkillpoints(settings))
            return null;
        if(settings.getKnownGods().isEmpty())
            return null;

        Random rnd = new Random();
        if(rnd.nextInt() % 100 >= settings.getChanceToShareKnowledgePercent())
            return null;

        //At this point we want to learn something, we start with picking a teacher
        String teacherName = settings.getKnownGods().get(rnd.nextInt(settings.getKnownGods().size()));

        Gson _gson = new GsonBuilder().create();

        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(teacherName, AID.ISLOCALNAME));
        message.setOntology("Teach");

        send(message);

        MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
        MessageTemplate ontology = MessageTemplate.MatchOntology("Teach");
        MessageTemplate template = MessageTemplate.and(performative, ontology);
        ACLMessage response = blockingReceive(template);
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
                var learningAction = ConsiderLearningSomething();
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
                var learningAction = ConsiderLearningSomething();
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
                var learningAction = ConsiderLearningSomething();
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
            var learningAction = ConsiderLearningSomething();
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
            var learningAction = ConsiderLearningSomething();
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
     * Reply to question from other god
     */
    public void ProcessQuery(ACLMessage msg) {
        if (msg != null) {
            ACLMessage response = msg.createReply();
            response.addReceiver(msg.getSender());
            response.setSender(this.getAID());
            if(!turned && msg.getOntology().contains(settings.getType().toString()) || msg.getOntology().contains("Protector")|| msg.getOntology().contains("Chaotic")) {
                Gson _gson = new GsonBuilder().create();
                this.settings.setSeparate(false);
                response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                response.setOntology("ProcessQuery");
                response.setContent(_gson.toJson(new GodRegionWrapper(settings, regionsFromCreatorOrDestroyer)));
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
        if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
            Common.removeAgentFromDf(this);
            this.settings.setSeparate(false);
            Common.registerAgentInDf(this, "0");
            Gson _gson = new GsonBuilder().create();
            GodRegionWrapper tmp = (_gson.fromJson(msg.getContent(), GodRegionWrapper.class));
            List<GodRegionWrapper> list = new ArrayList<>();
            list.add(tmp);
            list.add(new GodRegionWrapper(settings, regionsFromCreatorOrDestroyer));
            var regionsProfile = new ProfileImpl();
            var runtime = jade.core.Runtime.instance();
            regionsProfile.setParameter(Profile.CONTAINER_NAME, settings.getName() + tmp.getGod().getName());
            regionsProfile.setParameter(Profile.MAIN_HOST, "localhost");
            var regionsContainerController  = runtime.createAgentContainer(regionsProfile);
            ACLMessage response2 = new ACLMessage(ACLMessage.INFORM);
            jade.core.Location location = here();
            //response2.setContentObject(location);
            /*
            */

            try {
                var god1 = this.getContainerController().createNewAgent(settings.getName() + tmp.getGod().getName(), "teamwork.agents.God",
                        new Object[]{true,list,new AID[]{this.getAID(), msg.getSender(),},location});
                //var god = regionsContainerController.createNewAgent(settings.getName() + tmp.getGod().getName(), "teamwork.agents.God",
                  //      new Object[]{true, list, new AID[]{this.getAID(), msg.getSender(),}, location});
                //god.start();
                god1.start();
            }
            catch (Exception e) {
                System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
                System.out.println("Couldn't start God agents");
                runtime.shutDown();
                return;
            }
            //this.myAgent.doMove(location);
            System.out.println("CREATED SUPERGOD:" + settings.getName() + tmp.getGod().getName());
        }
        else {
            Common.removeAgentFromDf(this);
            //this.settings.setSeparate(true);
            Common.registerAgentInDf(this, "1");
        }
    }


    /**
     * Interprets what kind of information god got, calls appropriate function and responds
     */
    private void processTurn(ACLMessage msg) {
        Gson _gson = new GsonBuilder().create();
        GodAction action;
        boolean sep = true;

        System.out.println(settings.getName()+": start");
        if (settings.getChanceToCooperatePercent() > 50) {
            ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF);
            message.setLanguage("Cyclic");
            message.setOntology(settings.getType().toString());
            List<DFAgentDescription> godDescriptors2 = Arrays.asList(Common.findAgentsInDf(this, God.class));
            int i = 0;
            List<DFAgentDescription> godDescriptors = new ArrayList<>();
            for (var d : godDescriptors2) {

                if (d.getAllLanguages().hasNext())
                {if (settings.getKnownGods().contains(d.getName().getLocalName())&& d.getAllLanguages().next().toString().equals("1")) {
                   // System.out.println(d.getAllLanguages().next());
                    godDescriptors.add(d);
                }}

            }

            if (godDescriptors.size()>0) {
                Random rnd = new Random();
                message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
                send(message);
                MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                MessageTemplate ontology = MessageTemplate.MatchOntology("ProcessQuery");

                ACLMessage msg3 = blockingReceive(ontology);

                if (msg3 != null) {
                    System.out.println(msg3.getSender().getLocalName()+ " to " + settings.getName() + " " + msg3.getPerformative() + ": " + msg3.getOntology().toString());
                    if (msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        try {
                            ProcessReplyTurn(msg3);
                            sep = false;
                        } catch (StaleProxyException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        sep = true;
                    }

                }
            }
        }
        System.out.println(settings.getName()+": end");
        if (settings.getSeparate()){
        switch(msg.getOntology()) {
            case "Your Turn (God)":
                RegionWrapper[] knownRegions = _gson.fromJson(msg.getContent(), RegionWrapper[].class);
                regionsFromCreatorOrDestroyer = new ArrayList<>();
                for(var kr : knownRegions)
                    regionsFromCreatorOrDestroyer.add(kr);
                action = ProcessGodTurn(knownRegions);
                break;
            case "Your Turn (Chaotic)":
                action = ProcessChaoticTurn();
                break;
            case "Your Turn (Protector)":
                ProtectorTurnInfoWrapper protectorInfo = _gson.fromJson(msg.getContent(), ProtectorTurnInfoWrapper.class);
                action = ProcessProtectorTurn(protectorInfo);
                break;
            default:
                action = new GodDoNothingAction(getLocalName());
                break;
        }}
        else{
            action = new GodDoNothingAction(getLocalName());
        }
        settings.setSeparate(sep);
        turned = true;
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.CONFIRM);
        response.setOntology(action.actionType());
        response.setContent(_gson.toJson(action));
        send(response);
    }

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
                        {turned = false;
                            Common.responseWithInformationAbout(getAgent(), settings, msg);}
                        else if(msg.getOntology().equals("Teach"))
                            Teach(msg);
                        break;
                    case ACLMessage.INFORM:
                        if (msg.getOntology().startsWith("Your Turn")) {
                            System.out.println(settings.getName()+" got message from Time");
                            processTurn(msg);
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
