package com.madroakos.currencyconverter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.json.JSONObject;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    protected ComboBox<String> baseCurrencyChooser;
    @FXML
    protected ComboBox<String> returnCurrencyChooser;
    @FXML
    protected ImageView exchangeImageView;
    @FXML
    protected TextField baseCurrencyValue;
    @FXML
    protected TextField returnCurrencyValue;
    private final Currencies[] currencies = new Currencies[4];
    private String apiKey;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fillCurrencies();
        loadConfiguration();
        System.out.println(apiKey);
        getCurrentValues();
    }

    private void fillCurrencies() {
        currencies[0] = new Currencies();
        currencies[0].setName("USD");
        currencies[0].setValue(1);
        currencies[1] = new Currencies();
        currencies[1].setName("EUR");
        currencies[2] = new Currencies();
        currencies[2].setName("GBP");
        currencies[3] = new Currencies();
        currencies[3].setName("HUF");
        for (Currencies currency : currencies) {
            baseCurrencyChooser.getItems().add(currency.getName());
            returnCurrencyChooser.getItems().add(currency.getName());
        }
        baseCurrencyChooser.getSelectionModel().select(currencies[0].getName());
        returnCurrencyChooser.getSelectionModel().select(currencies[1].getName());
    }

    private void getCurrentValues() {
        if (apiKey == null) {
            JOptionPane.showMessageDialog(null, "API key not found", "Warning", JOptionPane.WARNING_MESSAGE);
            Platform.exit();
        } else {
            String baseCurrency = "USD";
            StringBuilder targetCurrency = new StringBuilder(currencies[1].getName());

            for (int i = 2; i < currencies.length; i++) {
                if (currencies[i].getName() != null) {
                    targetCurrency.append(",").append(currencies[i].getName());
                }
            }
            String url = "https://openexchangerates.org/api/latest.json" +
                    "?app_id=" + apiKey +
                    "&base=" + baseCurrency +
                    "&symbols=" + targetCurrency;

            try {
                URL apiUrl = new URL(url);

                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());

                    for (int i = 1; i < currencies.length; i++) {
                        currencies[i].setValue(jsonResponse.getJSONObject("rates").getDouble(currencies[i].getName()));
                        System.out.println(currencies[i].getName() + " " + currencies[i].getValue());
                    }
                } else {
                    System.out.println("Error: " + responseCode);
                }

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfiguration() {
        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config.properties")).getPath();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(rootPath));
            apiKey = properties.getProperty("openExchangeRates.appId");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleInputChange() {
        if (!baseCurrencyValue.getCharacters().isEmpty()) {
            double baseValue = 0;
            double returnValue = 0;
            for (Currencies c : currencies) {
                if (c.getName().equals(baseCurrencyChooser.getValue())) {
                    baseValue = c.getValue();
                }
                if (c.getName().equals(returnCurrencyChooser.getValue())) {
                    returnValue = c.getValue();
                }
            }

            if ((baseValue > 0) && (returnValue > 0)) {
                double equals = (returnValue / baseValue) * Double.parseDouble(baseCurrencyValue.textProperty().getValue());
                returnCurrencyValue.setText(String.format("%.3f", equals));
            }
        } else {
            returnCurrencyValue.setText("");
        }
    }
}