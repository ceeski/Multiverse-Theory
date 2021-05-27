package teamwork.agents.wrappers;

import teamwork.agents.enums.GodType;

import java.util.List;

public class GodWrapper {
    private String name;
    private GodType type;

    private int speed;
    private int chanceToShareKnowledgePercent;
    private int chanceToCooperatePercent;

    private List<String> knownGods;
    private List<String> knownRegions;

    private int maxSkillPoints;

    private int fireSkill;
    private int waterSkill;

    private int lightSkill;
    private int darknessSkill;

    private int earthSkill;
    private int airSkill;

    private int knowledgeSkill;
    private int amusementSkill;

    private int loveSkill;
    private int restraintSkill;

    public GodWrapper() {
    }

    public GodWrapper(String name, GodType type, int speed, int chanceToShareKnowledgePercent, int chanceToCooperatePercent, List<String> knownGods, List<String> knownRegions, int maxSkillPoints, int fireSkill, int waterSkill, int lightSkill, int darknessSkill, int earthSkill, int airSkill, int knowledgeSkill, int amusementSkill, int loveSkill, int restraintSkill) {
        this.name = name;
        this.type = type;
        this.speed = speed;
        this.chanceToShareKnowledgePercent = chanceToShareKnowledgePercent;
        this.chanceToCooperatePercent = chanceToCooperatePercent;
        this.knownGods = knownGods;
        this.knownRegions = knownRegions;
        this.maxSkillPoints = maxSkillPoints;
        this.fireSkill = fireSkill;
        this.waterSkill = waterSkill;
        this.lightSkill = lightSkill;
        this.darknessSkill = darknessSkill;
        this.earthSkill = earthSkill;
        this.airSkill = airSkill;
        this.knowledgeSkill = knowledgeSkill;
        this.amusementSkill = amusementSkill;
        this.loveSkill = loveSkill;
        this.restraintSkill = restraintSkill;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GodType getType() {
        return type;
    }

    public void setType(GodType type) {
        this.type = type;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getChanceToShareKnowledgePercent() {
        return chanceToShareKnowledgePercent;
    }

    public void setChanceToShareKnowledgePercent(int chanceToShareKnowledgePercent) {
        this.chanceToShareKnowledgePercent = chanceToShareKnowledgePercent;
    }

    public int getChanceToCooperatePercent() {
        return chanceToCooperatePercent;
    }

    public void setChanceToCooperatePercent(int chanceToCooperatePercent) {
        this.chanceToCooperatePercent = chanceToCooperatePercent;
    }

    public List<String> getKnownGods() {
        return knownGods;
    }

    public void setKnownGods(List<String> knownGods) {
        this.knownGods = knownGods;
    }

    public List<String> getKnownRegions() {
        return knownRegions;
    }

    public void setKnownRegions(List<String> knownRegions) {
        this.knownRegions = knownRegions;
    }

    public int getMaxSkillPoints() {
        return maxSkillPoints;
    }

    public void setMaxSkillPoints(int maxSkillPoints) {
        this.maxSkillPoints = maxSkillPoints;
    }

    public int getFireSkill() {
        return fireSkill;
    }

    public void setFireSkill(int fireSkill) {
        this.fireSkill = fireSkill;
    }

    public int getWaterSkill() {
        return waterSkill;
    }

    public void setWaterSkill(int waterSkill) {
        this.waterSkill = waterSkill;
    }

    public int getLightSkill() {
        return lightSkill;
    }

    public void setLightSkill(int lightSkill) {
        this.lightSkill = lightSkill;
    }

    public int getDarknessSkill() {
        return darknessSkill;
    }

    public void setDarknessSkill(int darknessSkill) {
        this.darknessSkill = darknessSkill;
    }

    public int getEarthSkill() {
        return earthSkill;
    }

    public void setEarthSkill(int earthSkill) {
        this.earthSkill = earthSkill;
    }

    public int getAirSkill() {
        return airSkill;
    }

    public void setAirSkill(int airSkill) {
        this.airSkill = airSkill;
    }

    public int getKnowledgeSkill() {
        return knowledgeSkill;
    }

    public void setKnowledgeSkill(int knowledgeSkill) {
        this.knowledgeSkill = knowledgeSkill;
    }

    public int getAmusementSkill() {
        return amusementSkill;
    }

    public void setAmusementSkill(int amusementSkill) {
        this.amusementSkill = amusementSkill;
    }

    public int getLoveSkill() {
        return loveSkill;
    }

    public void setLoveSkill(int loveSkill) {
        this.loveSkill = loveSkill;
    }

    public int getRestraintSkill() {
        return restraintSkill;
    }

    public void setRestraintSkill(int restraintSkill) {
        this.restraintSkill = restraintSkill;
    }
}
