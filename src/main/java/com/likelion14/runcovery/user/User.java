package com.likelion14.runcovery.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal height;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(nullable = false)
    private String runningExperience;

    public User(UUID publicId, String nickname, Integer age, String gender, BigDecimal height, BigDecimal weight,
                String runningExperience) {
        this.publicId = publicId;
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.runningExperience = runningExperience;
    }

    public void update(String nickname, Integer age, String gender, BigDecimal height, BigDecimal weight,
                        String runningExperience) {
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.runningExperience = runningExperience;
    }
}
