/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author c0661137
 */
@Named
@ApplicationScoped
public class Posts {

    private List<Post> posts = new ArrayList();
    private Post currentPost;

    public Posts() {
        currentPost = new Post(-1, -1, "", null, "");
        getPostsFromDB();
    }

    public String newPost() {
        currentPost = new Post(-1, -1, "", null, "");
        return "newPost";
    }

    public String createPost(User currentUser) {
        try (Connection conn = DBUtils.getConnection();) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO posts VALUES (" + findPostId() + "," + currentUser.getId() + ",'" + currentPost.getTitle() + "', Now(),'" + currentPost.getContents() + "')");
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
        }
        getPostsFromDB();
        return "index";
    }

    /**
     * Wipe the Posts list and update it from the DB
     */
    private void getPostsFromDB() {
        try (Connection conn = DBUtils.getConnection()) {
            posts = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM posts");
            while (rs.next()) {
                Post p = new Post(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getTimestamp("created_time"),
                        rs.getString("contents")
                );
                posts.add(p);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
            // This Fails Silently -- Sets Post List as Empty
            posts = new ArrayList<>();
        }
    }

    public List<Post> getPosts() {
        return posts;
    }

    public Post getCurrentPost() {
        return currentPost;
    }

    public Post getPostById(int id) {
        for (Post p : posts) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
     private int findPostId(){
         int b = 0;
         for(Post p: posts){
             if(p.getId() >b){
                 b = p.getId();
             }
         }
         return b+1;
     }
    public Post getPostByTitle(String title) {
        for (Post p : posts) {
            if (p.getTitle().equals(title)) {
                return p;
            }
        }
        return null;
    }

    public String viewPost(Post post) {
        currentPost = post;
        return "viewPost";
    }

    public String addPost() {
        currentPost = new Post(-1, -1, "", null, "");
        return "editPost";
    }

    public String editPost() {
        return "editPost";
    }

    public String cancelPost() {
        // currentPost can be corrupted -- reset it based on the DB
        int id = currentPost.getId();
        getPostsFromDB();
        currentPost = getPostById(id);
        return "viewPost";
    }

    public String deletePost() {
        try (Connection conn = DBUtils.getConnection();) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM posts WHERE id =" + currentPost.getId());
            getPostsFromDB();
            return "index";
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "index";
    }

    public String savePost(User user) {
        try (Connection conn = DBUtils.getConnection()) {
            // If there's a current post, update rather than insert
            if (currentPost.getId() >= 0) {
                String sql = "UPDATE posts SET title = ?, contents = ? WHERE id = ?";
                PreparedStatement statment = conn.prepareStatement(sql);
                statment.setString(1, currentPost.getTitle());
                statment.setString(2, currentPost.getContents());
                statment.setInt(3, currentPost.getId());
                
                System.out.println("Update "+statment.executeUpdate());
            } else {
                String sql = "INSERT INTO posts (user_id, title, created_time, contents) VALUES (?,?,NOW(),?)";
                PreparedStatement statment = conn.prepareStatement(sql);
                statment.setInt(1, user.getId());
                statment.setString(2, currentPost.getTitle());
                statment.setString(3, currentPost.getContents());
                System.out.println("Insert "+statment.executeUpdate());
            }
        } catch (SQLException ex) {
            Logger.getLogger(Posts.class.getName()).log(Level.SEVERE, null, ex);
        }
        getPostsFromDB();
        // Update the currentPost so that its details appear after navigation
        //System.out.println(currentPost.getTitle());
        currentPost = getPostByTitle(currentPost.getTitle());
        return "viewPost";
    }
}
