package teamwork.agents;

import com.google.gson.annotations.SerializedName;

/**
 * Types of gods used in simulation (as enum)
 */
public enum GodType {

    @SerializedName("Creator")
    CREATOR("Creator"),
    @SerializedName("Destructor")
    DESTRUCTOR("Destructor"),
    @SerializedName("Destructor")
    NEUTRAL("Neutral"),
    @SerializedName("Chaotic")
    CHAOTIC("Chaotic"),
    @SerializedName("Protector")
    PROTECTOR("Protector");

    private final String type;
    public String getType() {
        return type;
    }

    private GodType(String type) {
        this.type = type;
    }
}
