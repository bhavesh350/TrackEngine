package net.mzi.trackengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Poonam on 6/14/2017.
 */
@IgnoreExtraProperties
public class FirebaseTicketData {

    private String Action;
   /* private String Attended;
    private String Flag;
    private String IssueId;
    private String NotificationMessage;*/
/*
     String Action;
     String Attended;
     String Flag;
     String IssueId;
     String NotificationMessage;*/

    /*, String Attended, String Flag, String IssueId, String NotificationMessage*/

    public void FirebaseTicketData(String Action){

        this.Action=Action;
      /*  this.NotificationMessage=NotificationMessage;
        this.IssueId=IssueId;
        this.Flag=Flag;
        this.Attended=Attended;*/
    }

    public String getAction() {
        return Action;
    }
    /*public String getIssueID(){return IssueId;}
    public String getNotificationMessage() {
        return NotificationMessage;
    }
    public String getFlag() {
        return Flag;
    }
    public String getAttended() {
        return Attended;
    }*/

    @Override
    public String toString() {
        return "value = {Action="+Action+"};";
    }

    /*@Override
    public String toString() {
        return "value = {IssueId="+IssueId+", Flag="+Flag+", Action="+Action+",Attended="+Attended+", NotificationMessage="+NotificationMessage+"};";
    }*/
}
