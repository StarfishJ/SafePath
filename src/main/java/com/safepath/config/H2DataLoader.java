package com.safepath.config;

import com.safepath.model.AppUser;
import com.safepath.model.CrimeReport;
import com.safepath.model.UserAlert;
import com.safepath.repository.AppUserRepository;
import com.safepath.repository.CrimeReportRepository;
import com.safepath.repository.UserAlertRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@Profile("h2")
public class H2DataLoader {

    private final AppUserRepository userRepository;
    private final CrimeReportRepository crimeReportRepository;
    private final UserAlertRepository userAlertRepository;

    public H2DataLoader(AppUserRepository userRepository,
                        CrimeReportRepository crimeReportRepository,
                        UserAlertRepository userAlertRepository) {
        this.userRepository = userRepository;
        this.crimeReportRepository = crimeReportRepository;
        this.userAlertRepository = userAlertRepository;
    }

    @PostConstruct
    public void load() {
        // sample user
        AppUser u = new AppUser();
        u.setEmail("test@example.com");
        u.setPasswordHash("password"); 
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);

        // sample crime report
        CrimeReport cr = new CrimeReport();
        cr.setReportNumber("TEST-1");
        cr.setReportDatetime(LocalDateTime.now().minusDays(1));
        cr.setPrecinct("Precinct A");
        cr.setSector("S1");
        cr.setBeat("B1");
        cr.setMcppNeighborhood("Neighborhood");
        cr.setBlurredAddress("100 Test St");
        cr.setBlurredLatitude(47.6);
        cr.setBlurredLongitude(-122.33);
        crimeReportRepository.save(cr);

        // sample user alert
        UserAlert a = new UserAlert();
        a.setUser(u);
        a.setRadiusM(500);
        a.setCenterLat(47.6);
        a.setCenterLon(-122.33);
        a.setCrimeTypeFilter("ASSAULT");
        a.setActiveFlag(Boolean.TRUE);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        userAlertRepository.save(a);
    }
}
