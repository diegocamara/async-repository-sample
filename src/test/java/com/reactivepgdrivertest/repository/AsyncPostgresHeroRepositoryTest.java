package com.reactivepgdrivertest.repository;

import com.reactivepgdrivertest.AbstractTest;
import com.reactivepgdrivertest.entity.Hero;
import com.reactivepgdrivertest.exception.OperationFailException;
import com.reactivepgdrivertest.repository.impl.AsyncPostgresHeroRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import io.reactiverse.pgclient.Tuple;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class AsyncPostgresHeroRepositoryTest extends AbstractTest {

    private HeroRepository heroRepository;

    @BeforeEach
    public void beforeEach() {
        this.heroRepository = new AsyncPostgresHeroRepository(pgClient);
    }

    @Test
    public void givenAValidHero_whenExecuteCreate_thenInsertHero(VertxTestContext vertxTestContext) {

        Hero hero = new Hero();
        hero.setName("Hero 1");
        hero.setIndividuality("Magnetic arms");

        this.heroRepository.create(hero).subscribe(createdHero -> {
            Assertions.assertNotNull(createdHero.getId());
            Assertions.assertNotNull(createdHero.getName());
            Assertions.assertNotNull(createdHero.getIndividuality());
            vertxTestContext.completeNow();
        });

    }

    @Test
    public void givenAnPersistedHero_whenExecuteFindById_shouldReturnOnePersistedHero(VertxTestContext vertxTestContext) {

        String heroId = UUID.randomUUID().toString();
        String heroName = "Hero 1";
        String heroIndividuality = "Eletric wave";

        pgClient.preparedQuery("INSERT INTO HEROES (ID, NAME, INDIVIDUALITY) VALUES ($1, $2, $3)",
                Tuple.of(heroId, heroName, heroIndividuality),
                pgRowSetAsyncResult -> {
                    if (pgRowSetAsyncResult.succeeded()) {
                        this.heroRepository.findById(heroId).subscribe(result -> {
                            Assertions.assertEquals(heroId, result.getId());
                            Assertions.assertEquals(heroName, result.getName());
                            Assertions.assertEquals(heroIndividuality, result.getIndividuality());
                            vertxTestContext.completeNow();
                        });
                    }
                });

    }

    @Test
    public void givenAnInvalidHeroId_whenExecuteFindById_shouldCompleteOperation(VertxTestContext vertxTestContext) {

        String heroId = UUID.randomUUID().toString();

        this.heroRepository.findById(heroId).subscribe(
                ar -> {
                }, ar -> {
                }, vertxTestContext::completeNow);

    }

    @Test
    public void givenAnPersistedHero_whenExecuteUpdate_shouldReturnAnHeroWithUpdatedFields(VertxTestContext vertxTestContext) {

        Hero hero = new Hero();
        hero.setId(UUID.randomUUID().toString());
        hero.setName("Hero 1");
        hero.setIndividuality("Fire hands");

        pgClient.preparedQuery("INSERT INTO HEROES (ID, NAME, INDIVIDUALITY) VALUES ($1, $2, $3)",
                Tuple.of(hero.getId(), hero.getName(), hero.getIndividuality()),
                pgRowSetAsyncResult -> {
                    if (pgRowSetAsyncResult.succeeded()) {
                        hero.setName("Hero 2");
                        this.heroRepository.update(hero).subscribe(result -> {
                            Assertions.assertEquals(hero.getName(), result.getName());
                            vertxTestContext.completeNow();
                        });
                    }
                });
    }

    @Test
    public void givenAnInvalidHero_whenExecuteUpdate_shouldReturnAnOperationFailException(VertxTestContext vertxTestContext) {

        Hero hero = new Hero();
        hero.setId(UUID.randomUUID().toString());
        hero.setName("Hero 1");
        hero.setIndividuality("Speaker fingers");

        this.heroRepository.update(hero).subscribe(ar -> {
        }, throwable -> {
            OperationFailException operationFailException = (OperationFailException) throwable;
            Assertions.assertEquals("UPDATE", operationFailException.getType());
            Assertions.assertEquals("Update fail for id: " + hero.getId(), operationFailException.getMessage());
            vertxTestContext.completeNow();
        });

    }

    @Test
    public void givenAnPersistedHero_whenExecuteDelete_shouldCompleteOperation(VertxTestContext vertxTestContext) {

        Hero hero = new Hero();
        hero.setId(UUID.randomUUID().toString());
        hero.setName("Hero 1");
        hero.setIndividuality("Fire hands");

        pgClient.preparedQuery("INSERT INTO HEROES (ID, NAME, INDIVIDUALITY) VALUES ($1, $2, $3)",
                Tuple.of(hero.getId(), hero.getName(), hero.getIndividuality()),
                pgRowSetAsyncResult -> {
                    if (pgRowSetAsyncResult.succeeded()) {

                        this.heroRepository.delete(hero).subscribe(() -> {
                            vertxTestContext.completeNow();
                        });

                    }
                });

    }

    @Test
    public void givenAnInvalidHero_whenExecuteDelete_shouldReturnAnOperationFailException(VertxTestContext vertxTestContext) {

        Hero hero = new Hero();
        hero.setId(UUID.randomUUID().toString());
        hero.setName("Hero 1");
        hero.setIndividuality("Thunder heart");

        this.heroRepository.delete(hero).subscribe(() -> {
        }, throwable -> {
            OperationFailException operationFailException = (OperationFailException) throwable;
            Assertions.assertEquals("DELETE", operationFailException.getType());
            Assertions.assertEquals("Delete fail for id: " + hero.getId(), operationFailException.getMessage());
            vertxTestContext.completeNow();
        });

    }

}
