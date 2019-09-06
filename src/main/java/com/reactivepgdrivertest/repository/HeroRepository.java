package com.reactivepgdrivertest.repository;

import com.reactivepgdrivertest.entity.Hero;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface HeroRepository {
    Maybe<Hero> create(Hero hero);

    Maybe<Hero> findById(String heroId);

    Single<Hero> update(Hero hero);

    Completable delete(Hero hero);
}
