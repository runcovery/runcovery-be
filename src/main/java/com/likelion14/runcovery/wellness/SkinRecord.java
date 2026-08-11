package com.likelion14.runcovery.wellness;

import com.likelion14.runcovery.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SkinRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skin_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkinRecordType type;

    @Column(nullable = false)
    private LocalDate measuredDate;

    private Integer totalScore;

    @Column(nullable = false)
    private Integer redness;

    @Column(nullable = false)
    private Integer oiliness;

    @Column(nullable = false)
    private Integer texture;

    @Column(nullable = false)
    private Integer pores;

    @Column(nullable = false)
    private Integer blemishes;

    @Column(nullable = false)
    private Integer hydration;

    @Column(nullable = false)
    private Integer pigment;

    private String skinImage;

    public SkinRecord(User user, SkinRecordType type, LocalDate measuredDate, Integer redness, Integer oiliness,
                       Integer texture, Integer pores, Integer blemishes, Integer hydration, Integer pigment) {
        this.user = user;
        this.type = type;
        this.measuredDate = measuredDate;
        this.redness = redness;
        this.oiliness = oiliness;
        this.texture = texture;
        this.pores = pores;
        this.blemishes = blemishes;
        this.hydration = hydration;
        this.pigment = pigment;
    }

    public void update(SkinRecordType type, LocalDate measuredDate, Integer redness, Integer oiliness,
                        Integer texture, Integer pores, Integer blemishes, Integer hydration, Integer pigment) {
        this.type = type;
        this.measuredDate = measuredDate;
        this.redness = redness;
        this.oiliness = oiliness;
        this.texture = texture;
        this.pores = pores;
        this.blemishes = blemishes;
        this.hydration = hydration;
        this.pigment = pigment;
    }
}
