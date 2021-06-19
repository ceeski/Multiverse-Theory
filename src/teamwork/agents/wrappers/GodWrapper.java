package teamwork.agents.wrappers;

import org.javatuples.Pair;
import teamwork.agents.enums.ElementType;
import teamwork.agents.enums.GodType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GodWrapper {
    private String name;
    private  Boolean separate;
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

    public GodWrapper( String name, boolean separate, GodType type, int speed, int chanceToShareKnowledgePercent, int chanceToCooperatePercent, List<String> knownGods, List<String> knownRegions, int maxSkillPoints, int fireSkill, int waterSkill, int lightSkill, int darknessSkill, int earthSkill, int airSkill, int knowledgeSkill, int amusementSkill, int loveSkill, int restraintSkill) {
        this.name = name;
        this.separate = separate;
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

    public void updateGod(GodWrapper otherGod){
        if (!this.name.contains((otherGod.name)))
        {this.name += otherGod.name;}
        this.separate = false;
        this.speed = (this.speed+otherGod.speed)/2;
        this.chanceToShareKnowledgePercent = (this.chanceToShareKnowledgePercent+otherGod.chanceToShareKnowledgePercent)/2;
        this.chanceToCooperatePercent = (this.chanceToCooperatePercent + otherGod.chanceToCooperatePercent)/2;
        for (String known : otherGod.getKnownGods()){
        if (this.getKnownGods().stream().noneMatch(name -> name.equals(known))) {
            this.getKnownGods().add(known);
        }}
        for (String known : otherGod.getKnownRegions()){
            if (this.getKnownRegions().stream().noneMatch(name -> name.equals(known))) {
                this.getKnownRegions().add(known);
            }}
        this.maxSkillPoints += otherGod.maxSkillPoints;
        this.fireSkill += otherGod.fireSkill;
        this.waterSkill += otherGod.waterSkill;
        this.lightSkill += otherGod.lightSkill;
        this.darknessSkill += otherGod.darknessSkill;
        this.earthSkill += otherGod.earthSkill;
        this.airSkill += otherGod.airSkill;
        this.knowledgeSkill += otherGod.knowledgeSkill;
        this.amusementSkill += otherGod.amusementSkill;
        this.loveSkill += otherGod.loveSkill;
        this.restraintSkill += otherGod.restraintSkill;
    }

    public Boolean getSeparate() {
        return separate;
    }

    public void setSeparate(Boolean separate) {
        this.separate = separate;
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

    /**
     * Adds knowledge about element (up to 10)
     * @param element
     */
    public void learnAbout(ElementType element) {
        if(element == ElementType.AIR && airSkill < 10)
            airSkill++;
        if(element == ElementType.EARTH && earthSkill < 10)
            earthSkill++;
        if(element == ElementType.LOVE && loveSkill < 10)
            loveSkill++;
        if(element == ElementType.RESTRAINT && restraintSkill < 10)
            restraintSkill++;
        if(element == ElementType.FIRE && fireSkill < 10)
            fireSkill++;
        if(element == ElementType.WATER && waterSkill < 10)
            waterSkill++;
        if(element == ElementType.DARKNESS && darknessSkill < 10)
            darknessSkill++;
        if(element == ElementType.LIGHT && lightSkill < 10)
            lightSkill++;
        if(element == ElementType.KNOWLEDGE && knowledgeSkill < 10)
            knowledgeSkill++;
        if(element == ElementType.AMUSEMENT && amusementSkill < 10)
            amusementSkill++;
    }

    /**
     * Gets element that can be taught by this God
     * @return
     */
    public ElementType getElementToTeach() {
        List<Pair<ElementType, Integer>> knownElements = new ArrayList<>();
        knownElements.add(new Pair<>(ElementType.AIR, airSkill));
        knownElements.add(new Pair<>(ElementType.EARTH, earthSkill));
        knownElements.add(new Pair<>(ElementType.WATER, waterSkill));
        knownElements.add(new Pair<>(ElementType.FIRE, fireSkill));
        knownElements.add(new Pair<>(ElementType.LIGHT, lightSkill));
        knownElements.add(new Pair<>(ElementType.DARKNESS, darknessSkill));
        knownElements.add(new Pair<>(ElementType.LOVE, loveSkill));
        knownElements.add(new Pair<>(ElementType.RESTRAINT, restraintSkill));
        knownElements.add(new Pair<>(ElementType.KNOWLEDGE, knowledgeSkill));
        knownElements.add(new Pair<>(ElementType.AMUSEMENT, amusementSkill));

        knownElements = knownElements.stream().sorted((o1, o2) -> Integer.compare(Math.abs(o2.getValue1()), Math.abs(o1.getValue1()))).collect(Collectors.toList());

        int maxVal = Math.abs(knownElements.get(0).getValue1());
        //We want to limit possible actions to only the ones with maximal (the same) change:
        knownElements = knownElements.stream().takeWhile(entry -> Math.abs(entry.getValue1()) == maxVal).collect(Collectors.toList());

        Random rand = new Random();
        Pair<ElementType, Integer> element = knownElements.get(rand.nextInt(knownElements.size()));

        return  element.getValue0();
    }
}
