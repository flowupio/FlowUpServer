package controllers;

import lombok.Data;
import usecases.InsertResult;

@Data
class ReportResponse {
    private final String message;
    private final InsertResult result;
}
