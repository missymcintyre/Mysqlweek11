package projects.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {

	  private static final String CATEGORY_TABLE = "category";
	  private static final String MATERIAL_TABLE = "material";
	  private static final String PROJECT_TABLE = "project";
	  private static final String PROJECT_CATEGORY_TABLE = "project_category";
	  private static final String STEP_TABLE = "step";
	  
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
    public Optional <Project> fetchProjectById(Integer projectId) {
        String sql = "SELECT project_id, project_name, estimated_hours, actual_hours, difficulty, notes "
                   + "FROM project WHERE project_id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);
            
            try {
            	Project project = null;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, projectId);

                try(ResultSet rs = stmt.executeQuery()) {
                    if(rs.next()) {
                      project = extract(rs, Project.class);
                    }
                  }
                }

                /*
                 * This null check isn't expressly needed because if the project ID is invalid, each method
                 * will simply return an empty list. However, it avoids three unnecessary database calls.
                 */
                if(Objects.nonNull(project)) {
                  project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
                  project.getSteps().addAll(fetchStepsForProject(conn, projectId));
                  project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
                }

                commitTransaction(conn);

                /*
                 * Optional.ofNullable() is used because project may be null at this point if the given
                 * project ID is invalid.
                 */
                return Optional.ofNullable(project);
              }
              catch(Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
              }
            }
            catch(SQLException e) {
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
    private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) {
        // @formatter:off
        String sql = ""
            + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
            + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
            + "WHERE project_id = ?";
        // @formatter:on

        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, projectId, Integer.class);

          try(ResultSet rs = stmt.executeQuery()) {
            List<Category> categories = new LinkedList<>();

            while(rs.next()) {
              categories.add(extract(rs, Category.class));
            }

            return categories;
          }
        }
        catch(SQLException e) {
          throw new DbException(e);
        }
      }

      /**
       * This method uses JDBC method calls to retrieve project steps for the given project ID. The
       * connection is supplied by the caller so that steps can be retrieved on the current transaction.
       * 
       * @param conn The caller-supplied connection.
       * @param projectId The project ID used to retrieve the steps.
       * @return A list of steps in step order.
       * @throws SQLException Thrown if the database driver encounters an error.
       */
      private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, projectId, Integer.class);

          try(ResultSet rs = stmt.executeQuery()) {
            List<Step> steps = new LinkedList<>();

            while(rs.next()) {
              steps.add(extract(rs, Step.class));
            }

            return steps;
          }
        }
      }

      /**
       * This method uses JDBC method calls to retrieve project materials for the given project ID. The
       * connection is supplied by the caller so that project materials can be retrieved on the current
       * transaction.
       * 
       * @param conn The caller-supplied connection.
       * @param projectId The project ID used to retrieve the materials.
       * @return A list of materials.
       * @throws SQLException Thrown if the database driver encounters an error.
       */
      private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)
          throws SQLException {
        String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, projectId, Integer.class);

          try(ResultSet rs = stmt.executeQuery()) {
            List<Material> materials = new LinkedList<>();

            while(rs.next()) {
              materials.add(extract(rs, Material.class));
            }

            return materials;
          }
        
      }

    }

}