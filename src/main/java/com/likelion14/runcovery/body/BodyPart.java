package com.likelion14.runcovery.body;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BodyPart {

    @Id
    @Column(name = "body_part_code", length = 30)
    private String bodyPartCode;

    @Column(nullable = false, length = 50)
    private String bodyName;

    @Column(length = 30)
    private String side;

    @Column(length = 30)
    private String direction;

    public BodyPart(String bodyPartCode, String bodyName, String side, String direction) {
        this.bodyPartCode = bodyPartCode;
        this.bodyName = bodyName;
        this.side = side;
        this.direction = direction;
    }

    public void update(String bodyName, String side, String direction) {
        this.bodyName = bodyName;
        this.side = side;
        this.direction = direction;
    }
}
