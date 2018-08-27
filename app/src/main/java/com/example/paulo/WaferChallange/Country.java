package com.example.paulo.WaferChallange;

public class Country {

    private String countryName;
    private String currency;
    private String language;

    public Country(String countryName, String currency, String language) {
        this.countryName = countryName;
        this.currency = currency;
        this.language = language;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLangauge(String langauge) {
        this.language = language ;
    }
}
