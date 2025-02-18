package edu.depaul.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.UUID;

public class ContainerManager {
    private static final String CONTAINER_PATH = System.getProperty("user.home") + "/ai-containers/";
    private Map<String, Process> runningContainers = new HashMap<>();
    private FileSystemHelper fileSystemHelper;

    public ContainerManager(FileSystemHelper fileSystemHelper) {
        this.fileSystemHelper = fileSystemHelper;
    }

    /**
     * Creates a new container with isolated filesystem.
     *
     * FIXME: looks like this does not yet have the permissions to create a directory in /var/lib/ai-containers
     *
     * @return                  container ID
     */
    public String createContainer() {
        String containerID = UUID.randomUUID().toString();
        String containerPath = CONTAINER_PATH + containerID;

        if (!fileSystemHelper.createDirectory(containerPath)) {
            throw new IllegalStateException("Failed to create container directory: " + containerPath);
        }
        System.out.println("Created container: " + containerPath);
        return containerID;
    }

    /**
     * Start a container process in an isolated namespace
     *
     * @param containerID       the ID of the container
     * @param command           the command to run inside the container
     */
    public void startContainer(String containerID, String command) {
        String containerPath = CONTAINER_PATH + containerID;
        File containerDir = new File(containerPath);

        if (!containerDir.exists()) {
            throw new IllegalStateException("container directory does not exist: " + containerPath);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("unshare", "--pid", "--mount", "--net", "--uts", "--ipc",
                "chroot", containerPath, "/bin/sh", "-c", command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            runningContainers.put(containerID, process);
            System.out.println("Starting container " + containerID);
        }
        catch (IOException e) {
            System.err.println("Failed to start container " + containerID + ": " + e.getMessage());
        }
    }

    /**
     * Stop a running container
     *
     * @param containerID         the ID of the container
     */
    public void stopContainer(String containerID) {
        Process process = runningContainers.get(containerID);
        if (process != null) {
            process.destroy();
            runningContainers.remove(containerID);
            System.out.println("Stopping container " + containerID);
        }
        else {
            System.out.println("Container " + containerID + " not found");
        }
    }

    /**
     * List all running containers
     */
    public void listRunningContainers() {
        if (runningContainers.isEmpty()) {
            System.out.println("No running containers found");
        }
        else {
            runningContainers.keySet().forEach(containerID -> System.out.println("Running container: " + containerID));
        }
    }
}
