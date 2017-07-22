//package com.chat.ichat.screens.people_nearby;
//
//import com.chat.ichat.api.location.NearbyPeopleResponse;
//import com.chat.ichat.api.location.UserLocation;
//import com.chat.ichat.core.BasePresenter;
//import com.chat.ichat.core.BaseView;
//
//import java.util.List;
//
///**
// * Created by vidhun on 20/05/17.
// */
//
//public class PeopleNearbyContract {
//    interface View extends BaseView {
//        void updatePeopleNearby(List<UserLocation> nearbyPeopleResponseList);
//        void updateLocationNotAvailable();
//        void onLocationCleared();
//    }
//
//    interface Presenter extends BasePresenter<PeopleNearbyContract.View> {
//        void getPeopleNearby(double latitude, double longitude);
//        void clearLocation();
//    }
//}
