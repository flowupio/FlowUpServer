package models;

public class UserToPublicUserMapper {

    public PublicUser map(User user, Organization organization) {
        return new PublicUser(user.getId(),
                user.getName(),
                user.getEmail(),
                organization.hasApplications());
    }
}
