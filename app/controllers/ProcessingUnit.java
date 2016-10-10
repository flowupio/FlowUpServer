package controllers;

interface ProcessingUnit extends DatapointTags {
    long getTimestamp();

    double getConsumption();
}
