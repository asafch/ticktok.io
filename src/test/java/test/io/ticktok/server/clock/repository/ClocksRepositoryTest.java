package test.io.ticktok.server.clock.repository;

import io.ticktok.server.clock.Clock;
import io.ticktok.server.clock.repository.ClocksRepository;
import io.ticktok.server.schedule.repository.SchedulesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.io.ticktok.server.support.IntegrationTest;
import test.io.ticktok.server.support.RepositoryCleanupConfiguration;
import test.io.ticktok.server.support.RepositoryCleanupExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ClocksRepositoryTestConfiguration.class, RepositoryCleanupConfiguration.class})
@IntegrationTest
class ClocksRepositoryTest {

    @Autowired
    @RegisterExtension
    RepositoryCleanupExtension repositoryCleanupExtension;

    @Autowired
    ClocksRepository repository;
    @Autowired
    SchedulesRepository schedulesRepository;
    @Autowired
    java.time.Clock systemClock;

    @BeforeEach
    void setUp() {
        reset(schedulesRepository);
    }

    @Test
    void updateModifiedDate() {
        Clock savedClock = repository.saveClock("eli", "every.30.seconds");
        assertThat(repository.findById(savedClock.getId()).get().getLastModifiedDate(), is(systemClock.millis()));
    }

    @Test
    void updateClockByNameAndSchedule() {
        String clockName = "kuku";
        Clock clock = repository.saveClock(clockName, "every.5.seconds");
        assertThat(clock.getId(), is(repository.saveClock(clockName, "every.5.seconds").getId()));
        assertThat(clock.getId(), is(not(repository.saveClock(clockName, "every.2.seconds").getId())));
    }

    @Test
    void deleteClock() {
        Clock clock = repository.saveClock("popov", "every.6.seconds");
        repository.deleteClock(clock);
        assertFalse(repository.findById(clock.getId()).isPresent());
    }

    @Test
    void ignoreDeleteIfMoreUpdatedClockExists() {
        Clock clock = repository.saveClock("popov", "every.6.seconds");
        repository.save(Clock.builder()
                .id(clock.getId())
                .name(clock.getName())
                .schedule(clock.getSchedule())
                .lastModifiedDate(clock.getLastModifiedDate() + 10)
                .build());
        repository.deleteClock(clock);
        assertTrue(repository.findById(clock.getId()).isPresent(), "Clock should've not be deleted");
    }

    @Test
    void clockShouldCreatedAsPending() {
        Clock clock = repository.saveClock("lili", "every.11.seconds");
        assertThat(repository.findById(clock.getId()).get().getStatus(), is(Clock.PENDING));
    }

    @Test
    void updateClockStatus() {
        Clock clock = repository.saveClock("lulu", "every.11.seconds");
        repository.updateStatus(clock.getId(), Clock.ACTIVE);
        assertThat(repository.findById(clock.getId()).get().getStatus(), is(Clock.ACTIVE));
    }

    @Test
    void addScheduleUponClockSave() {
        Clock clock = repository.saveClock("lulu", "every.11.seconds");
        verify(schedulesRepository, times(1)).addClock(clock);
    }

    @Test
    void removeScheduleUponClockDelete() {
        Clock clock = repository.saveClock("lulu", "every.11.seconds");
        repository.deleteClock(clock);
        verify(schedulesRepository, times(1)).removeClock(clock);
    }

    @Test
    void shouldNotChangeStateWhenSavingAnexistingClock() {
        Clock clock = repository.saveClock("popov", "every.13.seconds");
        repository.updateStatus(clock.getId(), Clock.ACTIVE);
        repository.saveClock("popov", "every.13.seconds");
        assertThat(repository.findById(clock.getId()).get().getStatus(), is(Clock.ACTIVE));
    }

}