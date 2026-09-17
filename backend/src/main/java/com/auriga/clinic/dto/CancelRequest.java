package com.auriga.clinic.dto;

import java.time.LocalDateTime;

public class CancelRequest {
    // Optional — agar na bheje, to "abhi" (system time) maan lenge
    private LocalDateTime cancelledAt;

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
}