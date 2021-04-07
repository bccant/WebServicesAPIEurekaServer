package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final MapsClient mapsClient;
    private final PriceClient priceClient;

    public CarService(CarRepository repository, MapsClient mapsClient, PriceClient priceClient) {
        this.repository = repository;
        this.mapsClient = mapsClient;
        this.priceClient = priceClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        List<Car> carList = repository.findAll();

        for (Car car: carList) {
            car.setPrice(priceClient.getPrice(car.getId()));
            car.setLocation(mapsClient.getAddress(car.getLocation()));
        }

        return carList;
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> optionalCar = repository.findById(id);
        if (!optionalCar.isPresent()) {
            throw new CarNotFoundException("No car with the ID#" + id + "was found");
        }

        Car car = optionalCar.get();

        car.setPrice(priceClient.getPrice(car.getId()));
        car.setLocation(mapsClient.getAddress(car.getLocation()));
        car.setCondition(car.getCondition());
        car.setModifiedAt(LocalDateTime.now());

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        carToBeUpdated.setCondition(car.getCondition());
                        carToBeUpdated.setModifiedAt(LocalDateTime.now());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        //repository.save(car);
        car.setLocation(mapsClient.getAddress(car.getLocation()));
        car.setPrice(priceClient.getPrice(car.getId()));
        car.setCondition(car.getCondition());
        car.setModifiedAt(LocalDateTime.now());

        return repository.save(car);
        //return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> optionalCar = repository.findById(id);
        if (!optionalCar.isPresent()) {
            throw new CarNotFoundException("No car with the ID#" + id + "was found");
        }

        repository.delete(optionalCar.get());
    }
}
