package abonnement;

import java.sql.Connection;
import java.sql.DriverManager;

public class AbonDB {
        public static Connection connectDb(){
        
        try{
            
            Class.forName("com.mysql.jdbc.Driver");                     
            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/abonnementkh", "root", ""); 
            return connect;
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    
}

