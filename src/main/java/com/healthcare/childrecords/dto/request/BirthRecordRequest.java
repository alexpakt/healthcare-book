package com.healthcare.childrecords.dto.request;

import com.healthcare.childrecords.entity.DeliveryType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BirthRecordRequest(
        @NotNull Long childId,

        @Positive Integer birthWeightGrams,
        @Positive Double birthLengthCm,
        @Positive Double birthHeadCircumferenceCm,

        @Min(22) @Max(45) Integer gestationalAgeWeeks,

        DeliveryType deliveryType,

        @Min(0) @Max(10) Integer apgarScore1Min,
        @Min(0) @Max(10) Integer apgarScore5Min,

        String birthHospital,
        Boolean nicuAdmission,
        @Positive Integer nicuDurationDays,
        String notes
) {}
