package teamwork.agents.actions;

import teamwork.agents.enums.ElementType;

public class GodLearnAction extends GodAction{

    private ElementType elementType;

    public GodLearnAction() {
    }

    public GodLearnAction(String godName, ElementType elementType) {
        super(godName);
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return String.format("Action: [%s] learns about {%s}\n", godName, elementType.getType());
    }

    @Override
    public String actionType() {
        return GodLearnAction.class.getCanonicalName();
    }

    public static String getActionType() {
        return GodLearnAction.class.getCanonicalName();
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }
}
