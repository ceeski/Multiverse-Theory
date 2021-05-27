package teamwork.agents.actions;

/**
 * Abstract class representing god's action.
 */
public abstract class GodAction {
    protected String godName;

    public GodAction() {
    }

    public GodAction(String godName) {
        this.godName = godName;
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
