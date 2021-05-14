package teamwork.agents.jsonwrappers;

public class TimeWrapper {
    private int numberOfTurns;

    public TimeWrapper() {
    }

    public TimeWrapper(int numberOfTurns) {
        this.numberOfTurns = numberOfTurns;
    }

    public int getNumberOfTurns() {
        return numberOfTurns;
    }

    public void setNumberOfTurns(int numberOfTurns) {
        this.numberOfTurns = numberOfTurns;
    }
}
