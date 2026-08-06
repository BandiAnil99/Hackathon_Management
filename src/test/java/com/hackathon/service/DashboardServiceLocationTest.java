package com.hackathon.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hackathon.dto.HackathonsByLocationResponse;
import com.hackathon.entity.Event;
import com.hackathon.repository.EmailLogRepository;
import com.hackathon.repository.EventRepository;
import com.hackathon.repository.FeedbackRepository;
import com.hackathon.repository.PanelistRepository;
import com.hackathon.repository.ParticipantRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceLocationTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private PanelistRepository panelistRepository;

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void hackathonsByLocationReturnsCountsOrdered() {
        when(eventRepository.countHackathonsByLocation()).thenReturn(List.of(
                new EventRepository.HackathonLocationCount() {
                    public String getLocation() { return "Hyderabad, Telangana"; }
                    public long getHackathonCount() { return 12; }
                },
                new EventRepository.HackathonLocationCount() {
                    public String getLocation() { return "Chennai, Tamil Nadu"; }
                    public long getHackathonCount() { return 8; }
                }
        ));

        List<HackathonsByLocationResponse> result = dashboardService.hackathonsByLocation();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).location()).isEqualTo("Hyderabad, Telangana");
        assertThat(result.get(0).hackathonCount()).isEqualTo(12);
    }

    @Test
    void locationsReturnsAllowedList() {
        assertThat(dashboardService.locations()).containsExactly(
                "KRC, Raidurg, Hyderabad, Telangana",
                "Sector 126, Noida, Uttar Pradesh",
                "Sholinganallur, Chennai, Tamil Nadu",
                "Jigani, Bengaluru, Karnataka",
                "Karle Town Centre, Nagawara, Bengaluru, Karnataka"
        );
    }
}
