package worker.client.component.control.targets;

import dto.users.UserDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import worker.logic.Worker;

import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TargetsControlRefresher extends TimerTask {

    private final Consumer<Void> targetsConsumer;
    private final Worker worker;
    private final BooleanProperty shouldUpdate;

    public TargetsControlRefresher(Consumer<Void> targetsConsumer) {
        this.shouldUpdate = new SimpleBooleanProperty(true);
        this.targetsConsumer = targetsConsumer;
        this.worker = null;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }
        targetsConsumer.accept(null);
    }

    public void pause() {
        shouldUpdate.set(false);
    }

    public void resume() {
        shouldUpdate.set(true);
    }
}
