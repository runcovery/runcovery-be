package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.wellness.entity.Prescription;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionRepository extends Repository<Prescription, Long> {

    List<Prescription> findByPrescriptionDate(LocalDate prescriptionDate);
}
