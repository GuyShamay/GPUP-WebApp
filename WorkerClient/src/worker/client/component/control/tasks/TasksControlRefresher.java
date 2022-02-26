package worker.client.component.control.tasks;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import worker.logic.Worker;

import java.util.TimerTask;
import java.util.function.Consumer;

public class TasksControlRefresher extends TimerTask {
    private final Consumer<Void> tasksConsumer;
    private final Worker worker;
    private final BooleanProperty shouldUpdate;

    public TasksControlRefresher(Consumer<Void> tasksConsumer) {
        this.shouldUpdate = new SimpleBooleanProperty(false);
        this.tasksConsumer = tasksConsumer;
        this.worker = null;
    }

    @Override
    public void run() {
        if (!shouldUpdate.get()) {
            return;
        }
        tasksConsumer.accept(null);
    }

    public void pause() {
        shouldUpdate.set(false);
    }

    public void resume() {
        shouldUpdate.set(true);
    }

}
