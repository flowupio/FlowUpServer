package controllers.view;

public class ApplicationViewModel {
    private String id;
    private String name;

    public ApplicationViewModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
