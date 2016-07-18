package com.stairway.spotlight.core.di.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by vidhun on 16/07/16.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface UserSessionScope {
}
