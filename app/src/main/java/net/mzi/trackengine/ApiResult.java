package net.mzi.trackengine;

import com.google.gson.annotations.SerializedName;

import net.mzi.trackengine.model.TicketInfoClass;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Poonam on 12/14/2017.
 */
public class ApiResult {

    public class Result {
        @SerializedName("Id")
        public String Id;

        @SerializedName("Status")
        public String Status;

        @SerializedName("Message")
        public String Message;

        @SerializedName("TicketRedirection")
        public boolean TicketRedirection;
    }

    public class CaptureTicket {
        @SerializedName("Result")
        public ApiResult.Result resData;

    }

    public class ComplainCategory {
        @SerializedName("Id")
        public String Id;

        @SerializedName("CategoryName")
        public String CategoryName;

        @SerializedName("Active")
        public String Active;

    }

    public class ContractManpower {

        @SerializedName("Id")
        public String Id;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("UserName")
        public String UserName;

        @SerializedName("FieldUser")
        public String FieldUser;

        @SerializedName("FromDate")
        public String FromDate;

        @SerializedName("EndDate")
        public String EndDate;

        @SerializedName("FromTime")
        public String FromTime;

        @SerializedName("ToTime")
        public String ToTime;

        @SerializedName("IsField")
        public String IsField;

        @SerializedName("Duration")
        public String Duration;

        @SerializedName("Period")
        public String Period;


    }

    public class Complain {

        @SerializedName("Id")
        public String Id;

        @SerializedName("CategoryName")
        public String CategoryName;

        @SerializedName("Subject")
        public String Subject;

        @SerializedName("ComplainText")
        public String ComplainText;

        @SerializedName("CreatedOn")
        public String CreatedOn;

        @SerializedName("CreatedBy")
        public String CreatedBy;

        @SerializedName("Company")
        public String Company;

        @SerializedName("Location")
        public String Location;

        @SerializedName("LastUpdateOn")
        public String LastUpdateOn;

        @SerializedName("LastComment")
        public String LastComment;

        @SerializedName("StatusName")
        public String StatusName;

        @SerializedName("StatusId")
        public String StatusId;

        @SerializedName("LastActionBy")
        public String LastActionBy;

        @SerializedName("LastActionByType")
        public String LastActionByType;

        @SerializedName("CreationSource")
        public String CreationSource;

        @SerializedName("FilterType")
        public String FilterType;

        @SerializedName("FilterCategory")
        public String FilterCategory;

        @SerializedName("Listing")
        public String Listing;

        @SerializedName("HasError")
        public String HasError;

        @SerializedName("Error")
        public String Error;


    }

    public class Customer {

        @SerializedName("CustomerName")
        public String CustomerName;

        @SerializedName("Id")
        public String Id;

        @SerializedName("Mobile1")
        public String Mobile1;

        @SerializedName("Email")
        public String Email;

        @SerializedName("EnterpriseId")
        public String EnterpriseId;

        @SerializedName("Duration")
        public String Duration;

        @SerializedName("ParentEnterpriseId")
        public String ParentEnterpriseId;

        @SerializedName("DepartmentId")
        public String DepartmentId;

        @SerializedName("DepartmentName")
        public String DepartmentName;

        @SerializedName("AssetCount")
        public String AssetCount;
    }

    public class Enterprise {
        @SerializedName("Id")
        public String Id;

        @SerializedName("EnterpriseName")
        public String EnterpriseName;

    }

    public class IssueDetail {

        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("StatusId")
        public String Status;

        @SerializedName("IssueCount")
        public String IssueCount;

        @SerializedName("TicketNumber")
        public String TicketNumber;

        @SerializedName("HistoryId")
        public String HistoryId;

        @SerializedName("TicketId")
        public String IssueId;

        @SerializedName("TaskId")
        public String taskId;

        @SerializedName("IssueIds")
        public String IssueIds;

        @SerializedName("TaskIds")
        public String TaskIds;

        @SerializedName("ActionedBy")
        public String ActionedBy;

        @SerializedName("IssueStatus")
        public String IssueStatus;

        @SerializedName("Comment")
        public String Comment;

        @SerializedName("UserAssign")
        public String UserAssign;

        @SerializedName("AssignedTo")
        public String AssignedTo;

        @SerializedName("CreatedDate")
        public String CreatedDate;

        @SerializedName("LoggedInUserDetail")
        public lstDetails data;

        @SerializedName("ParentCompanyId")
        public String ParentCompanyId;

        @SerializedName("ActivityDate")
        public String ActivityDate;

        @SerializedName("DepartmentId")
        public String DepartmentId;

        @SerializedName("Latitude")
        public String Latitude;

        @SerializedName("Longitude")
        public String Longitude;

        @SerializedName("AssetSerialNo")
        public String AssetSerialNo;

        @SerializedName("RealtimeUpdate")
        public String RealtimeUpdate;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("LastTransportMode")
        public String LastTransportMode;

        @SerializedName("ModeOfTransport")
        public String ModeOfTransport;

        @SerializedName("Expense")
        public String Expense;

        @SerializedName("AssignedUserId")
        public String AssignedUserId;

        @SerializedName("LastAction")
        public String LastAction;

        @SerializedName("IsAssetVerificationEnable")
        public String IsAssetVerificationEnable;

        @SerializedName("ScheduleDate")
        public String scheduleDate;

        @SerializedName("StartingForSite")
        public String StartingForSite;

        @SerializedName("CustomDestination")
        public String CustomDestination;

        @SerializedName("lstDetails")
        public lstDetails IssueDetail[];

        public IssueDetail(String Userid, String ParentCompanyId, String IsuueId, String Status, String Comment, String ActivityDate, String DepartmentId, String Latitude, String Longitude, String AssetSerialNo, String DeviceId, String RealtimeUpdate, String LastTransportMode, String Expense, String AssignedUserId, String customeDestination, String startingForSite) {
            this.UserId = Userid;
            this.ParentCompanyId = ParentCompanyId;
            this.IssueId = IsuueId;
            Map<String,TicketInfoClass> issueDetailsMap = MyApp.getApplication().readIssueDetailsHistory();
            if (issueDetailsMap.containsKey(IssueId)) {
                if(issueDetailsMap.get(IssueId).getType().equals("Ticket")){
                    this.taskId = "0";
                }else {
                    this.taskId = IssueId;
                    this.IssueId = "0";
                }
            } else {
                this.taskId = "0";
            }

            this.Status = Status;
            this.Comment = Comment;
            this.ActivityDate = ActivityDate;
            this.DepartmentId = DepartmentId;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
            this.AssetSerialNo = AssetSerialNo;
            this.DeviceId = DeviceId;
            this.RealtimeUpdate = RealtimeUpdate;
            this.AssignedUserId = AssignedUserId;
            this.LastAction = LastAction;
            this.Expense = Expense;
            this.LastTransportMode = LastTransportMode;
            this.ModeOfTransport = LastTransportMode;
            this.CustomDestination = customeDestination;
            this.StartingForSite = startingForSite;
        }

        public IssueDetail(String IssueIds, String taskIds, String UserId, String IsAssetVerificationEnable, String DepartmentId, String LastAction) {
            this.IssueIds = IssueIds;
            this.TaskIds = taskIds;
            this.UserId = UserId;
            this.IsAssetVerificationEnable = IsAssetVerificationEnable;
            this.DepartmentId = DepartmentId;
            this.ActivityDate = ActivityDate;
            this.LastAction = LastAction;
        }


        public class lstDetails {

            @SerializedName("EnterpriseAddress")
            public String EnterpriseAddress;

            @SerializedName("CreatedOn")
            public String CreatedOn;

            @SerializedName("UpdatedOn")
            public String UpdatedOn;

            @SerializedName("MobileNumber")
            public String MobileNumber;

            @SerializedName("SLABreachDate")
            public String SLABreachDate;

            @SerializedName("StatusId")
            public String StatusId;

            @SerializedName("StatusName")
            public String StatusName;

            @SerializedName("TicketAssignedTo")
            public String TicketAssignedTo;

            @SerializedName("AssetSubType")
            public String AssetSubType;

            @SerializedName("AssetType")
            public String AssetType;

            @SerializedName("AssetSerialNo")
            public String AssetSerialNo;

            @SerializedName("CategoryName")
            public String CategoryName;

            @SerializedName("CorporateName")
            public String CorporateName;

            @SerializedName("IssueText")
            public String IssueText;

            @SerializedName("Latitude")
            public String Latitude;

            @SerializedName("Longitude")
            public String Longitude;

            @SerializedName("ServiceItemNo")
            public String ServiceItemNo;

            @SerializedName("Subject")
            public String Subject;

            @SerializedName("TicketHolder")
            public String TicketHolder;

            @SerializedName("TicketNumber")
            public String TicketNumber;

            @SerializedName("OEMTicketId")
            public String OEMTicketId;

            @SerializedName("AssetDetail")
            public String AssetDetail;

            @SerializedName("ContractSubTypeName")
            public String ContractSubTypeName;

            @SerializedName("ContractName")
            public String ContractName;

            @SerializedName("IsAssetVerified")
            public boolean IsAssetVerified;

            @SerializedName("ImplementCSAT")
            public boolean ImplementCSAT;

            @SerializedName("PreviousStatusId")
            public String PreviousStatusId;

            @SerializedName("PreviousStatusName")
            public String PreviousStatusName;

            @SerializedName("Id")
            public String Id;

            @SerializedName("Status")
            public boolean Status;

            @SerializedName("Message")
            public String Message;

            @SerializedName("ScheduleDate")
            public String scheduleDate;

            @SerializedName("LastTransportMode")
            public String LastTransportMode;

            @SerializedName("Type")
            public String type;

            @SerializedName("JourneyStatus")
            public String journeyStatus;

            @SerializedName("OtherDepartment")
            public String otherDepartment;

        }
    }

    public class IssueStatus {

        @SerializedName("lstIssue_Status")
        public lstDetails data;


        public class lstDetails implements Serializable {

            private static final long serialVersionUID = 7562287L;

            @SerializedName("Id")
            public String Id;

            @SerializedName("StatusName")
            public String StatusName;

            @SerializedName("CommentRequired")
            public String CommentRequired;

            @SerializedName("ParentStatuses")
            public String ParentStatuses;

            @SerializedName("MainStatusId")
            public String MainStatusId;

            @SerializedName("StartingForSite")
            public String StartingForSite;

            @SerializedName("IsMobileStatus")
            public String IsMobileStatus;

        }

    /*public IssueDetail(String sUsername, String sPassword, String sDeviceId, String sDeviceModel) {
        this.Username=sUsername;
        this.Password=sPassword;
        this.DeviceId=sDeviceId;
        this.DeviceModel=sDeviceModel;

    }*/


    }

    public class Location {
        @SerializedName("Id")
        public String Id;

        @SerializedName("AreaName")
        public String AreaName;

        @SerializedName("Name")
        public String Name;

    }

    public class User implements Serializable {

        private static final long serialVersionUID = 756228897L;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("LoginId")
        public String LoginId;

        @SerializedName("OldPassword")
        public String OldPassword;

        @SerializedName("NewPassword")
        public String NewPassword;

        @SerializedName("Username")
        public String Username;

        @SerializedName("ActivityDate")
        public String ActivityDate;

        @SerializedName("password")
        public String Password;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("DeviceModel")
        public String DeviceModel;

        @SerializedName("AppVersionCode")
        public String AppVersionCode;

        @SerializedName("AppVersionName")
        public String AppVersionName;

        @SerializedName("AndroidVersion")
        public String AndroidVersion;

        @SerializedName("StatusByHierarchy")
        public boolean StatusByHierarchy;


        @SerializedName("LoggedInUserDetail")
        public LoginDetail.Datum data;

        public User(String sUsername, String sPassword, String sDeviceId, String sDeviceModel, String ActivityDate, String AndroidVersion, String AppVersionCode, String AppVersionName) {
            this.Username = sUsername;
            this.Password = sPassword;
            this.DeviceId = sDeviceId;
            this.DeviceModel = sDeviceModel;
            this.ActivityDate = ActivityDate;
            this.AndroidVersion = AndroidVersion;
            this.AppVersionCode = AppVersionCode;
            this.AppVersionName = AppVersionName;
        }
    }

    public class LoginDetail {

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("LoginId")
        public String LoginId;

        @SerializedName("OldPassword")
        public String OldPassword;

        @SerializedName("NewPassword")
        public String NewPassword;

        @SerializedName("Username")
        public String Username;

        @SerializedName("password")
        public String Password;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("DeviceModel")
        public String DeviceModel;

        @SerializedName("Id")
        public String Id;

        @SerializedName("Status")
        public String Status;

        @SerializedName("Message")
        public String Message;

        @SerializedName("LoggedInUserDetail")
        public Datum data;

        public class Datum implements Serializable {
            private static final long serialVersionUID = 756223487L;
            @SerializedName("UserName")
            public String Username;

            @SerializedName("UserId")
            public String UserId;

            @SerializedName("DepartmentId")
            public String DepartmentId;

            @SerializedName("RoleId")
            public String RoleId;

            @SerializedName("IsCoordinator")
            public String IsCoordinator;

            @SerializedName("IsFieldAgent")
            public String IsFieldAgent;

            @SerializedName("UserType")
            public String UserType;

            @SerializedName("CompanyId")
            public String CompanyId;

            @SerializedName("ParentCompanyId")
            public String ParentCompanyId;

            @SerializedName("CheckedInTime")
            public String CheckedInTime;

            @SerializedName("CheckedInStatus")
            public String CheckedInStatus;

            @SerializedName("IsDefaultDepartment")
            public String IsDefaultDepartment;

            @SerializedName("AppLocationSendingFrequency")
            public int AppLocationSendingFrequency;

            @SerializedName("AppBatterySendingFrequency")
            public int AppBatterySendingFrequency;

            @SerializedName("CSATEnable")
            public boolean CSATEnable;

            @SerializedName("AssetVerification")
            public boolean AssetVerification;

            @SerializedName("Id")
            public String Id;

            @SerializedName("Status")
            public String Status;

            @SerializedName("Message")
            public String Message;

            @SerializedName("IssueStatusList")
            public IssueStatus.lstDetails dataStatus[];

            @SerializedName("IssueAssetPartList")
            public IssueAssetPartList issueAssetPartList[];

            @SerializedName("ModeOfTrasportList")
            public ModeOfTrasportList modeOfTrasportList[];

        }

        public LoginDetail(String sUsername, String sPassword, String sDeviceId, String sDeviceModel) {
            this.Username = sUsername;
            this.Password = sPassword;
            this.DeviceId = sDeviceId;
            this.DeviceModel = sDeviceModel;

        }


    }

    public class VoucherList {
        @SerializedName("lstConveyanceVoucherApprover")
        public Voucher Vouchers[];

        @SerializedName("listConveyanceVoucherHistory")
        public VoucherHistory VouchersHistory[];
    }


    public class VoucherHistory {
        @SerializedName("CreatedDate")
        public String CreatedDate;

        @SerializedName("Action")
        public String Action;

        @SerializedName("DoneBy")
        public String DoneBy;

        @SerializedName("Remark")
        public String Remark;
    }

    public class Voucher {
        @SerializedName("VoucherId")
        public String VoucherId;

        @SerializedName("Period")
        public String Period;

        @SerializedName("FromDate")
        public String FromDate;

        @SerializedName("ToDate")
        public String ToDate;

        @SerializedName("DistanceTravel")
        public String DistanceTravel;

        @SerializedName("ConveyanceAmount")
        public String ConveyanceAmount;

        @SerializedName("VoucherNumber")
        public String VoucherNumber;

        @SerializedName("SeniorApproved")
        public String SeniorApproved;

        @SerializedName("AccountApproved")
        public String AccountApproved;

        @SerializedName("Id")
        public String Id;
    }


    public class ModeOfTrasportList implements Serializable {

        private static final long serialVersionUID = 75622348967L;

        @SerializedName("Id")
        public String Id;

        @SerializedName("TransportMode")
        public String TransportMode;

        @SerializedName("IsPublic")
        public String IsPublic;
    }

    public class SubCategoryName {

        @SerializedName("Id")
        public String Id;

        @SerializedName("CategoryName")
        public String CategoryName;

    }

    public class User_Location {
        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("Id")
        public String Id;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("AutoCaptured")
        public String AutoCaptured;

        @SerializedName("Address")
        public String Address;

        @SerializedName("ActionDate")
        public String ActionDate;

        @SerializedName("ActionTime")
        public String ActionTime;
        /*
                @SerializedName("UserId")
                public String UserId;

                @SerializedName("DeviceId")
                public String DeviceId;*/
        @SerializedName("Latitude")
        public String Latitude;

        @SerializedName("Longitude")
        public String Longitude;

        /*     @SerializedName("AutoCaptured")
             public String AutoCaptured;
     */
        @SerializedName("ActivityDate")
        public String ActivityDate;

        @SerializedName("AddressLine")
        public String AddressLine;

        @SerializedName("Premises")
        public String Premises;

        @SerializedName("SubLocality")
        public String SubLocality;

        @SerializedName("PostalCode")
        public String PostalCode;

        @SerializedName("City")
        public String City;

        @SerializedName("State")
        public String State;

        @SerializedName("Country")
        public String Country;

        @SerializedName("KnownName")
        public String KnownName;

        @SerializedName("Provider")
        public String Provider;

        @SerializedName("RealTimeUpdate")
        public String RealTimeUpdate;

        @SerializedName("SubAdminArea")
        public String SubAdminArea;


        public Double latitude;
        public Double longitude;

        public User_Location(String RealTimeUpdate, String UserId, String DeviceId, String Latitude, String Longitude, String ActivityDate, String AutoCaptured, String AddressLine, String SubLocality, String PostalCode, String City, String State, String Country, String KnownName, String Provider) {
            this.RealTimeUpdate = RealTimeUpdate;
            this.UserId = UserId;
            this.DeviceId = DeviceId;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
            this.ActivityDate = ActivityDate;
            this.AutoCaptured = AutoCaptured;
            this.AddressLine = AddressLine;
            this.SubLocality = SubLocality;
            this.PostalCode = PostalCode;
            this.City = City;
            this.State = State;
            this.Country = Country;
            this.KnownName = KnownName;
            this.Provider = Provider;
        }

        public User_Location(String RealTimeUpdate, String UserId, String DeviceId, String Latitude, String Longitude, String ActivityDate, String AutoCaptured) {
            this.RealTimeUpdate = RealTimeUpdate;
            this.UserId = UserId;
            this.DeviceId = DeviceId;
            this.Latitude = Latitude;
            this.Longitude = Longitude;
            this.ActivityDate = ActivityDate;
            this.AutoCaptured = AutoCaptured;

        }
    }


    public class IssueAssetPartList implements Serializable {
        private static final long serialVersionUID = 756223487147L;
        @SerializedName("Id")
        public String Id;

        @SerializedName("CompanyId")
        public String CompanyId;

        @SerializedName("ParentId")
        public String ParentId;

        @SerializedName("PartName")
        public String PartName;

        @SerializedName("AssetType")
        public String AssetType;

        @SerializedName("AssetSubType")
        public String AssetSubType;

    }


    public class UserCheckInOut {

        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("IsCheckedIn")
        public String IsCheckedIn;

        @SerializedName("ActivityDate")
        public String ActivityDate;

        @SerializedName("RealTimeUpdate")
        public String RealTimeUpdate;

        @SerializedName("FieldStaffName")
        public String FieldStaffName;

        @SerializedName("CheckInTime")
        public String CheckInTime;

        @SerializedName("CheckInLocation")
        public String CheckInLocation;

        @SerializedName("CheckOutTime")
        public String CheckOutTime;

        @SerializedName("CheckOutLocation")
        public String CheckOutLocation;

        @SerializedName("Duration")
        public String Duration;

        public UserCheckInOut(String RealTimeUpdate, String UserId, String DeviceId, String IsCheckedIn, String ActivityDate) {
            this.RealTimeUpdate = RealTimeUpdate;
            this.UserId = UserId;
            this.DeviceId = DeviceId;
            this.IsCheckedIn = IsCheckedIn;
            this.ActivityDate = ActivityDate;

        }
    }

    public class User_BatteryLevel {
        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("Battery")
        public String Battery;

        @SerializedName("AutoCaptured")
        public String AutoCaptured;

        @SerializedName("ActivityDate")

        public String ActivityDate;

        @SerializedName("RealTimeUpdate")
        public String RealTimeUpdate;

        public User_BatteryLevel(String RealTimeUpdate, String UserId, String DeviceId, String Battery, String ActivityDate, String AutoCaptured) {
            this.RealTimeUpdate = RealTimeUpdate;
            this.ActivityDate = ActivityDate;
            this.AutoCaptured = AutoCaptured;
            this.Battery = Battery;
            this.DeviceId = DeviceId;
            this.UserId = UserId;
        }
    }

    public class User_MobileData {

        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("Enabled")
        public String Enabled;

        @SerializedName("ActionDate")
        public String ActionDate;

        @SerializedName("RealTimeUpdate")
        public String RealTimeUpdate;

        public User_MobileData(String RealTimeUpdate, String UserId, String DeviceId, String Enabled, String ActionDate) {
            this.UserId = UserId;
            this.RealTimeUpdate = RealTimeUpdate;
            this.DeviceId = DeviceId;
            this.Enabled = Enabled;
            this.ActionDate = ActionDate;
        }
    }

    public class User_GPS {

        @SerializedName("Result")
        public ApiResult.Result resData;

        @SerializedName("UserId")
        public String UserId;

        @SerializedName("DeviceId")
        public String DeviceId;

        @SerializedName("Enabled")
        public String Enabled;

        @SerializedName("ActionDate")
        public String ActionDate;

        @SerializedName("RealTimeUpdate")
        public String RealTimeUpdate;


        public User_GPS(String RealTimeUpdate, String UserId, String DeviceId, String Enabled, String ActionDate) {
            this.UserId = UserId;
            this.RealTimeUpdate = RealTimeUpdate;
            this.DeviceId = DeviceId;
            this.Enabled = Enabled;
            this.ActionDate = ActionDate;
        }
    }


    public class Department {

        @SerializedName("Id")
        public String Id;

        @SerializedName("DepartmentName")
        public String DepartmentName;

        @SerializedName("EnterpriseId")
        public String EnterpriseId;

        @SerializedName("EnterpriseName")
        public String EnterpriseName;

        @SerializedName("ParentEnterpriseId")
        public String ParentEnterpriseId;
    }
}
