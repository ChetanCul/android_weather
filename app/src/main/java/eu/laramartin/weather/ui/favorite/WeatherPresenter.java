package eu.laramartin.weather.ui.favorite;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import eu.laramartin.weather.ui.common.WeatherIcons;
import eu.laramartin.weather.business.WeatherInteractor;
import eu.laramartin.weather.api.model.CurrentWeatherResponse;
import eu.laramartin.weather.api.model.Forecast;
import eu.laramartin.weather.api.model.ForecastResponse;
import eu.laramartin.weather.ui.common.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lara on 21/10/2016.
 */

public class WeatherPresenter {

    private final static String LOG_TAG = WeatherPresenter.class.getCanonicalName();
    private WeatherInteractor interactor;
    WeatherView view;

    public WeatherPresenter(WeatherInteractor interactor) {
        this.interactor = interactor;
    }

    public void bind(WeatherView view) {
        this.view = view;
    }

    public void unbind() {
        view = null;
    }

    public void performCall(String location) {
        Call<ForecastResponse> callForecast = interactor.getForecasts(location);
        callForecast.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.body() == null) {
                    view.setErrorVisibility(true);
                    return;
                }
                int i = 0;
                for (Forecast forecast : response.body().getForecasts()) {
                    view.displayForecast(i, getDayOfTheWeek(forecast.getDate()),
                            (int) forecast.getTemperature().getTempMin(),
                            (int) forecast.getTemperature().getTempMax(),
                            WeatherIcons.getIcon(forecast.getWeather().get(0).getIcon()));
                    i++;
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                view.setErrorVisibility(true);
                Log.e(LOG_TAG, "error in ForecastResponse: " + t.getLocalizedMessage(), t);
            }
        });
        interactor.getWeather(location).enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.body() == null) {
                    view.setErrorVisibility(true);
                    return;
                }
                view.setContentVisibility(true);
                view.displayCurrentDate(getWholeDateOfCurrentWeather(response.body().getDate()));
                view.displayCurrentHour(getHourFromUnixTime(response.body().getDate()));
                view.displayCurrentTemp((int) response.body().getMain().getTemperature());
                view.displayCurrentDescription(
                        TextUtils.setFirstCharInUppercase(
                                response.body().getWeather().get(0).getDescription()));
                view.displayCurrentHumidity((int) response.body().getMain().getHumidity());
                view.displayCurrentPressure((int) response.body().getMain().getPressure());
                view.displayCurrentWind(response.body().getWind().getWindSpeed());
                view.displayCurrentCity(
                        TextUtils.setFirstCharInUppercase(
                                response.body().getCity()));
                view.displayCurrentIcon(response.body().getWeather().get(0).getIcon());
                view.displayCurrentSunriseSunsetTime(
                        getHourFromUnixTime(response.body().getWeatherSys().getSunrise()),
                        getHourFromUnixTime(response.body().getWeatherSys().getSunset()));
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                view.setErrorVisibility(true);
                Log.e(LOG_TAG, "error in CurrentWeatherResponse: " + t.getLocalizedMessage(), t);
            }
        });
    }

    private String getDayOfTheWeek(long unixTime) {
        Date date = new Date(unixTime * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE");
        return formatter.format(date);
    }

    private String getWholeDateOfCurrentWeather(long unixTime) {
        Date date = new Date(unixTime * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d");
        return formatter.format(date);
    }

    private String getHourFromUnixTime(long unixTime) {
        Date date = new Date(unixTime * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(date);
    }
}