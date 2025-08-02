package projects;

import java.math.BigDecimal;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.service.ProjectService;
import projects.exception.DbException;


public class ProjectsApp {
    private Scanner scanner = new Scanner(System.in);


private ProjectService projectService = new ProjectService();
private Project curProject;

 
	  // @formatter:off
  private List<String> operations = List.of(
      "1) Add a project",
      "2) List projects",
      "3) Select a project",
      "4) Modify a project",
      "5) Delete a project"
      
  );
  // @formatter:on

  public static void main(String[] args) {
	  new ProjectsApp().processUserSelections();
	// System.out.println("compile test successful.");  
  }
  
  private void processUserSelections() {
	    boolean done = false;

	    while (!done) {
	        try {
	            printOperations(); // ✅ Show the menu

	            int selection = getUserSelection();

	            switch (selection) {
	                case -1:
	                    done = exitMenu();
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
	                    break;
	            }
	        } catch (Exception e) {
	            System.out.println("\nError: " + e.getMessage() + " Try again.");
	        }
	    }
	}


  private void updateProjectDetails() {
	    if (Objects.isNull(curProject)) {
	        System.out.println("\nPlease select a project first.");
	        return;
	    }

	    String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
	    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
	    BigDecimal actualHours = getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");
	    Integer difficulty = getIntInput("Enter the project difficulty (1-5) [" + curProject.getDifficulty() + "]");
	    String notes = getStringInput("Enter the project notes [" + curProject.getNotes() + "]");

	    Project updatedProject = new Project();
	    updatedProject.setProjectId(curProject.getProjectId());
	    updatedProject.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
	    updatedProject.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
	    updatedProject.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
	    updatedProject.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
	    updatedProject.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

	    projectService.modifyProjectDetails(updatedProject);

	    
	    curProject = projectService.fetchProjectById(curProject.getProjectId());
	    if (curProject == null) {
	        System.out.println("Error: Could not reload the updated project.");
	    } else {
	        System.out.println("Project updated successfully.");
	    }
	}
  
	  private List<Project> listProjects() {
	    List<Project> projects = projectService.fetchAllProjects();

	    System.out.println("\nProjects:");
	    
	    projects.forEach(project -> System.out
	    		.println("   " + project.getProjectId() + ": " + project.getProjectName()));

	    return projects; // ✅ Add this line
	}

  
	private void deleteProject() {
	    listProjects();
	    Integer projectId = getIntInput("Enter the ID of the project to delete");

	    if (Objects.nonNull(projectId)) {
	      projectService.deleteProject(projectId);

	      System.out.println("You have deleted project " + projectId);
	    
	      if (Objects.nonNull(curProject) 
	          && curProject.getProjectId().equals(projectId)) {
		        curProject = null;
		      }
			}
		}
  

  	private void selectProject() {
  		listProjects();
  		Integer projectId = getIntInput("Enter a project ID to select a project");

  		curProject = projectService.fetchProjectById(projectId);
  		if (curProject == null) {
  		    System.out.println("Invalid project ID selected.");
  		}
  	}


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

  private BigDecimal getDecimalInput(String prompt) {
    String input = getStringInput(prompt);

    if(Objects.isNull(input)) {
      return null;
    }

    try {
      /* Create the BigDecimal object and set it to two decimal places (the scale). */
      return new BigDecimal(input).setScale(2);
    }
    catch(NumberFormatException e) {
      throw new DbException(input + " is not a valid decimal number.");
    }
  }

  private boolean exitMenu() {
    System.out.println("Exiting the menu.");
    return true;
  }

  private int getUserSelection() {
	    Integer input = getIntInput("Enter a menu selection");
	    return Objects.isNull(input) ? -1 : input;
	}

  /*private int getUserSelection() {
	    printOperations(); // Print menu before getting input

	    Integer input = getIntInput("Enter a menu selection");

	    return Objects.isNull(input) ? -1 : input;
	}
*/
  private Integer getIntInput(String prompt) {
    String input = getStringInput(prompt);

    if(Objects.isNull(input)) {
      return null;
    }

    try {
      return Integer.valueOf(input);
    }
    catch(NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }


  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    String input = scanner.nextLine();

    return input.isBlank() ? null : input.trim();
  }


  private void printOperations() {
	  System.out.println("\nThese are the available selections (press Enter to exit):");

    operations.forEach(line -> System.out.println("  " + line));


    if(Objects.isNull(curProject)) {
      System.out.println("\nYou are not working with a project.");
    }
    else {
      System.out.println("\nYou are working with project: " + curProject);
    }
  }
}


