package teamwork.agents;

public class Head{
/*

    private GodWrapper settings;
    private static final int BALANCE = 575; //Balance is slightly higher to represent the fact that people will use resources
    private static final int SMALL_CHANGE = 45; //If change is small (below this number) - god might consider learning something
    private static final int MAX = 1000;
    private static final int MIN = 0;
    private List<GodRegionWrapper> holonSettings;
    private AID[] gods;
    private List<RegionWrapper> regions;

    //@Override
    protected void setup() {
        holonSettings = (List<GodRegionWrapper>) getArguments()[0];
        gods = (AID[]) getArguments()[1];
        for (var s : holonSettings) {
            for (var r : s.getRegions()) {
                regions.add(r);
            }
        }
        Common.registerAgentInDf(this, "1");
        //addBehaviour(processMessage);
    }

    int fire = 0, water = 0, light = 0, darkness = 0, earth = 0, air = 0,
            knowledge = 0, amusement = 0, love = 0, restraint = 0;

    GodAction ProcessSuperGodTurn() {
        int min_population = 10000;
        RegionWrapper regionName = new RegionWrapper();
        for (var element : regions) {
            if (element.getPopulation() < min_population) {
                regionName = element;
                min_population = element.getPopulation();
            }
        }
        Random rnd = new Random();
        if (min_population == 10000) //if all regions don't have any knownRegions
        {
            //int regionIndex = rnd.nextInt(holonSettings[godIndex].getKnownRegions().size());
            regionName = regions.get(rnd.nextInt(regions.size()));
        }
        Pair<ElementType, Integer> pair = new Pair<>(ElementType.FIRE, fire);

        return new GodInfluenceRegionAction(getLocalName(), regionName.getName(), Collections.singletonList(pair.getValue0()), Collections.singletonList(pair.getValue1()));
    }
*/

}
