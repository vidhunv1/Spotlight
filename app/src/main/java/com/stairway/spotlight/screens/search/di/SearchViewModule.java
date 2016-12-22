package com.stairway.spotlight.screens.search.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.search.SearchContactsUseCase;
import com.stairway.spotlight.screens.search.SearchPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by vidhun on 17/12/16.
 */
@Module
public class SearchViewModule {
    @Provides
    @ViewScope
    public SearchPresenter providesSearchPresenter(SearchContactsUseCase searchContactsUseCase) {
        if(searchContactsUseCase == null)
            throw new IllegalStateException("UseCase is null");
        return new SearchPresenter(searchContactsUseCase);
    }
}
