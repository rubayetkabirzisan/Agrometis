package com.rubaet.agrihub.ui.weather;

import com.rubaet.agrihub.SceneTransition;
import com.rubaet.agrihub.state.AppState;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherController {

    private int currentDayIndex = 0;
    private JsonNode forecastList;
    private String currentCity;
    private String currentCountry;

    @FXML private Label time;
    @FXML private Label city;
    @FXML private Label country;
    @FXML private Label date;
    @FXML private Label day;
    @FXML private Label humidity;
    @FXML private Label pressure;
    @FXML private TextField readCity;
    @FXML private Label temperature;
    @FXML private Label visibility;
    @FXML private Label wind;
    @FXML private Label description;
    @FXML private Label showerror;
    @FXML private ImageView weatherHomelogo;

    public class InvalidCityNameException extends Exception {
        public InvalidCityNameException(String message) {
            super(message);
        }
    }

    private void validateCityName(String city) throws InvalidCityNameException {
        if (city == null || city.isEmpty() || !Character.isUpperCase(city.charAt(0)) || !city.substring(1).equals(city.substring(1).toLowerCase())) {
            throw new InvalidCityNameException("City name must start with a capital letter and be followed by lowercase letters (e.g. 'Dhaka').");
        }
    }

    @FXML
    void button(ActionEvent event) {
        String cityText = readCity.getText().trim();
        showerror.setText(null);

        try {
            validateCityName(cityText);
            
            com.rubaet.agrihub.service.ApiService.getWeatherData(cityText)
                .thenAccept(jsonData -> {
                    javafx.application.Platform.runLater(() -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(jsonData);
                            if (root.has("list")) {
                                this.forecastList = root.path("list");
                                this.currentCity = root.path("city").path("name").asText();
                                this.currentCountry = root.path("city").path("country").asText();
                                this.currentDayIndex = 0;
                                updateLabels(currentDayIndex);
                            } else if (root.has("title")) {
                                showerror.setText("Server Error: " + root.get("detail").asText());
                            }
                        } catch (Exception e) {
                            showerror.setText("Error parsing weather data.");
                        }
                    });
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> showerror.setText("Error connecting to backend API."));
                    return null;
                });

        } catch (InvalidCityNameException e) {
            showerror.setText("Error: " + e.getMessage());
        }
    }

    private void updateLabels(int index) {
        if (forecastList == null || index < 0 || index >= forecastList.size()) return;
        JsonNode item = forecastList.get(index);
        
        String dtTxt = item.path("dt_txt").asText();
        String[] parts = dtTxt.split(" ");
        String dateStr = parts[0];
        String timeStr = parts.length > 1 ? parts[1] : "";
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dayStr = "";
        try {
            Date d = format.parse(dateStr);
            dayStr = new SimpleDateFormat("EEEE").format(d);
        } catch(Exception ignored) {}

        double tempKelvin = item.path("main").path("temp").asDouble();
        double tempCelsius = Math.round((tempKelvin - 273.15) * 100.0) / 100.0;
        
        int pressureVal = item.path("main").path("pressure").asInt();
        int humidityVal = item.path("main").path("humidity").asInt();
        double windSpeed = item.path("wind").path("speed").asDouble();
        String mainDesc = item.path("weather").get(0).path("main").asText();
        int vis = item.path("visibility").asInt();

        time.setText(timeStr);
        city.setText(currentCity);
        country.setText(currentCountry);
        date.setText(dateStr);
        day.setText(dayStr);
        humidity.setText(String.valueOf(humidityVal));
        pressure.setText(String.valueOf(pressureVal));
        temperature.setText(String.valueOf(tempCelsius));
        visibility.setText(String.valueOf(vis));
        wind.setText(String.valueOf(windSpeed));
        description.setText(mainDesc);
    }

    @FXML
    void nextday(ActionEvent event) {
        if (forecastList != null) {
            currentDayIndex++;
            if (currentDayIndex >= forecastList.size()) currentDayIndex = forecastList.size() - 1;
            updateLabels(currentDayIndex);
        }
    }

    @FXML
    void prevday(ActionEvent event) {
        if (forecastList != null) {
            currentDayIndex--;
            if (currentDayIndex < 0) currentDayIndex = 0;
            updateLabels(currentDayIndex);
        }
    }

    @FXML
    public void homeBtn() {
        SceneTransition.navigateTo(
            AppState.getInstance().getPrimaryStage(),
            "/com/rubaet/agrihub/Functions.fxml",
            "Agri-Hub — Dashboard"
        );
    }
}
