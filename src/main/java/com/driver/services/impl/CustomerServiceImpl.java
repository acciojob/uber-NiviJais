package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> optionalCustomer = customerRepository2.findById(customerId);
		optionalCustomer.ifPresent(customer -> customerRepository2.delete(customer));
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> availableDrivers = driverRepository2.findByCabAvailableIsTrueOrderByDriverId();

		if (availableDrivers.isEmpty()) {
			throw new Exception("No cab available!");
		}

		// Choose the driver with the lowest driverId (assuming lower driverId means the driver is available sooner)
		Driver selectedDriver = availableDrivers.get(0);

		// Mark the selected driver as unavailable
		selectedDriver.setCabAvailable(false);
		driverRepository2.save(selectedDriver);

		// Create a new TripBooking
		TripBooking tripBooking = new TripBooking();
		tripBooking.setCustomer(customerRepository2.findById(customerId).orElseThrow(() -> new Exception("Customer not found")));
		tripBooking.setDriver(selectedDriver);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setTripStatus(TripStatus.CONFIRMED);

		// Save the TripBooking
		return tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		optionalTripBooking.ifPresent(tripBooking -> {
			tripBooking.setTripStatus(TripStatus.CANCELED);
			// Update driver availability
			tripBooking.getDriver().setCabAvailable(true);
			driverRepository2.save(tripBooking.getDriver());
			tripBookingRepository2.save(tripBooking);
		});
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		optionalTripBooking.ifPresent(tripBooking -> {
			tripBooking.setTripStatus(TripStatus.COMPLETED);
			// Update driver availability
			tripBooking.getDriver().setCabAvailable(true);
			driverRepository2.save(tripBooking.getDriver());
			tripBookingRepository2.save(tripBooking);
		});
	}
}
