package com.pamperpaw.visit.dto;

import com.pamperpaw.visit.entity.PaymentMethod;
import com.pamperpaw.visit.entity.PaymentStatus;
import com.pamperpaw.visit.entity.VisitStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    @Test
    void visitRequestDtoTest() {

        VisitRequestDTO dto = new VisitRequestDTO();

        dto.setCustomerId(1L);
        dto.setVetId(2L);
        dto.setPetId(3L);
        dto.setVisitDate("2026-05-08");
        dto.setTimeSlot("10 AM - 11 AM");
        dto.setReason("Checkup");
        dto.setPaymentMethod(PaymentMethod.ONLINE);

        assertEquals(1L, dto.getCustomerId());
        assertEquals(2L, dto.getVetId());
        assertEquals(3L, dto.getPetId());
        assertEquals("2026-05-08", dto.getVisitDate());
        assertEquals("10 AM - 11 AM", dto.getTimeSlot());
        assertEquals("Checkup", dto.getReason());

        assertEquals(
                PaymentMethod.ONLINE,
                dto.getPaymentMethod());
    }

    @Test
    void visitResponseDtoTest() {

        VisitResponseDTO dto = new VisitResponseDTO();

        dto.setId(1L);
        dto.setCustomerId(2L);
        dto.setVetId(3L);
        dto.setPetId(4L);
        dto.setVisitDate("2026-05-08");
        dto.setTimeSlot("10 AM - 11 AM");
        dto.setReason("Vaccination");
        dto.setStatus(VisitStatus.CONFIRMED);
        dto.setPaymentMethod(PaymentMethod.CASH);
        dto.setPaymentStatus(PaymentStatus.SUCCESS);
        dto.setConsultationFee(BigDecimal.valueOf(500));

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getCustomerId());
        assertEquals(3L, dto.getVetId());
        assertEquals(4L, dto.getPetId());

        assertEquals(
                "2026-05-08",
                dto.getVisitDate());

        assertEquals(
                "10 AM - 11 AM",
                dto.getTimeSlot());

        assertEquals(
                "Vaccination",
                dto.getReason());

        assertEquals(
                VisitStatus.CONFIRMED,
                dto.getStatus());

        assertEquals(
                PaymentMethod.CASH,
                dto.getPaymentMethod());

        assertEquals(
                PaymentStatus.SUCCESS,
                dto.getPaymentStatus());

        assertEquals(
                BigDecimal.valueOf(500),
                dto.getConsultationFee());
    }

    @Test
    void paymentResponseTest() {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentStatus(PaymentStatus.SUCCESS);

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus());
    }

    @Test
    void paymentInitiateResponseTest() {

        PaymentInitiateResponse response =
                new PaymentInitiateResponse();

        response.setPaymentId(1L);
        response.setAppointmentId(2L);
        response.setUserId(3L);
        response.setAmount(BigDecimal.valueOf(500));

        response.setPaymentMethod(
                PaymentMethod.ONLINE);

        response.setPaymentStatus(
                PaymentStatus.SUCCESS);

        response.setRazorpayOrderId(
                "order_123");

        response.setRazorpayKeyId(
                "rzp_test_123");

        response.setCurrency("INR");

        assertEquals(
                1L,
                response.getPaymentId());

        assertEquals(
                2L,
                response.getAppointmentId());

        assertEquals(
                3L,
                response.getUserId());

        assertEquals(
                BigDecimal.valueOf(500),
                response.getAmount());

        assertEquals(
                PaymentMethod.ONLINE,
                response.getPaymentMethod());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus());

        assertEquals(
                "order_123",
                response.getRazorpayOrderId());

        assertEquals(
                "rzp_test_123",
                response.getRazorpayKeyId());

        assertEquals(
                "INR",
                response.getCurrency());
    }

    @Test
    void petDtoTest() {

        PetDTO dto = new PetDTO();

        dto.setId(1L);
        dto.setName("Max");

        assertEquals(1L, dto.getId());
        assertEquals("Max", dto.getName());
    }

    @Test
    void vetFeeResponseTest() {

        VetFeeResponse response =
                new VetFeeResponse(
                        1L,
                        BigDecimal.valueOf(500));

        assertEquals(
                1L,
                response.getVetId());

        assertEquals(
                BigDecimal.valueOf(500),
                response.getConsultationFee());
    }

    @Test
    void updateVisitPaymentStatusRequestTest() {

        UpdateVisitPaymentStatusRequest request =
                new UpdateVisitPaymentStatusRequest();

        request.setPaymentStatus(
                PaymentStatus.SUCCESS);

        assertEquals(
                PaymentStatus.SUCCESS,
                request.getPaymentStatus());
    }

    @Test
    void updateVisitStatusRequestTest() {

        UpdateVisitStatusRequest request =
                new UpdateVisitStatusRequest();

        request.setStatus(
                VisitStatus.CANCELLED);

        assertEquals(
                VisitStatus.CANCELLED,
                request.getStatus());
    }
    
    @Test
    void paymentInitiateRequestBuilderTest() {

        PaymentInitiateRequest request =
                PaymentInitiateRequest.builder()
                        .appointmentId(1L)
                        .userId(2L)
                        .amount(BigDecimal.valueOf(500))
                        .paymentMethod(PaymentMethod.ONLINE)
                        .build();

        assertEquals(
                1L,
                request.getAppointmentId());

        assertEquals(
                2L,
                request.getUserId());

        assertEquals(
                BigDecimal.valueOf(500),
                request.getAmount());

        assertEquals(
                PaymentMethod.ONLINE,
                request.getPaymentMethod());
    }

    @Test
    void paymentResponseFullCoverageTest() {

        PaymentResponse response =
                new PaymentResponse();

        response.setId(1L);
        response.setAppointmentId(2L);
        response.setUserId(3L);

        response.setAmount(
                BigDecimal.valueOf(700));

        response.setPaymentMethod(
                PaymentMethod.CASH);

        response.setPaymentStatus(
                PaymentStatus.SUCCESS);

        response.setTransactionId(
                "txn_123");

        response.setRazorpayOrderId(
                "order_123");

        assertEquals(1L, response.getId());

        assertEquals(
                2L,
                response.getAppointmentId());

        assertEquals(
                3L,
                response.getUserId());

        assertEquals(
                BigDecimal.valueOf(700),
                response.getAmount());

        assertEquals(
                PaymentMethod.CASH,
                response.getPaymentMethod());

        assertEquals(
                PaymentStatus.SUCCESS,
                response.getPaymentStatus());

        assertEquals(
                "txn_123",
                response.getTransactionId());

        assertEquals(
                "order_123",
                response.getRazorpayOrderId());
    }

    @Test
    void petDtoFullCoverageTest() {

        PetDTO dto = new PetDTO();

        dto.setId(1L);
        dto.setName("Max");
        dto.setType("Dog");
        dto.setAge(5);
        dto.setImageUrl("image-url");

        assertEquals(1L, dto.getId());

        assertEquals(
                "Max",
                dto.getName());

        assertEquals(
                "Dog",
                dto.getType());

        assertEquals(
                5,
                dto.getAge());

        assertEquals(
                "image-url",
                dto.getImageUrl());
    }

    @Test
    void vetLeaveRequestDtoTest() {

        VetLeaveRequestDTO dto =
                new VetLeaveRequestDTO();

        dto.setVetId(1L);
        dto.setDate("2026-05-08");

        assertEquals(
                1L,
                dto.getVetId());

        assertEquals(
                "2026-05-08",
                dto.getDate());
    }

    @Test
    void vetLeaveResponseDtoTest() {

        VetLeaveResponseDTO dto =
                new VetLeaveResponseDTO();

        dto.setId(1L);
        dto.setVetId(2L);
        dto.setDate("2026-05-08");

        assertEquals(1L, dto.getId());

        assertEquals(
                2L,
                dto.getVetId());

        assertEquals(
                "2026-05-08",
                dto.getDate());
    }

    @Test
    void visitResponseDtoFullCoverageTest() {

        VisitResponseDTO dto =
                new VisitResponseDTO();

        PaymentInitiateResponse payment =
                new PaymentInitiateResponse();

        payment.setPaymentId(100L);

        dto.setPayment(payment);
        dto.setPetName("Max");

        assertEquals(
                "Max",
                dto.getPetName());

        assertNotNull(dto.getPayment());

        assertEquals(
                100L,
                dto.getPayment().getPaymentId());
    }
}