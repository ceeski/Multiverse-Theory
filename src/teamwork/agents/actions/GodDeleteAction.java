package teamwork.agents.actions;

public class GodDeleteAction extends GodAction{

    public GodDeleteAction() {
    }

    public GodDeleteAction(String godName) {
        super(godName);
    }

    @Override
    public String toString() {
        return String.format("Action: [%s] killed itself\n", godName);
    }

    @Override
    public String actionType() {
        return GodDeleteAction.class.getCanonicalName();
    }

    public static String getActionType() {
        return GodDeleteAction.class.getCanonicalName();
    }
}
