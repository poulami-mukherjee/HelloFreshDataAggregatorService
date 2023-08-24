package com.hellofresh.service.model;

import com.hellofresh.service.exception.InvalidXValueException;
import com.hellofresh.service.exception.InvalidYValueException;

import java.math.BigDecimal;
public record EventData(long timestamp, BigDecimal x, int y) {
    public EventData {

        if (x.compareTo(BigDecimal.ZERO) < 0 || x.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidXValueException("x should be between 0 and 1 inclusive");
        }

        if (x.scale() != 10) {
            throw new InvalidXValueException("x should have exactly 10 fractional digits");
        }

        if (y < 1_073_741_823L || y > 2_147_483_647L) {
            throw new InvalidYValueException("Value of y should be in the range 1,073,741,823..2,147,483,647");
        }

    }
}