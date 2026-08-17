package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.body.BodyPart;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;

public interface WellnessBodyPartRepository extends Repository<BodyPart, String> {

    List<BodyPart> findAllByBodyPartCodeIn(Collection<String> bodyPartCodes);
}