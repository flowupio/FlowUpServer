package controllers.api;

import controllers.api.DatapointTags;

interface ProcessingUnit extends DatapointTags {
    long getTimestamp();

    double getConsumption();
}
