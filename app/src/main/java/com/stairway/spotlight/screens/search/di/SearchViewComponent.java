package com.stairway.spotlight.screens.search.di;

import com.stairway.spotlight.core.di.scope.ViewScope;
import com.stairway.spotlight.screens.search.SearchActivity;

import dagger.Subcomponent;

/**
 * Created by vidhun on 17/12/16.
 */
@ViewScope
@Subcomponent(modules = SearchViewModule.class)
public interface SearchViewComponent {
    void inject(SearchActivity searchActivity);
}
