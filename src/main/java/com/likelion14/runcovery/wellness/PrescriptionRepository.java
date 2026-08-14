package com.likelion14.runcovery.wellness;

import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionRepository extends Repository<Prescription, Long> {

    List<Prescription> findByPrescriptionDate(LocalDate prescriptionDate);
}
