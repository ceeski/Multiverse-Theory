package teamwork.agents.utility;

public class GodDoNothingAction extends GodAction{

    public GodDoNothingAction() {
    }

    public GodDoNothingAction(String godName) {
        super(godName);
    }

    @Override
    public String toString() {
        return String.format("Action: [%s] does nothing\n", godName);
    }

    @Override
    public String actionType() {
        return GodDoNothingAction.class.getCanonicalName();
    }

    public static String getActionType() {
        return GodDoNothingAction.class.getCanonicalName();
    }
}
