package com.hackathon.repository;

import com.hackathon.entity.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    interface HackathonLocationCount {
        String getLocation();
        long getHackathonCount();
    }

    @Query("SELECT e.location AS location, COUNT(e) AS hackathonCount " +
           "FROM Event e " +
           "GROUP BY e.location " +
           "ORDER BY COUNT(e) DESC")
    List<HackathonLocationCount> countHackathonsByLocation();
}
