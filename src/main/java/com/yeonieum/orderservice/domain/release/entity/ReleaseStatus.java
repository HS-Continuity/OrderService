package com.yeonieum.orderservice.domain.release.entity;

import com.yeonieum.orderservice.global.converter.ReleaseStatusCodeConverter;
import com.yeonieum.orderservice.global.enums.ReleaseStatusCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "release_status")
public class ReleaseStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_status_id")
    private Long releaseStatusId;

    @Convert(converter = ReleaseStatusCodeConverter.class)
    @Column(name = "status_name", nullable = false)
    private ReleaseStatusCode statusName;


}

