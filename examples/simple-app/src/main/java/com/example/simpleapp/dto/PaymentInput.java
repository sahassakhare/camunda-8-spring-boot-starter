package com.example.simpleapp.dto;

import java.math.BigDecimal;

public record PaymentInput(String cardNumber, String cvc, String expiryDate, BigDecimal amount) {
}
