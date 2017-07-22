//package com.chat.ichat.screens.people_nearby;
//
//import com.chat.ichat.api.ApiManager;
//import com.chat.ichat.api.StatusResponse;
//import com.chat.ichat.api.location.NearbyPeopleResponse;
//import com.chat.ichat.api.location.SendLocationRequest;
//
//import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.schedulers.Schedulers;
//import rx.subscriptions.CompositeSubscription;
//
///**
// * Created by vidhun on 21/05/17.
// */
//
//public class PeopleNearbyPresenter implements PeopleNearbyContract.Presenter {
//    public PeopleNearbyContract.View peopleNearbyView;
//    private CompositeSubscription compositeSubscription;
//
//    public PeopleNearbyPresenter() {
//        this.compositeSubscription = new CompositeSubscription();
//    }
//
//    @Override
//    public void attachView(PeopleNearbyContract.View view) {
//        this.peopleNearbyView = view;
//    }
//
//    @Override
//    public void detachView() {
//        peopleNearbyView = null;
//        compositeSubscription.unsubscribe();
//    }
//
//    @Override
//    public void getPeopleNearby(double latitude, double longitude) {
//        ApiManager.getLocationApi().getNearbyPeople(new SendLocationRequest(latitude, longitude))
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<NearbyPeopleResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {}
//
//                    @Override
//                    public void onNext(NearbyPeopleResponse nearbyPeopleResponse) {
//                        peopleNearbyView.updatePeopleNearby(nearbyPeopleResponse.getNearbyPeople());
//                    }
//                });
//    }
//
//    @Override
//    public void clearLocation() {
//        ApiManager.getLocationApi().delete()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<StatusResponse>() {
//                    @Override
//                    public void onCompleted() {}
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(StatusResponse statusResponse) {
//                        peopleNearbyView.onLocationCleared();
//                    }
//                });
//    }
//}
