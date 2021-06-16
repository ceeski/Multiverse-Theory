package teamwork.agents.wrappers;

import org.javatuples.Pair;

public class RegionWrapper {
    private String name;
    private Pair<String, Integer> mostUnbalancedResource;

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
        //this.mostUnbalancedResource = new Pair<>("heatResource", heatResource);
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

    /**
     * Get the lowest value of one of the resources
     */

    public void setRestraintResource(int restraintResource) {
        this.restraintResource = restraintResource;
    }
}
