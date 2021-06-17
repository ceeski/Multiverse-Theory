package teamwork.agents.actions;

import teamwork.agents.enums.GodType;

/**
 * Abstract class representing god's action.
 */
public abstract class GodAction {
    protected String godName;
    protected GodType godType;

    public GodAction() {
    }

    public GodAction(String godName, GodType godType) {
        this.godName = godName;
        this.godType = godType;
    }

    public GodAction(String godName) {
        this.godName = godName;
    }

    public GodType getGodType() {
        return godType;
    }

    public void setGodType(GodType godType) {
        this.godType = godType;
    }

    public String getGodName() {
        return godName;
    }

    public void setGodName(String godName) {
        this.godName = godName;
    }

    /**
     * Get type as a string so it can be recognized during deserialization
     */
    public abstract String actionType();
}
