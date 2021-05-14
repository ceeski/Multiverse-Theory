package teamwork;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import teamwork.agents.jsonwrappers.SimulationWrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;

public class Main {

    private final static int afterRegionsMsDelay = 4000;
    private final static int afterGodsMsDelay = 4000;

    public static void main(String[] args) throws InterruptedException {
        if(args.length < 1) {
            System.out.println("You have to provide json file with simulation settings");
            return;
        }

        SimulationWrapper simulation;
        Gson _gson = new GsonBuilder().create();

        System.out.println("Parsing file with settings: '" + args[0] + "'...");

        try {
            Path fileName = Path.of(args[0]);
            String content = Files.readString(fileName);
            simulation = _gson.fromJson(content, SimulationWrapper.class);
        } catch(Exception e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("Couldn't start simulation");
            return;
        }

        System.out.println("Settings parsed.");
        System.out.println("Starting main container...");

        //Start main container with DF and all
        String[] mainContainer = {
                "-gui"
        };
        Boot.main(mainContainer);
        var runtime = jade.core.Runtime.instance();

        System.out.println("Main container started.");
        System.out.println("Starting region agents in new container...");

        //Start container with Regions
        var regionsProfile = new ProfileImpl();
        regionsProfile.setParameter(Profile.CONTAINER_NAME, "Regions");
        regionsProfile.setParameter(Profile.MAIN_HOST, "localhost");
        var regionsContainerController  = runtime.createAgentContainer(regionsProfile);
        int regionCounter = 0;
        try {
            for(var regionSettings : simulation.getRegions()) {
                //String settings = _gson.toJson(regionSettings);
                var regionAgent = regionsContainerController.createNewAgent(regionSettings.getName(), "teamwork.agents.Region", new Object[] {regionSettings});
                regionAgent.start();
                regionCounter++;
            }
        } catch (Exception e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("Couldn't start region agents");
            runtime.shutDown();
            return;
        }

        Thread.sleep(afterRegionsMsDelay);

        System.out.println(regionCounter + " regions has been created.");
        System.out.println("Starting God agents in new container...");

        //Start container with Gods
        var godsProfile = new ProfileImpl();
        godsProfile.setParameter(Profile.CONTAINER_NAME, "Gods");
        godsProfile.setParameter(Profile.MAIN_HOST, "localhost");
        var godsContainerController  = runtime.createAgentContainer(godsProfile);
        int godsCounter = 0;
        try {
            for(var godSettings : simulation.getGods()) {
                var godAgent = godsContainerController.createNewAgent(godSettings.getName(), "teamwork.agents.God", new Object[] {godSettings});
                godAgent.start();
                godsCounter++;
            }
        } catch (Exception e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("Couldn't start God agents");
            runtime.shutDown();
            return;
        }

        Thread.sleep(afterGodsMsDelay);

        System.out.println(godsCounter + " gods has been created.");
        System.out.println("Starting time agent in new container...");

        //Start container with Time manager
        var timeManagerProfile = new ProfileImpl();
        timeManagerProfile.setParameter(Profile.CONTAINER_NAME, "Time");
        timeManagerProfile.setParameter(Profile.MAIN_HOST, "localhost");
        var timeManagerContainerController  = runtime.createAgentContainer(timeManagerProfile);
        try {
            var timeManager = timeManagerContainerController.createNewAgent("Time", "teamwork.agents.Time", new Object[]{ simulation.getTime() });
            timeManager.start();
        } catch (Exception e) {
            System.out.println("Exception " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("Couldn't start time agent");
            runtime.shutDown();
            return;
        }

        System.out.println("Time agent has started.");
        System.out.println("Simulation has been started properly.");
    }
}
