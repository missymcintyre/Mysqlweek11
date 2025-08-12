package projects.service;

import java.util.List;
import java.util.NoSuchElementException;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    public Project fetchProjectById(Integer projectId) {
		return projectDao.fetchProjectById(projectId)
				.orElseThrow(() -> new NoSuchElementException("Project with ID=" + projectId + " does not exist."));
	} // end fetchProjectByIdService

    public void modifyProjectDetails(Project project) {
        boolean updated = projectDao.modifyProjectDetails(project);
        if (!updated) {
            throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
        }
    }

    public void deleteProject(Integer projectId) {
        boolean deleted = projectDao.deleteProject(projectId);
        if (!deleted) {
            throw new DbException("Project with ID=" + projectId + " does not exist.");
        }
    }
}


