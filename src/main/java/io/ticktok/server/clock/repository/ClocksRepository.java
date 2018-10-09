package io.ticktok.server.clock.repository;

import io.ticktok.server.clock.Clock;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClocksRepository extends MongoRepository<Clock, String>, UpdateClocksRepository {

    List<Clock> findByLatestScheduledTickLessThanEqual(long time);

}