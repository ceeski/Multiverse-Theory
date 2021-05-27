package teamwork.agents.enums;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Types of elements used in simulation (as enum)
 */
public enum ElementType {

    @SerializedName("Fire")
    FIRE("Fire"),
    @SerializedName("Water")
    WATER("Water"),
    @SerializedName("Light")
    LIGHT("Light"),
    @SerializedName("Darkness")
    DARKNESS("Darkness"),
    @SerializedName("Earth")
    EARTH("Earth"),
    @SerializedName("Air")
    AIR("Air"),
    @SerializedName("Knowledge")
    KNOWLEDGE("Knowledge"),
    @SerializedName("Amusement")
    AMUSEMENT("Amusement"),
    @SerializedName("Love")
    LOVE("Love"),
    @SerializedName("Restraint")
    RESTRAINT("Restraint");

    private final String type;
    public String getType() {
        return type;
    }

    ElementType(String type) {
        this.type = type;
    }

    public static List<ElementType> AllTypes() {
        return Arrays.asList(ElementType.FIRE, ElementType.WATER, ElementType.LIGHT, ElementType.DARKNESS, ElementType.EARTH, ElementType.AIR, ElementType.KNOWLEDGE, ElementType.AMUSEMENT, ElementType.LOVE, ElementType.RESTRAINT);
    }
}
