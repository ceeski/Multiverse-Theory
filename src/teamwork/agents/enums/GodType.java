package teamwork.agents.enums;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Types of gods used in simulation (as enum)
 */
public enum GodType {

    @SerializedName("Creator")
    CREATOR("Creator"),
    @SerializedName("Destructor")
    DESTRUCTOR("Destructor"),
    @SerializedName("Neutral")
    NEUTRAL("Neutral"),
    @SerializedName("Chaotic")
    CHAOTIC("Chaotic"),
    @SerializedName("Protector")
    PROTECTOR("Protector"),
    @SerializedName("Supergod")
    SUPERGOD("Supergod");

    private final String type;
    public String getType() {
        return type;
    }

    GodType(String type) {
        this.type = type;
    }

    public static List<GodType> AllTypes() {
        return Arrays.asList(GodType.CREATOR, GodType.DESTRUCTOR, GodType.NEUTRAL, GodType.CHAOTIC, GodType.PROTECTOR);
    }
}
