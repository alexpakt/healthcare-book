package com.healthcare.childrecords.dto.response;

import com.healthcare.childrecords.entity.DeliveryType;

public record BirthRecordResponse(
        Long id,
        Long childId,
        String childName,
        Integer birthWeightGrams,
        Double birthLengthCm,
        Double birthHeadCircumferenceCm,
        Integer gestationalAgeWeeks,
        DeliveryType deliveryType,
        Integer apgarScore1Min,
        Integer apgarScore5Min,
        String birthHospital,
        Boolean nicuAdmission,
        Integer nicuDurationDays,
        String notes
) {}
