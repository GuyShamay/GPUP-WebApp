package old.component.progressdata;

import old.component.target.FinishResult;
import old.component.target.Result;
import old.component.target.RunResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProgressData {
    //RUN-RESULT-LISTS
    private ObservableList<String> frozen;
    private ObservableList<String> skipped;
    private ObservableList<String> waiting;
    private ObservableList<String> inprocces;

    //FINISH-RESULT-LISTS
    private ObservableList<String> success;
    private ObservableList<String> warning;
    private ObservableList<String> failure;

    public ProgressData() {
        this.frozen = FXCollections.observableArrayList();
        this.skipped = FXCollections.observableArrayList();
        this.waiting = FXCollections.observableArrayList();
        this.inprocces = FXCollections.observableArrayList();
        this.success = FXCollections.observableArrayList();
        this.warning = FXCollections.observableArrayList();
        this.failure = FXCollections.observableArrayList();
    }

    public ObservableList<String> getFrozen() {
        return frozen;
    }

    public ObservableList<String> getWaiting() {
        return waiting;
    }

    public ObservableList<String> getSuccess() {
        return success;
    }

    public ObservableList<String> getSkipped() {
        return skipped;
    }

    public ObservableList<String> getInprocces() {
        return inprocces;
    }

    public ObservableList<String> getWarning() {
        return warning;
    }

    public void setWarning(ObservableList<String> warning) {
        this.warning = warning;
    }

    public ObservableList<String> getFailure() {
        return failure;
    }

    public void setFailure(ObservableList<String> failure) {
        this.failure = failure;
    }


    public void initToFrozen(String name) {
        frozen.add(name);
    }

    public void move(Result from, Result to, String name) {
            switch ((RunResult) from) {
                case FROZEN:
                    remove(frozen, name);
                    break;
                case WAITING:
                    remove(waiting, name);
                    break;
                case INPROCESS:
                    remove(inprocces,name);
                    break;
                case SKIPPED:
                    remove(skipped,name);
                    break;
            }
            if (to instanceof RunResult) {
                switch ((RunResult) to) {
                    case FROZEN:
                        if(!frozen.contains(name)) {
                            frozen.add(name);
                        }
                        break;
                    case WAITING:
                        if(!waiting.contains(name)) {
                            waiting.add(name);
                        }
                        break;
                    case INPROCESS:
                        if(!inprocces.contains(name)) {
                            inprocces.add(name);
                        }
                        break;
                    case SKIPPED:
                        if(!skipped.contains(name)) {
                            skipped.add(name);
                        }
                        break;
                }
            } else {
                switch ((FinishResult) to) {
                    case SUCCESS:
                        if(!success.contains(name)) {
                            success.add(name);
                        }
                        break;
                    case WARNING:
                        if(!warning.contains(name)) {
                            warning.add(name);
                        }
                        break;
                    case FAILURE:
                        if(!failure.contains(name)) {
                            failure.add(name);
                        }
                        break;
                }
            }
    }

    private void remove(ObservableList<String> list, String name) {
        if(list.contains(name)) {
            int index = list.indexOf(name);
            if (index >= 0) {
                list.remove(index);
            }
        }
    }
}
