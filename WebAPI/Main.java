import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import javax.servlet.MultipartConfigElement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import javax.servlet.http.Part;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Main {

  public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    get("/stl", (request, response) -> { 

      Connection connection = null;
      Map<String, Object> attributes = new HashMap<>();
      try {
        connection = DatabaseUrl.extract().getConnection();

        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery( "SELECT stl_data FROM models WHERE model_name = '" + request.raw().getParameter("param1") + "'");

        byte[] model_byte_data = null;
        if (rs != null) {
            while (rs.next()) {
                model_byte_data = rs.getBytes(1);
            }
            rs.close();
        } else {
          return "null result set";
        }

        if (model_byte_data == null) {
          return "no byte data";
        }

        return rs;
      } catch (Exception e) {
        return e; //"you're fucked, bucko";
      } finally {
        if (connection != null) try{connection.close();} catch(SQLException g){}
      }

    });

    get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

  }

}
