package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	private static final String SCHEMA_FILE = "project_schema.sql";
	private static final String DATA_FILE = "project_data.sql";
	
    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public Project fetchProjectById(Integer projectId) {
        return projectDao.fetchProjectById(projectId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Project with id=" + projectId + " does not exist"));
    }
    public void createAndPopulateTables() {
    	loadFromFile(SCHEMA_FILE);
    	loadFromFile(DATA_FILE);
    }
    
    public void deleteProject(Integer projectId) {
        if (!projectDao.deleteProject(projectId)) {
            throw new DbException("Project with ID=" + projectId + " does not exist.");
        }
    }

    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    public void modifyProjectDetails(Project project) {
        projectDao.modifyProjectDetails(project);
    }
}
