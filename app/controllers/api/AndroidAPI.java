package controllers.api;

import org.jetbrains.annotations.NotNull;

class AndroidAPI implements Comparable<AndroidAPI> {

    private final Integer apiValue;

    AndroidAPI(Integer apiValue) {
        this.apiValue = apiValue;
    }

    public Integer getApiValue() {
        return apiValue;
    }

    static AndroidAPI fromString(String value) {
        try {
            int apiValue = Integer.parseInt(value.replace("API", ""));
            return new AndroidAPI(apiValue);
        } catch (NumberFormatException e) {
            return new AndroidAPI(Integer.MAX_VALUE);//TODO: Review this
        }
    }

    @Override
    public int compareTo(@NotNull AndroidAPI other) {
        return getApiValue().compareTo(other.getApiValue());
    }
}
