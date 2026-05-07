package com.pamperpaw.visit.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void visitEntityTest() {

        Visit visit = new Visit();

        visit.setId(1L);
        visit.setCustomerId(2L);
        visit.setVetId(3L);
        visit.setPetId(4L);

        visit.setVisitDate("2026-05-08");

        visit.setReason("Checkup");

        visit.setTimeSlot("10 AM - 11 AM");

        visit.setStatus(VisitStatus.CONFIRMED);

        visit.setPaymentMethod(
                PaymentMethod.ONLINE);

        visit.setPaymentStatus(
                PaymentStatus.SUCCESS);

        visit.setConsultationFee(
                BigDecimal.valueOf(500));

        assertEquals(1L, visit.getId());
        assertEquals(2L, visit.getCustomerId());
        assertEquals(3L, visit.getVetId());
        assertEquals(4L, visit.getPetId());

        assertEquals(
                "2026-05-08",
                visit.getVisitDate());

        assertEquals(
                "Checkup",
                visit.getReason());

        assertEquals(
                "10 AM - 11 AM",
                visit.getTimeSlot());

        assertEquals(
                VisitStatus.CONFIRMED,
                visit.getStatus());

        assertEquals(
                PaymentMethod.ONLINE,
                visit.getPaymentMethod());

        assertEquals(
                PaymentStatus.SUCCESS,
                visit.getPaymentStatus());

        assertEquals(
                BigDecimal.valueOf(500),
                visit.getConsultationFee());
    }

    @Test
    void visitAllArgsConstructorTest() {

        Visit visit = new Visit(
                1L,
                2L,
                3L,
                4L,
                "2026-05-08",
                "Checkup",
                "10 AM - 11 AM",
                VisitStatus.PENDING,
                PaymentMethod.CASH,
                PaymentStatus.PENDING,
                BigDecimal.valueOf(300)
        );

        assertNotNull(visit);
    }

    @Test
    void vetLeaveEntityTest() {

        VetLeave leave = new VetLeave();

        leave.setId(1L);
        leave.setVetId(2L);
        leave.setDate("2026-05-08");

        assertEquals(1L, leave.getId());

        assertEquals(2L, leave.getVetId());

        assertEquals(
                "2026-05-08",
                leave.getDate());
    }

    @Test
    void vetLeaveBuilderTest() {

        VetLeave leave = VetLeave.builder()
                .id(1L)
                .vetId(2L)
                .date("2026-05-08")
                .build();

        assertEquals(1L, leave.getId());

        assertEquals(2L, leave.getVetId());

        assertEquals(
                "2026-05-08",
                leave.getDate());
    }

    @Test
    void vetLeaveAllArgsConstructorTest() {

        VetLeave leave =
                new VetLeave(
                        1L,
                        2L,
                        "2026-05-08");

        assertNotNull(leave);
    }
}