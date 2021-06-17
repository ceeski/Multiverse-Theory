package teamwork.agents.actions;

import java.util.List;

public class GodMeetOthersAction extends GodAction{

    private String friendName;
    private List<String> newFriendsNames;

    public GodMeetOthersAction() {
    }

    public GodMeetOthersAction(String godName, String friendName, List<String> newFriendsNames) {
        super(godName);
        this.friendName = friendName;
        this.newFriendsNames = newFriendsNames;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("Action: [%s] asks [%s] to organize a meeting. ", godName, friendName));
        if(newFriendsNames == null || newFriendsNames.size() == 0)
            sb.append("Sadly no one came.");
        else {
            sb.append(String.format("[ %s", newFriendsNames.get(0)));
            for(int i = 1; i < newFriendsNames.size(); i++) {
                sb.append(String.format(", %s", newFriendsNames.get(i)));
            }
            sb.append(" ] came.");
        }
        return sb.append("\n").toString();
    }

    @Override
    public String actionType() {
        return GodMeetOthersAction.class.getCanonicalName();
    }

    public static String getActionType() {
        return GodMeetOthersAction.class.getCanonicalName();
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public List<String> getNewFriendsNames() {
        return newFriendsNames;
    }

    public void setNewFriendsNames(List<String> newFriendsNames) {
        this.newFriendsNames = newFriendsNames;
    }
}
