package com.likelion14.runcovery.wellness.entity;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private WellnessReport wellnessReport;

    @ManyToOne
    @JoinColumn(name = "skin_id", nullable = false)
    private SkinRecord skinRecord;

    @Column(nullable = false)
    private LocalDate prescriptionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrescriptionCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Lob
    private String detail;

    private Boolean isCompleted = false;

    private String recommendedLink;

    private String skinResult;

    public Prescription(WellnessReport wellnessReport, SkinRecord skinRecord, LocalDate prescriptionDate,
                         PrescriptionCategory category, String title, String summary) {
        this.wellnessReport = wellnessReport;
        this.skinRecord = skinRecord;
        this.prescriptionDate = prescriptionDate;
        this.category = category;
        this.title = title;
        this.summary = summary;
    }

    public void update(LocalDate prescriptionDate, PrescriptionCategory category, String title, String summary) {
        this.prescriptionDate = prescriptionDate;
        this.category = category;
        this.title = title;
        this.summary = summary;
    }
}

