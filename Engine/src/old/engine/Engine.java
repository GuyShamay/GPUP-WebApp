package old.engine;

import engine.target.TargetsRelationType;
import old.component.task.ProcessingType;
import old.component.task.RunTask;
import old.component.task.config.TaskConfig;
import old.dto.*;
import old.exception.ElementExistException;
import javafx.collections.ObservableList;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

public interface Engine {
    void buildGraphFromXml(String path) throws JAXBException, FileNotFoundException, ElementExistException;

    void buildGraphFromXml(File file) throws JAXBException, FileNotFoundException, ElementExistException;

    TargetDTO getTargetInfo(String name);

    TargetGraphDTO getGraphInfo();

    boolean isInitialized();

    void initTask(TaskConfig taskConfig);

    void initTaskGPUP1(int targetProcessingTimeMs, int taskProcessingTimeType, float successProb, float successWithWarningsProb, ProcessingType status);

    void setProcessingType(ProcessingType processingStartStatus);

    PathsDTO findPaths(String src, String dest, TargetsRelationType type);

    boolean isFirstTaskRun();

    CircuitDTO findCircuit(String targetName);

    List<TargetInfoDTO> getTargetsInfo();

    Set<String> getAllTargetsByName();

    List<SerialSetDTO> getSerialSetInfo();

    List<TargetInfoDTO> getTargetsByRelation(String target, TargetsRelationType relationType);

    int getMaxParallelism();

    default void runTask(){}

    void resume();

    void pause();

    RunTask getCurrTask();

    ObservableList<String> getList(String runStatus);
    void increaseThreadsNum(Integer newVal);

    boolean isCircuit();

    Set<String> getCurrentTaskTargetByName();

    TargetInfoDTO getTargetInfoDTO(String name);
}
