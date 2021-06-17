package teamwork.agents.utility;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import teamwork.agents.God;
import teamwork.agents.wrappers.GodWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Sequential extends SequentialBehaviour {
    ACLMessage msg, reply ;
    Agent a;
    GodWrapper settings;
    String     ConvID ;
    public Sequential(Agent myAgent, GodWrapper settings, ACLMessage msg)
    {
        a = a;
        settings = settings;
        this.msg = msg;
        ConvID = msg.getConversationId();
    }
    public void onStart()
    {
        addSubBehaviour(new OneShotBehaviour(a) {
            @Override
            public void action() {
                System.out.println(settings.getName()+": start");
                if (settings.getChanceToCooperatePercent() > 50 && settings.getSeparate()) {
                    ACLMessage message = new ACLMessage();
                    message.setPerformative(ACLMessage.QUERY_IF);
                    message.setOntology("good");
                    List<DFAgentDescription> godDescriptors2 = Arrays.asList(Common.findAgentsInDf(this.myAgent, God.class));//(DFAgentDescription[]) Arrays.stream(Common.findAgentsInDf(this.getAgent(), God.class)).filter(i -> settings.getKnownGods().contains(i.getName())).toArray();
                    //System.out.println(godDescriptors);
                    int i = 0;
                    List<DFAgentDescription> godDescriptors = new ArrayList<>();
                    for (var d : godDescriptors2) {
                        if (settings.getKnownGods().contains(d.getName().getLocalName())) {
                            godDescriptors.add(d);
                            System.out.println(d.getName().getLocalName() + " " + settings.getName() + "|");
                        }

                    }

                    //Random rnd = new Random();
                    if (godDescriptors != null) {
                        Random rnd = new Random();
                        message.addReceiver(godDescriptors.get(rnd.nextInt(godDescriptors.size())).getName());
                        //a.start = false;
                        a.send(message);
                        MessageTemplate performative = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        //MessageTemplate performative2 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
                        MessageTemplate ontology = MessageTemplate.MatchOntology("ProcessQuery"), ontology2 = MessageTemplate.MatchOntology("bad");
                        MessageTemplate template = MessageTemplate.and(performative, ontology);
                        MessageTemplate template2 = MessageTemplate.and(performative, ontology2);

                        ACLMessage msg3 = a.receive();

                        if (msg3 != null) {
                            System.out.println(msg3.getSender().getName() + "to " + settings.getName() + " " + msg3.getPerformative() + ": " + msg3.getOntology().toString());
                            if (msg3.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                                a.doWait();
                                //ProcessReplyTurn(msg3);
                                System.out.println("Created new god: " + settings.getName() + msg3.getSender().getName());
                                //System.out.println(msg.getSender().getName() + "to " + settings.getName() + " " + msg.getPerformative() + ": " + msg.getOntology().toString());
                                //a.start = true;
                                //action = new GodDoNothingAction(getLocalName());
                                //open = false;
                            } else {
                                //a.start = true;
                            }
                            //messageSentAndReceived = true;

                        }
                    }
                }System.out.println(settings.getName()+": end");
            }

        }); addSubBehaviour(new TickerBehaviour(a,700)
    { int exec = 0; long t1 = System.currentTimeMillis(); protected void onTick()
    { if(exec == 3){ stop();
        //a.processTurn(msg);
    } else
    { exec++; } } });
    }
}