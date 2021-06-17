package teamwork.agents.wrappers;

public class RegionWrapper {
    private String name;
    private int population;

    private int heatResource;
    private int waterResource;

    private int lightResource;
    private int darknessResource;

    private int earthResource;
    private int airResource;

    private int knowledgeResource;
    private int amusementResource;

    private int loveResource;
    private int restraintResource;

    public RegionWrapper() {
    }

    public RegionWrapper(String name, int population, int heatResource, int waterResource, int lightResource, int darknessResource, int earthResource, int airResource, int knowledgeResource, int amusementResource, int loveResource, int restraintResource) {
        this.name = name;
        this.population = population;
        this.heatResource = heatResource;
        this.waterResource = waterResource;
        this.lightResource = lightResource;
        this.darknessResource = darknessResource;
        this.earthResource = earthResource;
        this.airResource = airResource;
        this.knowledgeResource = knowledgeResource;
        this.amusementResource = amusementResource;
        this.loveResource = loveResource;
        this.restraintResource = restraintResource;
    }

    @Override
    public String toString() {
        return String.format("Region [%s]:\n\tPopulation: [%d]\n\tHeat: [%d]; Water: [%d]; Light: [%d]; Darkness: [%d]; Earth: [%d]\n\tAir: [%d]; Knowledge: [%d]; Amusement: [%d]; Love: [%d]; Restraint: [%d]\n",
                name, population, heatResource, waterResource, lightResource, darknessResource, earthResource, airResource, knowledgeResource, amusementResource, loveResource, restraintResource);
    }
    //575
    public String colorElement(int el){
        String before = "033[38;5;", middle="m", end="\033[0m";
        int bad = 196, good = 154, perfect = 27;
        if (isPerfect(el)){
            good = perfect;
        }
        else if (isNotBalance(el)){
            good = bad;
        }
        return before + good + middle + el + end;
    }

    public boolean isNotBalance(int el){
        return el < 300 && el > 700;
    }

    public boolean isPerfect(int el){
        return el == 575;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getHeatResource() {
        return heatResource;
    }

    public void setHeatResource(int heatResource) {
        this.heatResource = heatResource;
    }

    public int getWaterResource() {
        return waterResource;
    }

    public void setWaterResource(int waterResource) {
        this.waterResource = waterResource;
    }

    public int getLightResource() {
        return lightResource;
    }

    public void setLightResource(int lightResource) {
        this.lightResource = lightResource;
    }

    public int getDarknessResource() {
        return darknessResource;
    }

    public void setDarknessResource(int darknessResource) {
        this.darknessResource = darknessResource;
    }

    public int getEarthResource() {
        return earthResource;
    }

    public void setEarthResource(int earthResource) {
        this.earthResource = earthResource;
    }

    public int getAirResource() {
        return airResource;
    }

    public void setAirResource(int airResource) {
        this.airResource = airResource;
    }

    public int getKnowledgeResource() {
        return knowledgeResource;
    }

    public void setKnowledgeResource(int knowledgeResource) {
        this.knowledgeResource = knowledgeResource;
    }

    public int getAmusementResource() {
        return amusementResource;
    }

    public void setAmusementResource(int amusementResource) {
        this.amusementResource = amusementResource;
    }

    public int getLoveResource() {
        return loveResource;
    }

    public void setLoveResource(int loveResource) {
        this.loveResource = loveResource;
    }

    public int getRestraintResource() {
        return restraintResource;
    }

    public void setRestraintResource(int restraintResource) {
        this.restraintResource = restraintResource;
    }
}
