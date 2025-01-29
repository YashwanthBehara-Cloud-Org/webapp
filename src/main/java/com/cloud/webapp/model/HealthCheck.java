package com.cloud.webapp.model;

import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "health_check")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Check_Id")
    private Long checkId;

    // Store the UTC date-time in an Instant
    @Column(name = "Datetime", nullable = false)
    private Instant dateTime;
}
