package com.likelion14.runcovery.body;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BodyIssueId implements Serializable {
    private Long userId;

    @Column(length = 30)
    private String bodyPartCode;
}
