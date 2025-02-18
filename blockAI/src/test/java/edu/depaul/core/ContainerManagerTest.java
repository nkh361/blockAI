package edu.depaul.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerManagerTest {
    private ContainerManager containerManager;
    private FileSystemHelper fileSystemHelper;

    @BeforeEach
    void setup() {
        fileSystemHelper = new FileSystemHelper();
        containerManager = new ContainerManager(fileSystemHelper);
    }

    @Test
    @DisplayName("Test container creation")
    public void testContainerCreation() {
        String containerID = containerManager.createContainer();
        assertNotNull(containerID, "ContainerID should not be null");

        File containerDir = new File(System.getProperty("user.home") + "/ai-containers/" + containerID);
        assertTrue(containerDir.exists(), "Container directory does not exist");
    }

    @Test
    @DisplayName("Test starting container")
    public void testStartingContainer() {
        String containerID = containerManager.createContainer();

        // check if unshare and chroot are available
        File unshare = new File("/usr/bin/unshare");
        File chroot = new File("/usr/sbin/chroot");
        if (!unshare.exists() || !chroot.exists()) {
            System.out.println("Skipping test: unshare or chroot is missing");
            return;
        }

        assertDoesNotThrow(() -> containerManager.startContainer(containerID, "echo Hello"),
                "Container should start running without exceptions");
    }

    @Test
    @DisplayName("Test stopping a container")
    public void testStoppingContainer() {
        String containerID = containerManager.createContainer();
        containerManager.startContainer(containerID, "sleep 5");
        assertDoesNotThrow(() -> containerManager.stopContainer(containerID),
                "Container should stop running without exceptions");
    }

    @Test
    @DisplayName("Test listing running containers")
    public void testListingRunningContainers() {
        String containerID = containerManager.createContainer();
        containerManager.startContainer(containerID, "echo Running");
        assertDoesNotThrow(() -> containerManager.listRunningContainers(), "Listing running containers should not throw exceptions");
    }

    @Test
    @DisplayName("Test stopping a non-existent container")
    public void testStoppingNonExistentContainer() {
        String randomID = UUID.randomUUID().toString();
        assertDoesNotThrow(() -> containerManager.stopContainer(randomID),
                "Stopping a non-existent container should not throw exceptions");
    }

}