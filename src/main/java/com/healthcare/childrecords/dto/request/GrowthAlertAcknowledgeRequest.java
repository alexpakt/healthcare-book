package com.healthcare.childrecords.dto.request;

import jakarta.validation.constraints.Size;

public record GrowthAlertAcknowledgeRequest(
        @Size(max = 500) String note
) {}
