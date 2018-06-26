package net.mzi.trackengine.model;

/**
 * Created by Poonam on 1/11/2018.
 */

public class InternalIssueClass {
    /*        {"Id":10,"CategoryName":"Mobile","Subject":"testmobile",
                "ComplainText":"mobile testing","CreatedOn":"11-01-2018 18:04:23",
                "CreatedBy":"Gaurab Guha","Company":"Test","Location":"Test",
                "LastUpdateOn":"11-01-2018 18:04:23","LastComment":"Complain created.",
                "StatusName":"Open","LastActionBy":"Gaurab Guha","LastActionByType":"External",
                "CreationSource":"Mobile","HasError":false,"Error":""}*/
    public String IssueID;
    public String CreatedDate;
    public String UpdatedDate;
    public String CreatedBy;
    public String Subject;
    public String IssueText;
    public String StatusName;

    public void InternalIssueClass(String IssueID,String CreatedDate, String UpdatedDate,String CreatedBy,String Subject,String IssueText,String StatusName){
        this.IssueID=IssueID;
        this.CreatedDate=CreatedDate;
        this.UpdatedDate=UpdatedDate;
        this.CreatedBy=CreatedBy;
        this.Subject=Subject;
        this.IssueText=IssueText;
        this.StatusName=StatusName;

    }

    public void InternalIssueClass(){

    }

}
