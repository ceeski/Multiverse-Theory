package teamwork.agents.utility;

import teamwork.agents.ElementType;

/**
 * God's action representing influencing a region
 */
public class GodInfluenceRegionAction extends GodAction {
    protected String regionName;
    protected ElementType[] element;
    protected int[] value;

    public GodInfluenceRegionAction() {
    }

    public GodInfluenceRegionAction(String godName, String regionName, ElementType[] element, int[] value) {
        super(godName);
        this.regionName = regionName;
        this.element = element;
        this.value = value;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public ElementType[] getElement() {
        return element;
    }

    public void setElement(ElementType[] element) {
        this.element = element;
    }

    public int[] getValue() {
        return value;
    }

    public void setValue(int[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < element.length; i++) {
            sb.append(String.format("Action: [%s] applies [%s] on region [%s] with value [%+d]\n", godName, element[i].getType(), regionName, value[i]));
        }
        return sb.toString();
    }

    @Override
    public String actionType() {
        return GodInfluenceRegionAction.class.getCanonicalName();
    }

    public static String getActionType() {
        return GodInfluenceRegionAction.class.getCanonicalName();
    }
}
