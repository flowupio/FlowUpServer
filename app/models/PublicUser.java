package models;

import lombok.Data;

import java.util.UUID;

@Data
public class PublicUser {

    private final UUID id;
    private final String name;
    private final String email;
    private final boolean hasApplications;

}
