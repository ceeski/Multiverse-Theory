package teamwork.agents.utility;

import org.javatuples.Pair;
import teamwork.agents.enums.ElementType;
import teamwork.agents.wrappers.GodWrapper;
import teamwork.agents.wrappers.RegionWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GodHelper {
    /**
     * Returns list of all types and changes that can be made (all 3 possible interactions)
     * @param god Settings of a god
     * @return Dictionary - key is element type, value is a pair (min, max) describing possible change
     */
    public static Map<ElementType, Pair<Integer, Integer>> getPossibleElementChanges(GodWrapper god) {
        var waterPair = new Pair<>(Math.min(-god.getWaterSkill() * 15, -god.getFireSkill() * 8), god.getWaterSkill() * 25);
        var firePair = new Pair<>(Math.min(-god.getFireSkill() * 15, -god.getWaterSkill() * 8), god.getFireSkill() * 25);
        var lightPair = new Pair<>(Math.min(-god.getLightSkill() * 15, -god.getDarknessSkill() * 8), god.getLightSkill() * 25);
        var darknessPair = new Pair<>(Math.min(-god.getDarknessSkill() * 15, -god.getLightSkill() * 8), god.getDarknessSkill() * 25);
        var earthPair = new Pair<>(Math.min(-god.getEarthSkill() * 15, -god.getAirSkill() * 8), god.getEarthSkill() * 25);
        var airPair = new Pair<>(Math.min(-god.getAirSkill() * 15, -god.getEarthSkill() * 8), god.getAirSkill() * 25);
        var knowledgePair = new Pair<>(Math.min(-god.getKnowledgeSkill() * 15, -god.getAmusementSkill() * 8), god.getKnowledgeSkill() * 25);
        var amusementPair = new Pair<>(Math.min(-god.getAmusementSkill() * 15, -god.getKnowledgeSkill() * 8), god.getAmusementSkill() * 25);
        var lovePair = new Pair<>(Math.min(-god.getLoveSkill() * 15, -god.getRestraintSkill() * 8), god.getLoveSkill() * 25);
        var restraintPair = new Pair<>(Math.min(-god.getRestraintSkill() * 15, -god.getLoveSkill() * 8), god.getRestraintSkill() * 25);

        return new HashMap<>() {{
              put(ElementType.WATER, waterPair);
              put(ElementType.FIRE, firePair);
              put(ElementType.LIGHT, lightPair);
              put(ElementType.DARKNESS, darknessPair);
              put(ElementType.EARTH, earthPair);
              put(ElementType.AIR, airPair);
              put(ElementType.KNOWLEDGE, knowledgePair);
              put(ElementType.AMUSEMENT, amusementPair);
              put(ElementType.LOVE, lovePair);
              put(ElementType.RESTRAINT, restraintPair);
        }};
    }

    /**
     * Calculates final value of action
     * @param plannedValue Value that god wants to use
     * @param element Element
     * @param god God's information
     */
    public static int finalElementChange(int plannedValue, ElementType element, GodWrapper god) {
        Random rnd = new Random();
        int maxAbsPrecision;
        int precision;
        switch(element) {
            case WATER:
                maxAbsPrecision = 100 - 10 * god.getWaterSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case FIRE:
                maxAbsPrecision = 100 - 10 * god.getFireSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case LIGHT:
                maxAbsPrecision = 100 - 10 * god.getLightSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case DARKNESS:
                maxAbsPrecision = 100 - 10 * god.getDarknessSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case EARTH:
                maxAbsPrecision = 100 - 10 * god.getEarthSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case AIR:
                maxAbsPrecision = 100 - 10 * god.getAirSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case KNOWLEDGE:
                maxAbsPrecision = 100 - 10 * god.getKnowledgeSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case AMUSEMENT:
                maxAbsPrecision = 100 - 10 * god.getAmusementSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case LOVE:
                maxAbsPrecision = 100 - 10 * god.getLoveSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
            case RESTRAINT:
                maxAbsPrecision = 100 - 10 * god.getRestraintSkill();
                precision = rnd.nextInt() % (2 * maxAbsPrecision + 1) - maxAbsPrecision;
                plannedValue = (int)((double)plannedValue * (1.0 + (double)precision/100.0));
                break;
        }

        return plannedValue;
    }

    /**
            * Checks balance
     * @param finalValue Value that god wants to use
     * @param element Element
     * @param god God's information
            */
    public static boolean checkBalance(int finalValue, ElementType element, GodWrapper god) {
        boolean balance = false;
        switch(element) {
            case WATER:
                balance = Math.abs(god.getWaterSkill()+finalValue-575)< Math.abs(god.getWaterSkill()-575);
                break;
            case FIRE:
                balance = Math.abs(god.getFireSkill()+finalValue-575)< Math.abs(god.getFireSkill()-575);
                break;
            case LIGHT:
                balance = Math.abs(god.getLightSkill()+finalValue-575)< Math.abs(god.getLightSkill()-575);
                break;
            case DARKNESS:
                balance = Math.abs(god.getDarknessSkill()+finalValue-575)< Math.abs(god.getDarknessSkill()-575);
                break;
            case EARTH:
                balance = Math.abs(god.getEarthSkill()+finalValue-575)< Math.abs(god.getEarthSkill()-575);
                break;
            case AIR:
                balance = Math.abs(god.getAirSkill()+finalValue-575)< Math.abs(god.getAirSkill()-575);
                break;
            case KNOWLEDGE:
                balance = Math.abs(god.getKnowledgeSkill()+finalValue-575)< Math.abs(god.getKnowledgeSkill()-575);
                break;
            case AMUSEMENT:
                balance = Math.abs(god.getAmusementSkill()+finalValue-575)< Math.abs(god.getAmusementSkill()-575);
                break;
            case LOVE:
                balance = Math.abs(god.getLoveSkill()+finalValue-575)< Math.abs(god.getLoveSkill()-575);
                break;
            case RESTRAINT:
                balance = Math.abs(god.getRestraintSkill()+finalValue-575)< Math.abs(god.getRestraintSkill()-575);
                break;
        }

        return balance;
    }

    /**
     * Calculates the sum of distances of all resources to the perfect score, so basically 0 = perfect region, 5000 = everything is in the worst possible place
     */
    public static int getRegionScore(RegionWrapper region) {
        return Math.abs(500 - region.getWaterResource()) + Math.abs(500 - region.getHeatResource()) + Math.abs(500 - region.getLightResource()) +
                Math.abs(500 - region.getDarknessResource()) + Math.abs(500 - region.getEarthResource()) + Math.abs(500 - region.getAirResource()) +
                Math.abs(500 - region.getKnowledgeResource()) + Math.abs(500 - region.getAmusementResource()) + Math.abs(500 - region.getLoveResource()) +
                Math.abs(500 - region.getRestraintResource());
    }

    /**
     * Checks if God has at least one free skillpoint
     * @param god Settings of a god
     * @return True if it has, false otherwise
     */
    public static boolean hasFreeSkillpoints(GodWrapper god) {
        int sum = 0;
        sum += god.getAirSkill();
        sum += god.getAmusementSkill();
        sum += god.getDarknessSkill();
        sum += god.getEarthSkill();
        sum += god.getLoveSkill();
        sum += god.getFireSkill();
        sum += god.getKnowledgeSkill();;
        sum += god.getLightSkill();
        sum += god.getRestraintSkill();
        sum += god.getWaterSkill();

        return sum < god.getMaxSkillPoints();
    }
}
