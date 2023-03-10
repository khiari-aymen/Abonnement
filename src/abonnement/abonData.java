
package abonnement;

import java.sql.Date;
import java.time.LocalDate;


public class abonData {
    private int abonId;
    private int clientId;
    private String type;
    private Date dateDeb;
    private String duree;
    private int pointsFidelite; 

    
    public abonData(Integer abonId, Integer clientId, String type , Date dateDeb, String duree){
        this.abonId = abonId;
        this.clientId = clientId;
        this.type = type;
        this.dateDeb = dateDeb;
        this.duree = duree;
        this.pointsFidelite = 0; 
    }
    
    public abonData(){
    }
    
    public Integer getAbonnId(){
        return abonId;
    }
    public Integer getClientId(){
        return clientId;
    }
    public String getType(){
        return type;
    }
    public Date getDateDeb(){
        return dateDeb;
    }
    public String getDuree(){
        return duree;
    }
    
    public Integer getPointsFidelite() { 
        return pointsFidelite;
    }

    public void setPointsFidelite(Integer pointsFidelite) { 
        this.pointsFidelite = pointsFidelite;
    }
}
