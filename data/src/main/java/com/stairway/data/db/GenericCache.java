package com.stairway.data.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.stairway.data.db.core.DatabaseManager;
import com.stairway.data.config.Logger;
import com.stairway.data.db.core.SQLiteContract;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vidhun on 06/07/16.
 */
public class GenericCache {
    private static final String TAG = "GenericCache";
    private static GenericCache instance;

    private Map<String, String> memoryStore;
    private DatabaseManager databaseManager;

    public GenericCache() {
        databaseManager = DatabaseManager.getInstance();
        memoryStore = new HashMap<>();

        Logger.v("SQLite GenericCache initialized");
    }

    public static GenericCache getInstance() {
        if (instance == null)
            instance = new GenericCache();

        return instance;
    }

    public boolean put(final String key, final String value) {

        Observable<Object> putObservable= Observable.create( (subscriber) -> {
                Logger.v("GeneticCache.put() on thread = " + Thread.currentThread().getName());

                SQLiteDatabase db = databaseManager.openConnection();
                db.beginTransactionNonExclusive();

                SQLiteStatement stmt = db.compileStatement(SQLiteContract.GenericCacheContract.SQL_DELETE);
                stmt.bindString(1, key);
                stmt.execute();
                stmt.execute();
                stmt.clearBindings();
                stmt.close();


                stmt = db.compileStatement(SQLiteContract.GenericCacheContract.SQL_INSERT);
                stmt.bindString(1,key);
                stmt.bindString(2, value);
                stmt.execute();
                stmt.clearBindings();
                stmt.close();

                db.setTransactionSuccessful();
                db.endTransaction();
                databaseManager.closeConnection();

                subscriber.onCompleted();
        });

        putObservable.subscribeOn(Schedulers.io());
        putObservable.observeOn(AndroidSchedulers.mainThread());

        putObservable.subscribe(
                new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        memoryStore.put(key,value);
                        Logger.d(">>> Insert: "+ key + " = "+value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(">>> ! Insert failed: "+ key + " = "+value+", error: "+e.getMessage());
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                }
        );

        if(memoryStore.get(key) == value)
            return true;
        return false;
    }

    public String get(String key) {
        String value = memoryStore.get(key);

        if(value == null) {
            try
            {
                SQLiteDatabase db = databaseManager.openConnection();

                String[] projection = {SQLiteContract.GenericCacheContract.COLUMN_VALUE};
                String selection = SQLiteContract.GenericCacheContract.COLUMN_KEY + " = ?";
                String[] selectionArgs = {key};

                Cursor cursor = db
                        .query(SQLiteContract.GenericCacheContract.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();

                try
                {
                    value = cursor.getString(0);
                }
                catch (Exception e)
                {
                    Logger.d("<<< ! Unable to find '" + key + "' in cache");
                }

                cursor.close();
                databaseManager.closeConnection();
            }
            catch (Exception e)
            {
                Logger.e("<<< ! Error while reading key = " + key + " from cache [" + e
                        .getMessage() + "]");
            }
        }
        Logger.v("<<< key ="+value);
        return value;
    }

    public void delete(final String key)
    {
        memoryStore.remove(key);
        Observable<Object> deleteObservable = Observable
                .create( (subscriber) -> {
                        synchronized (TAG)
                        {
                            Logger.v("GeneticCache.remove() on thread = " + Thread.currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteContract.GenericCacheContract.SQL_DELETE);
                            statement.bindString(1, key);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                });

        deleteObservable.subscribeOn(Schedulers.io());
        deleteObservable.observeOn(AndroidSchedulers.mainThread());

        deleteObservable.subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Logger.d("<<-  [Deleted] " + key);
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Logger.e("<<- ! Delete failed: key = " + key + " [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

}
