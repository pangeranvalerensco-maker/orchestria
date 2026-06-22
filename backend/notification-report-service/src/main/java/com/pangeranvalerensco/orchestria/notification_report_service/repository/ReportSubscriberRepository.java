package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportSubscriberRepository extends JpaRepository<ReportSubscriber, Long> {
    Optional<ReportSubscriber> findByEmail(String email);
}
