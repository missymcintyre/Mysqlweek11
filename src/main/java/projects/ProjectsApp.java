package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
    private Scanner scanner = new Scanner(System.in);
    private ProjectService projectService = new ProjectService();
    private Project curProject;

    // Menu options
    private List<String> operations = List.of(
        "1) Add a project",
        "2) List projects",
        "3) Select a project",
        "4) Update project details",
        "5) Delete a project"
    );

    public static void main(String[] args) {
        new ProjectsApp().processUserSelections();
    }

    private void processUserSelections() {
        boolean done = false;
        while (!done) {
            try {
                int selection = getUserSelection();
                switch (selection) {
                    case -1:
                        done = true;
                        break;
                    case 1:
                        createProject();
                        break;
                    case 2:
                        listProjects();
                        break;
                    case 3:
                        selectProject();
                        break;
                    case 4:
                        updateProjectDetails();
                        break;
                    case 5:
                        deleteProject();
                        break;
                    default:
                        System.out.println("\n" + selection + " is not a valid selection. Try again.");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.toString());
            }
        }
    }

    private int getUserSelection() {
        printOperations();
        Integer input = getIntInput("Enter a menu selection or press Enter to quit");
        return Objects.isNull(input) ? -1 : input;
    }

    private void printOperations() {
        System.out.println("\nThese are the available selections. Press Enter to quit:");
        operations.forEach(line -> System.out.println("  " + line));
        if (Objects.nonNull(curProject)) {
            System.out.println("\nYou are working with project: " + curProject);
        }
    }

    // CREATE
    private void createProject() {
        String projectName = getStringInput("Enter the project name");
        BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
        BigDecimal actualHours = getDecimalInput("Enter the actual hours");
        Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
        String notes = getStringInput("Enter the project notes");

        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        Project dbProject = projectService.addProject(project);
        System.out.println("You have successfully created project: " + dbProject);
    }

    // READ (List)
    private void listProjects() {
        List<Project> projects = projectService.fetchAllProjects();
        System.out.println("\nProjects:");
        projects.forEach(p -> System.out.println("   " + p.getProjectId() + ": " + p.getProjectName()));
    }

    // READ (Select)
    private void selectProject() {
        listProjects();
        Integer projectId = getIntInput("Enter a project ID to select");
        curProject = null;
        curProject = projectService.fetchProjectById(projectId);
    }

    // UPDATE
    private void updateProjectDetails() {
        if (Objects.isNull(curProject)) {
            System.out.println("\nPlease select a project.");
            return;
        }

        System.out.println("\nEnter the new values for the project. Press Enter to keep the current value.");

        String projectName = getStringInput("Project name [" + curProject.getProjectName() + "]");
        BigDecimal estimatedHours = getDecimalInput("Estimated hours [" + curProject.getEstimatedHours() + "]");
        BigDecimal actualHours = getDecimalInput("Actual hours [" + curProject.getActualHours() + "]");
        Integer difficulty = getIntInput("Difficulty [" + curProject.getDifficulty() + "]");
        String notes = getStringInput("Notes [" + curProject.getNotes() + "]");

        Project updatedProject = new Project();
        updatedProject.setProjectId(curProject.getProjectId());
        updatedProject.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
        updatedProject.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
        updatedProject.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
        updatedProject.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
        updatedProject.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

        projectService.modifyProjectDetails(updatedProject);

        curProject = projectService.fetchProjectById(curProject.getProjectId());
    }

    // DELETE
    private void deleteProject() {
        listProjects();
        Integer projectId = getIntInput("Enter the project ID to delete");

        if (Objects.isNull(projectId)) {
            System.out.println("\nNo project selected.");
            return;
        }

        projectService.deleteProject(projectId);
        System.out.println("Project " + projectId + " deleted.");

        if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
            curProject = null;
        }
    }

    // Helpers
    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        String line = scanner.nextLine();
        return line.isBlank() ? null : line.trim();
    }

    private Integer getIntInput(String prompt) {
        String input = getStringInput(prompt);
        return Objects.isNull(input) ? null : Integer.valueOf(input);
    }

    private BigDecimal getDecimalInput(String prompt) {
        String input = getStringInput(prompt);
        return Objects.isNull(input) ? null : new BigDecimal(input).setScale(2);
    }
}


