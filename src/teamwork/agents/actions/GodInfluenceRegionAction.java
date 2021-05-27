package teamwork.agents.actions;

import teamwork.agents.enums.ElementType;

import java.util.List;

/**
 * God's action representing influencing a region
 */
public class GodInfluenceRegionAction extends GodAction {
    protected String regionName;
    protected List<ElementType> elements;
    protected List<Integer> values;

    public GodInfluenceRegionAction() {
    }

    public GodInfluenceRegionAction(String godName, String regionName, List<ElementType> element, List<Integer> value) {
        super(godName);
        this.regionName = regionName;
        this.elements = element;
        this.values = value;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public List<ElementType> getElements() {
        return elements;
    }

    public void setElements(List<ElementType> elements) {
        this.elements = elements;
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < elements.size(); i++) {
            sb.append(String.format("Action: [%s] applies [%s] on region [%s] with value [%+d]\n", godName, elements.get(i).getType(), regionName, values.get(i)));
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

    /**
     * Adds another influence to the action
     */
    public void AddAction(ElementType element, int value) {
        elements.add(element);
        values.add(value);
    }
}
