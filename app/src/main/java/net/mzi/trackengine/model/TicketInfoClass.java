package net.mzi.trackengine.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Poonam on 3/7/2017.
 */

public class TicketInfoClass implements Serializable {
    private static final long serialVersionUID = 756228457L;
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
    public String ScheduleDate;
    public String type;
    public String journeyStatus;
    public String OtherDepartment;
    public boolean isCaptured = false;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJourneyStatus() {
        return journeyStatus;
    }

    public void setJourneyStatus(String journeyStatus) {
        this.journeyStatus = journeyStatus;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }

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

    public void setIssueID(String issueID) {
        IssueID = issueID;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getServiceItemNumber() {
        return ServiceItemNumber;
    }

    public void setServiceItemNumber(String serviceItemNumber) {
        ServiceItemNumber = serviceItemNumber;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public void setAssetSerialNumber(String assetSerialNumber) {
        AssetSerialNumber = assetSerialNumber;
    }

    public void setAssetSubType(String assetSubType) {
        AssetSubType = assetSubType;
    }

    public void setAssetType(String assetType) {
        AssetType = assetType;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public void setCorporateName(String corporateName) {
        CorporateName = corporateName;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public void setIssueText(String issueText) {
        IssueText = issueText;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public void setSLADate(String SLADate) {
        this.SLADate = SLADate;
    }

    public void setStatusId(String statusId) {
        StatusId = statusId;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public void setTicketHolder(String ticketHolder) {
        TicketHolder = ticketHolder;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }

    public String getTicketNumber() {
        return TicketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        TicketNumber = ticketNumber;
    }

    public String getOEMNumber() {
        return OEMNumber;
    }

    public void setOEMNumber(String OEMNumber) {
        this.OEMNumber = OEMNumber;
    }

    public String getAssetDetail() {
        return AssetDetail;
    }

    public void setAssetDetail(String assetDetail) {
        AssetDetail = assetDetail;
    }

    public String getContractSubTypeName() {
        return ContractSubTypeName;
    }

    public void setContractSubTypeName(String contractSubTypeName) {
        ContractSubTypeName = contractSubTypeName;
    }

    public String getContractName() {
        return ContractName;
    }

    public void setContractName(String contractName) {
        ContractName = contractName;
    }

    public boolean isVerified() {
        return IsVerified;
    }

    public void setVerified(boolean verified) {
        IsVerified = verified;
    }

    public String getPreviousStatus() {
        return PreviousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        PreviousStatus = previousStatus;
    }

    public String getLastTransportMode() {
        return LastTransportMode;
    }

    public void setLastTransportMode(String lastTransportMode) {
        LastTransportMode = lastTransportMode;
    }

    public String getAlterNateNumber() {
        return alterNateNumber;
    }

    public void setAlterNateNumber(String alterNateNumber) {
        this.alterNateNumber = alterNateNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getScheduleDate() {
        return ScheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        ScheduleDate = scheduleDate;
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
