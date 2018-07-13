package net.mzi.trackengine.model;

import java.util.Date;

/**
 * Created by Poonam on 3/7/2017.
 */

public class TicketInfoClass {
    public String IssueID;
    public String Address;
    public String ServiceItemNumber;
    public String Latitude;
    public String Longitude;
    public String AssetSerialNumber;
    public String AssetSubType;
    public String AssetType;
    public String CategoryName;
    public String CorporateName;
    public String CreatedDate;
    public String IssueText;
    public String PhoneNo;
    public String SLADate;
    public String StatusId;
    public String Subject;
    public String TicketHolder;
    public String UpdatedDate;
    public String TicketNumber;
    public String OEMNumber;
    public String AssetDetail;
    public String ContractSubTypeName;
    public String ContractName;
    public boolean IsVerified;
    public String PreviousStatus;
    public String LastTransportMode;
    public String alterNateNumber;
    public String email;


    public void TicketInfoClass(String IssueID, String Address, String Latitude, String Longitude, String ServiceItemNumber, String AssetSerialNumber, String AssetSubType, String AssetType, String CategoryName, String CorporateName, String CreatedDate, String IssueText, String PhoneNo, String SLADate, String StatusId, String Subject, String TicketHolder, String UpdatedDate, String TicketNumber, String OEMNumber, String AssetDetail, String ContractSubTypeName, String ContractName, boolean IsVerified, String PreviousStatus, String LastTransportMode) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.IssueID = IssueID;
        this.Address = Address;
        this.ServiceItemNumber = ServiceItemNumber;
        this.AssetSerialNumber = AssetSerialNumber;
        this.CategoryName = CategoryName;
        this.CorporateName = CorporateName;
        this.CreatedDate = CreatedDate;
        this.IssueText = IssueText;
        this.PhoneNo = PhoneNo;
        this.SLADate = SLADate;
        this.StatusId = StatusId;
        this.Subject = Subject;
        this.TicketHolder = TicketHolder;
        this.AssetSubType = AssetSubType;
        this.AssetType = AssetType;
        this.UpdatedDate = UpdatedDate;
        this.TicketNumber = TicketNumber;
        this.OEMNumber = OEMNumber;
        this.AssetDetail = AssetDetail;
        this.ContractName = ContractName;
        this.ContractSubTypeName = ContractSubTypeName;
        this.IsVerified = IsVerified;
        this.PreviousStatus = PreviousStatus;
        this.LastTransportMode = LastTransportMode;
    }

    public String getIssueID() {
        return IssueID;
    }

    public String getAdress() {
        return Address;
    }

    public String getAssetName() {
        return ServiceItemNumber;
    }

    public String getAssetSerialNumber() {
        return AssetSerialNumber;
    }

    public String getAssetSubType() {
        return AssetSubType;
    }

    public String getAssetType() {
        return AssetType;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public String getCorporateName() {
        return CorporateName;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public String getIssueText() {
        return IssueText;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public String getSLADate() {
        return SLADate;
    }

    public String getStatusId() {
        return StatusId;
    }

    public String getSubject() {
        return Subject;
    }

    public String getTicketHolder() {
        return TicketHolder;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    @Override
    public String toString() {
        return "User{Address='" + Address + "', AssetName='" + ServiceItemNumber + "', AssetSerialNumber=" + AssetSerialNumber + "', CategoryName=" + CategoryName + "', CorporateName=" + CorporateName + "', CreatedDate=" + CreatedDate + "', IssueText=" + IssueText + "', PhoneNo=" + PhoneNo + "', SLADate=" + SLADate + "', StatusId=" + StatusId + "', StatusId=" + StatusId + "'};";
    }
}
