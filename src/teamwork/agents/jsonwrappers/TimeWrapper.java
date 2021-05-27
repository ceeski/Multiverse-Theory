package teamwork.agents.jsonwrappers;

public class TimeWrapper {
    private int numberOfTurns;
    private String pathToLogFile;

    public TimeWrapper() {
    }

    public TimeWrapper(int numberOfTurns, String pathToLogFile) {
        this.numberOfTurns = numberOfTurns;
        this.pathToLogFile = pathToLogFile;
    }

    public int getNumberOfTurns() {
        return numberOfTurns;
    }

    public void setNumberOfTurns(int numberOfTurns) {
        this.numberOfTurns = numberOfTurns;
    }

    public String getPathToLogFile() {
        return pathToLogFile;
    }

    public void setPathToLogFile(String pathToLogFile) {
        this.pathToLogFile = pathToLogFile;
    }
}
