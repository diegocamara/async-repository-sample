package com.reactivepgdrivertest.repository.impl;

import com.reactivepgdrivertest.entity.Hero;
import com.reactivepgdrivertest.exception.OperationFailException;
import com.reactivepgdrivertest.repository.HeroRepository;
import com.sun.istack.internal.NotNull;

import java.util.UUID;

import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class AsyncPostgreHeroRepository implements HeroRepository {

    private PgPool pgClient;

    public AsyncPostgreHeroRepository(PgPool pgClient) {
        this.pgClient = pgClient;
    }

    @Override
    public Maybe<Hero> create(Hero hero) {
        hero.setId(UUID.randomUUID().toString());
        return Maybe.create(emitter -> {
            pgClient.preparedQuery("INSERT INTO HEROES (ID, NAME, INDIVIDUALITY) VALUES($1, $2, $3)",
                    Tuple.of(hero.getId(), hero.getName(), hero.getIndividuality()),
                    asyncResult -> {
                        if (asyncResult.succeeded()) {
                            emitter.onSuccess(hero);
                        } else {
                            emitter.onError(asyncResult.cause());
                        }
                    });
        });
    }

    @Override
    public Maybe<Hero> findById(String heroId) {
        return Maybe.create(emitter -> {
            pgClient.preparedQuery("SELECT * FROM HEROES WHERE ID = $1", Tuple.of(heroId), asyncResult -> {
                if (asyncResult.succeeded()) {
                    PgRowSet rows = asyncResult.result();
                    if (rows.rowCount() > 0) {
                        Row row = rows.iterator().next();
                        emitter.onSuccess(fromRow(row));
                    } else {
                        emitter.onComplete();
                    }
                } else {
                    emitter.onError(asyncResult.cause());
                }
            });
        });
    }

    @Override
    public Single<Hero> update(Hero hero) {
        return Single.create(emitter -> {

            pgClient.preparedQuery("UPDATE HEROES SET NAME = $1, INDIVIDUALITY = $2 WHERE ID = $3",
                    Tuple.of(hero.getName(), hero.getIndividuality(), hero.getId()),
                    asyncResult -> {
                        if (asyncResult.succeeded()) {
                            PgRowSet pgRowSet = asyncResult.result();
                            if (pgRowSet.rowCount() > 0) {
                                emitter.onSuccess(hero);
                            } else {
                                emitter.onError(new OperationFailException("UPDATE", "Update fail for id: " + hero.getId()));
                            }
                        } else {
                            emitter.onError(asyncResult.cause());
                        }
                    });
        });
    }

    @Override
    public Completable delete(Hero hero) {
        return Completable.create(emitter -> {
            pgClient.preparedQuery("DELETE FROM HEROES WHERE ID = $1", Tuple.of(hero.getId()), asyncResult -> {
                if (asyncResult.succeeded()) {
                    PgRowSet pgRowSet = asyncResult.result();
                    if (pgRowSet.rowCount() > 0) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(new OperationFailException("DELETE", "Delete fail for id: " + hero.getId()));
                    }
                } else {
                    emitter.onError(asyncResult.cause());
                }
            });
        });
    }

    private Hero fromRow(Row row) {
        Hero hero = new Hero();
        hero.setId(row.getString("id"));
        hero.setName(row.getString("name"));
        hero.setIndividuality(row.getString("individuality"));
        return hero;
    }

}
