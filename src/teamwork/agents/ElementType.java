package teamwork.agents;

import com.google.gson.annotations.SerializedName;

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

    private ElementType(String type) {
        this.type = type;
    }

}
