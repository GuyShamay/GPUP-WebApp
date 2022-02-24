package worker.logic.task;

import dto.target.FinishResultDTO;
import worker.logic.target.TaskTarget;

public interface Task {
   public void run(TaskTarget target) throws InterruptedException;
}
