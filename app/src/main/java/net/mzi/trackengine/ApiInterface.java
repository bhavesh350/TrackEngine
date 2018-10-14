package net.mzi.trackengine;

/**
 * Created by Poonam on 12/14/2017.
 */

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;


public interface ApiInterface {
//@POST("/TrackEngine/api/post/Login")--local
/*  @GET("/api/unknown")
    Call<MultipleResource> doGetListResources();*/

    @POST("/api/post/Login")
//Live
        //@POST("/TrackEngine/api/post/Login")
    Call<ApiResult.User> isLogin(@Body ApiResult.User user);

    @POST("/api/post/GetIssuesForFireBase")
        //@POST("/TrackEngine/api/post/GetIssuesForFireBase")
    Call<ApiResult.IssueDetail> GetIssuesForFireBase(@Body ApiResult.IssueDetail issueDetail);

    @POST("/api/post/PostBatteryLevel")
        //@POST("/TrackEngine/api/post/PostBatteryLevel")
    Call<ApiResult.User_BatteryLevel> PostBatteryLevel(@Body ApiResult.User_BatteryLevel userBatteryLevel);

    @POST("/api/post/PostCoordinates")
        //@POST("/TrackEngine/api/post/PostCoordinates")
    Call<ApiResult.User_Location> PostCoordinates(@Body ApiResult.User_Location user_location);

    @POST("/api/Post/PostCoordinatesPlain")
        //@POST("/TrackEngine/api/post/PostCoordinates")
    Call<ApiResult.User_Location> PostCoordinatesShorten(@Body ApiResult.User_Location user_location);

    @POST("/api/post/PostTicketStatus")
        //@POST("/TrackEngine/api/post/PostTicketStatus")
    Call<ApiResult.IssueDetail> PostTicketStatus(@Body ApiResult.IssueDetail issueDetail);

    @POST("/api/post/PostGpsStatus")
        //@POST("/TrackEngine/api/post/PostGpsStatus")
    Call<ApiResult.User_GPS> PostGpsStatus(@Body ApiResult.User_GPS user_GPS);

    @POST("/api/post/PostMobileData")
        //@POST("/TrackEngine/api/post/PostMobileData")
    Call<ApiResult.User_MobileData> PostMobileData(@Body ApiResult.User_MobileData user_MobileData);

    @POST("/api/post/PostCheckIn")
        //@POST("/TrackEngine/api/post/PostCheckIn")
    Call<ApiResult.UserCheckInOut> PostCheckIn(@Body ApiResult.UserCheckInOut userCheckInOut);

    @GET("/api/post/GetConveyanceApprover?")
    Call<ApiResult.VoucherList> GetConveyanceApprover(@Query("iUserId") String iUserId, @Query("dtFromDate") String dtFromDate, @Query("dtToDate") String dtToDate, @Query("iApproverStatus") String iApproverStatus);

    @GET("/api/Post/PostTicketReceivingDate?")
    Call<ApiResult.CaptureTicket> captureTicket(@Query("iIssueId") String issueId, @Query("iUserId") String userId, @Query("dtReceivingDate") String date);

    @GET("/api/post/GetConveyanceVoucherHistory?")
    Call<ApiResult.VoucherList> GetConveyanceVoucherHistory(@Query("iConveyanceVoucherId") String iConveyanceVoucherId);

}