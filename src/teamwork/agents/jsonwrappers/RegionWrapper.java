package teamwork.agents.jsonwrappers;

public class RegionWrapper {
    private String name;
    private int initialPopulation;

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

    public RegionWrapper(String name, int initialPopulation, int heatResource, int waterResource, int lightResource, int darknessResource, int earthResource, int airResource, int knowledgeResource, int amusementResource, int loveResource, int restraintResource) {
        this.name = name;
        this.initialPopulation = initialPopulation;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInitialPopulation() {
        return initialPopulation;
    }

    public void setInitialPopulation(int initialPopulation) {
        this.initialPopulation = initialPopulation;
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
