package engine;

import dto.execution.ExecutionDTO;
import dto.execution.RunExecutionDTO;
import dto.execution.WorkerExecutionDTO;
import dto.execution.config.*;
import dto.graph.GraphDTO;
import dto.target.TargetDTO;
import dto.util.DTOUtil;
import engine.exceptions.GraphExistException;
import engine.execution.Execution;
import engine.execution.ExecutionType;
import engine.graph.TargetGraph;
import engine.jaxb.generated.v3.GPUPDescriptor;
import engine.jaxb.parser.GPUPParser;
import engine.target.TargetsRelationType;
import engine.users.UserManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class NewEngine {
    private Map<String, TargetGraph> graphsList;
    private Map<String, Execution> tasksList;
    private UserManager userManager;

    public NewEngine() {
        graphsList = new HashMap<>();
        tasksList = new HashMap<>();
        userManager = new UserManager();
        File workDir = new File("C:\\gpup-working-dir");
        workDir.mkdir();
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void parseFileToGraph(File file, String creatingUser) throws FileNotFoundException, JAXBException {
        final String PACKAGE_NAME = "engine.jaxb.generated.v3";
        GPUPDescriptor gpupDescriptor;

        InputStream inputStream = new FileInputStream(file);
        JAXBContext jc = JAXBContext.newInstance(PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        gpupDescriptor = (GPUPDescriptor) u.unmarshal(inputStream);
        TargetGraph graph = GPUPParser.parseGraph(gpupDescriptor, creatingUser);

        checkGraph(graph); // throw exception that will be handle in servlet: circuit, existing already
        addGraph(graph);

    }

    private void checkGraph(TargetGraph graph) {
        if (graphsList.containsKey(graph.getName())) {
            throw new GraphExistException(graph.getName());
        }
//        if (graph.hasCircuit()) {
//            throw new CircuitInGraphException();
//        }
    }

    private void addGraph(TargetGraph graph) {
        if (!graphsList.containsKey(graph.getName())) {
            graphsList.put(graph.getName(), graph);
        }
    }

    public List<GraphDTO> getGraphsDTO() {
        List<GraphDTO> list = new ArrayList<>();
        graphsList.forEach((s, targetGraph) -> list.add(new GraphDTO(targetGraph)));
        return list;
    }

    public List<String> getPathsDTO(String graphName, String fromTargetName, String toTargetName, TargetsRelationType relationType) {
        if (graphsList.containsKey(graphName)) {
            return graphsList.get(graphName).findPaths(fromTargetName, relationType, toTargetName);
        }
        return null;
    }

    public boolean isTargetExistInGraph(String graphName, String targetName) {
        if (graphsList.containsKey(graphName)) {
            return graphsList.get(graphName).isTargetExist(targetName);
        }
        return false;
    }

    public List<String> getGraphTargetListByName(String graphName) {
        if (graphsList.containsKey(graphName)) {
            return graphsList.get(graphName).getTargetsListByName();
        } else {
            return null;
        }
    }

    public boolean isGraphExist(String graphName) {
        return graphsList.containsKey(graphName);
    }

    public List<String> findCircuit(String graphName, String src) {
        if (graphsList.containsKey(graphName)) {
            return graphsList.get(graphName).findCircuit(src);
        }
        return null;

    }

    public List<TargetDTO> whatIf(String graphName, String src, String relationType) {
        TargetsRelationType type = relationType.equals("DependsOn") ? TargetsRelationType.DependsOn : TargetsRelationType.RequiredFor;
        if (graphsList.containsKey(graphName)) {
            return graphsList.get(graphName).whatIfDTO(src, type);
        }
        return null;
    }

    public void addExecution(ExecutionConfigDTO execConfig) {
        try {
            Execution execution = parseConfigToExecution(execConfig);
            synchronized (this) {
                tasksList.put(execution.getName(), execution);
            }
        } catch (RuntimeException ex) {
            System.out.println("Null exception!");
            System.out.println(ex.getMessage());
            ////// ADD //////
        }
    }

    private Execution parseConfigToExecution(ExecutionConfigDTO execConfig) {
        Execution execution = new Execution();
        execution.setName(execConfig.getName());
        execution.setCreatingUser(execConfig.getCreatingUser());
        execution.setPrice(execConfig.getPrice());
        execution.setType(ExecutionType.valueOf(execConfig.getExecutionType().name()));
        execution.createTaskGraph(execConfig, graphsList.get(execConfig.getGraphName()));
        execution.setExecutionDetails(parseExecutionConfig(execConfig));
        return execution;
    }

    private ConfigDTO parseExecutionConfig(ExecutionConfigDTO execConfig) {
        if (execConfig.getExecutionType().equals(DTOUtil.ExecutionTypeDTO.Compilation)) {
            CompilationConfigDTO details = new CompilationConfigDTO();
            CompilationConfigDTO configDTO = (CompilationConfigDTO) execConfig.getConfigDTO();
            details.setDestDir(configDTO.getDestDir());
            details.setSrcDir(configDTO.getSrcDir());
            return details;

        } else if (execConfig.getExecutionType().equals(DTOUtil.ExecutionTypeDTO.Simulation)) {
            SimulationConfigDTO details = new SimulationConfigDTO();
            SimulationConfigDTO configDTO = (SimulationConfigDTO) execConfig.getConfigDTO();
            details.setIsRandom(configDTO.getIsRandom());
            details.setProcessingTime(configDTO.getProcessingTime());
            details.setSuccessProb(configDTO.getSuccessProb());
            details.setSuccessWithWarningsProb(configDTO.getSuccessWithWarningsProb());
            return details;
        }
        return null;
    }

    public List<ExecutionDTO> getTasksDTO() {
        List<ExecutionDTO> list = new ArrayList<>();
        tasksList.forEach((s, task) -> list.add(new ExecutionDTO(task)));
        return list;
    }

    public void addExecution(ExistExecutionConfigDTO exeConfigDTO) {
        try {
            Execution execution = parseExistExeConfigToExecution(exeConfigDTO);
            synchronized (this) {
                tasksList.put(execution.getName(), execution);
            }
        } catch (RuntimeException ex) {
            System.out.println("Null exception!");
            System.out.println(ex.getMessage());
            ////// ADD //////
        }
    }

    private Execution parseExistExeConfigToExecution(ExistExecutionConfigDTO exeConfigDTO) {
        Execution execution = new Execution();
        Execution baseExec = tasksList.get(exeConfigDTO.getBaseName());
        execution.setName(generateExistingExecutionName(exeConfigDTO.getBaseName()));
        execution.setCreatingUser(baseExec.getCreatingUser());
        execution.setPrice(baseExec.getPrice());
        execution.setType(baseExec.getType());
        execution.createTaskGraph(baseExec.getTaskGraph());
        execution.setExecutionDetails(baseExec.getExecutionDetails());
        if (!exeConfigDTO.isIncremental()) // from scratch -> reset all targets run result
        {
            execution.fromScratchReset();
        } else {
            execution.checkValidIncremental();        // if incremental chosen, and all targets are finished -> from scratch
        }
        return execution;
    }

    private String generateExistingExecutionName(String baseName) {
        Integer i = 1;
        while (tasksList.containsKey(baseName.concat(i.toString()))) {
            i++;
        }
        return baseName.concat(i.toString());
    }

    public synchronized void registerWorker(String workerName, String taskName) {
        tasksList.get(taskName).addWorker(workerName);
    }

    public synchronized void unregisterWorker(String workerName, String taskName) {
        tasksList.get(taskName).removeWorker(workerName);
    }

    public synchronized WorkerExecutionDTO getWorkerExecution(String taskName) {
        return new WorkerExecutionDTO(tasksList.get(taskName));
    }

    public boolean isRegistered(String username, String taskName) {
        return tasksList.get(taskName).isWorkerExist(username);
    }

    public boolean isExecutionExist(String taskName) {
        return tasksList.containsKey(taskName);
    }

    public Double getExecutionProgress(String taskName) {
        return tasksList.get(taskName).getProgress();
    }

    public synchronized RunExecutionDTO getRunExecutionDTO(String taskName) {
        return new RunExecutionDTO(tasksList.get(taskName));
    }

    public void playExecution(String taskName) {
        new Thread(() -> tasksList.get(taskName).play()).start(); // so the servlet's thread will not do this ???
    }

    public boolean isExecutionCreatingUser(String task, String username) {
        return Objects.equals(tasksList.get(task).getCreatingUser(), username);
    }

    public void pauseExecution(String taskName) {
        tasksList.get(taskName).pause();
    }

    public void resumeExecution(String taskName) {
        tasksList.get(taskName).resume();
    }

    public void stopExecution(String taskName) {
        tasksList.get(taskName).stop();
    }

    public boolean isAllowedToPlayExecution(String taskName) {
        return tasksList.get(taskName).isAllowedToPlay();
    }
}
