package fr.oqom.ouquonmange.services;

import java.util.List;

import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.CommunityDetails;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.models.GSMToken;
import fr.oqom.ouquonmange.models.Group;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.models.InterestPointDetails;
import fr.oqom.ouquonmange.models.JoinGroup;
import fr.oqom.ouquonmange.models.Login;
import fr.oqom.ouquonmange.models.Message;
import fr.oqom.ouquonmange.models.Profile;
import fr.oqom.ouquonmange.models.SignUpUser;
import fr.oqom.ouquonmange.models.Token;
import fr.oqom.ouquonmange.models.User;
import fr.oqom.ouquonmange.models.Vote;
import fr.oqom.ouquonmange.models.VoteGroup;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface OuQuOnMangeService {

    @POST("/auth/local/login")
    Observable<Token> login(@Body Login login);

    @POST("/auth/local/signup")
    Observable<Token> signUp(@Body SignUpUser signUpUser);

    @GET("/api/user")
    Observable<Profile> getProfile();

    @PUT("/api/user/addgcmtoken")
    Observable<Message> addGcmToken(@Body GSMToken gsmToken);

    @GET("/api/event/{communityUuid}/{day}")
    Observable<List<Event>> getEvents(@Path("communityUuid") String communityUuid, @Path("day") String day);

    @POST("/api/event/{communityUuid}")
    Observable<Event> createEvent(@Path("communityUuid") String communityUuid, @Body Event event);

    @GET("/api/community")
    Observable<List<Community>> getMyCommunities();

    @GET("/api/community/search")
    Observable<List<Community>> searchCommunities(@Query("query") String query);

    @GET("/api/community/search")
    Observable<List<Community>> searchCommunities();

    @GET("/api/community/{communityUuid}")
    Observable<CommunityDetails> getCommunityDetails(@Path("communityUuid") String communityUuid);

    @POST("/api/community")
    Observable<Community> createCommunity(@Body Community community);

    @PUT("/api/community/{communityUuid}/join")
    Observable<User> joinCommunity(@Path("communityUuid") String communityUuid);

    @DELETE("/api/community/{communityUuid}/quit")
    Observable<Message> quitCommunity(@Path("communityUuid") String communityUuid);

    @POST("/api/group/{communityUuid}")
    Observable<Group> joinGroup(@Path("communityUuid") String communityUuid, @Body JoinGroup joinGroup);

    @DELETE("/api/group/{communityUuid}/{eventUuid}/{interestPointApiId}")
    Observable<Message> quitGroup(
            @Path("communityUuid") String communityUuid,
            @Path("eventUuid") String eventUuid,
            @Path("interestPointApiId") String interestPointApiId
    );

    @POST("/api/vote/{communityUuid}")
    Observable<Vote> voteGroup(@Path("communityUuid") String communityUuid, @Body VoteGroup joinGroup);

    @DELETE("/api/vote/{communityUuid}/{eventUuid}/{interestPointApiId}")
    Observable<Message> unvoteGroup(
            @Path("communityUuid") String communityUuid,
            @Path("eventUuid") String eventUuid,
            @Path("interestPointApiId") String interestPointApiId
    );

    @GET("/api/interest/point/{communityUuid}/{eventUuid}/{interestPointApiId}/{interestPointType}")
    Observable<InterestPointDetails> getInterestPointDetails(
            @Path("communityUuid") String communityUuid,
            @Path("eventUuid") String eventUuid,
            @Path("interestPointApiId") String interestPointApiId,
            @Path("interestPointType") String interestPointType
    );

    @GET("/api/interest/point/{communityUuid}/{eventUuid}")
    Observable<List<InterestPoint>> _getInterestPoints(
            @Path("communityUuid") String communityUuid,
            @Path("eventUuid") String eventUuid,
            @Query("lat") String latitude,
            @Query("lng") String longitude,
            @Query("query") String query
    );
}
