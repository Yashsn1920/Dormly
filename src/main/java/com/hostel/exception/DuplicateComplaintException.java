package com.hostel.exception;

import com.hostel.model.Complaint;

import java.io.Serial;
import java.util.Objects;

public final class DuplicateComplaintException extends BusinessRuleException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Complaint existingComplaint;

    public DuplicateComplaintException(Complaint existingComplaint) {
        super("A similar open complaint already exists for this room and category.");
        this.existingComplaint = Objects.requireNonNull(existingComplaint,
                "existingComplaint cannot be null");
    }

    public Complaint getExistingComplaint() {
        return existingComplaint;
    }
}
