package com.likelion14.runcovery.body;

import com.likelion14.runcovery.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BodyIssue {

    @EmbeddedId
    private BodyIssueId id;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("bodyPartCode")
    @ManyToOne
    @JoinColumn(name = "body_part_code", nullable = false)
    private BodyPart bodyPart;

    @Column(nullable = false)
    private Boolean isPainful = false;

    public BodyIssue(User user, BodyPart bodyPart, Boolean isPainful) {
        this.user = user;
        this.bodyPart = bodyPart;
        this.id = new BodyIssueId(user.getId(), bodyPart.getBodyPartCode());
        this.isPainful = isPainful;
    }

    public void update(Boolean isPainful) {
        this.isPainful = isPainful;
    }
}
