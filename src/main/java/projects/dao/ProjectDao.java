package projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import projects.entity.Project;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {

    /** CREATE - Insert a new project */
    public Project insertProject(Project project) {
        String sql = ""
            + "INSERT INTO project "
            + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, project.getProjectName());
                stmt.setBigDecimal(2, project.getEstimatedHours());
                stmt.setBigDecimal(3, project.getActualHours());
                stmt.setObject(4, project.getDifficulty(), java.sql.Types.INTEGER);
                stmt.setString(5, project.getNotes());

                stmt.executeUpdate();
                Integer projectId = getLastInsertId(conn, "project");

                commitTransaction(conn);
                project.setProjectId(projectId);
                return project;

            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** READ - Fetch all projects */
    public List<Project> fetchAllProjects() {
        String sql = "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes "
                   + "FROM project ORDER BY project_name";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    List<Project> projects = new LinkedList<>();

                    while (rs.next()) {
                        projects.add(extract(rs, Project.class));
                    }

                    commitTransaction(conn);
                    return projects;
                } catch (Exception e) {
                    rollbackTransaction(conn);
                    throw new DbException(e);
                }
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** READ - Fetch project by ID */
    public Project fetchProjectById(Integer projectId) {
        String sql = "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes "
                   + "FROM project WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);

                try (ResultSet rs = stmt.executeQuery()) {
                    Project project = null;

                    if (rs.next()) {
                        project = extract(rs, Project.class);
                    }

                    commitTransaction(conn);
                    return project;
                } catch (Exception e) {
                    rollbackTransaction(conn);
                    throw new DbException(e);
                }
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** UPDATE - Modify project details */
    public boolean modifyProjectDetails(Project project) {
        String sql = ""
            + "UPDATE project "
            + "SET project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? "
            + "WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, project.getProjectName());
                stmt.setBigDecimal(2, project.getEstimatedHours());
                stmt.setBigDecimal(3, project.getActualHours());
                stmt.setObject(4, project.getDifficulty(), java.sql.Types.INTEGER);
                stmt.setString(5, project.getNotes());
                stmt.setInt(6, project.getProjectId());

                boolean updated = stmt.executeUpdate() == 1;
                commitTransaction(conn);
                return updated;
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /** DELETE - Remove a project by ID */
    public boolean deleteProject(Integer projectId) {
        String sql = "DELETE FROM project WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);

                boolean deleted = stmt.executeUpdate() == 1;
                commitTransaction(conn);
                return deleted;
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }
}