package net.mzi.trackengine;

/**
 * Created by Poonam on 3/7/2017.
 */

public class StatusInfoClass {
    /*public String StatusId;
    public String Active;
    public String AutoUserUnassign;
    public String CommentRequired;
    public String IsDeleted;
    public String IsDefault;
    public String MainStatusId;
    public String HoldTat;
    public String Movement;
    public String SystemDefine;
    public String MobileAcceptStatus;
    public String Name;
    public String ParentStatus;
    public String UserChangeable;*/

    private int StatusId;
    //private int Active;
    //private int AutoUserUnassign;
    private int CommentRequired;
    //private int IsDeleted;
    //private int IsDefault;
    private int MainStatusId;
    //private int HoldTat;
    //private String Movement;
    //private int SystemDefine;
    //private int MobileAcceptStatus;
    private String Name;
    private int ParentStatus;
    //private int UserChangeable;


   public void StatusInfoClass(int StatusId ,int CommentRequired, int MainStatusId,int ParentStatus, String Name){
       this.StatusId=StatusId;
      /* this.Active=Active;
        this.AutoUserUnassign=AutoUserUnassign;*/
        this.CommentRequired=CommentRequired;
      /*  this.IsDeleted=IsDeleted;
        this.IsDefault=IsDefault;*/
        this.MainStatusId=MainStatusId;
       /* this.HoldTat=HoldTat;
        this.Movement=Movement;*/
        this.ParentStatus=ParentStatus;
        this.Name=Name;
       /* this.UserChangeable=UserChangeable;
        this.SystemDefine=SystemDefine;
        this.MobileAcceptStatus=MobileAcceptStatus;*/
    }
    public int getStatusId(){return StatusId;}
    /*public int getActive(){return Active;}*/
    /*public int getAutoUserUnassign() {
        return AutoUserUnassign;
    }*/
    public int getCommentRequired() {
        return CommentRequired;
    }
    /*public int getIsDeleted() {
        return IsDeleted;
    }
    public int getIsDefault() {
        return IsDefault;
    }*/
    public int getMainStatusId() {
        return MainStatusId;
    }
   /* public int getHoldTat() {
        return HoldTat;
    }
    public String getMovement() {
        return Movement;
    }*/
    public int getParentStatus() {
        return ParentStatus;
    }
    public String getName() {
        return Name;
    }/*
    public int getUserChangeable() {
        return UserChangeable;
    }
    public int getSystemDefine() {
        return SystemDefine;
    }
    public int getMobileAcceptStatus() {
        return MobileAcceptStatus;
    }*/

   /* @Override
    public String toString() {
        return "User{StatusId='"+StatusId+"', Active='"+Active+"', AutoUserUnassign='"+AutoUserUnassign+"', CommentRequired="+CommentRequired+"', IsDeleted="+IsDeleted+"', IsDefault="+IsDefault+"', MainStatusId="+MainStatusId+"', HoldTat="+HoldTat+"', Movement="+Movement+"', ParentStatus="+ParentStatus+"', StatusName="+Name+"', UserChangeable="+UserChangeable+"'};";
    }*/
}
