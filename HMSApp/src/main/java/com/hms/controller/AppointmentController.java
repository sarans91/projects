package com.hms.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hms.entities.Appointment;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
	
	

    @Autowired
    private AppointmentService appointmentService;
    
    
    @GetMapping("/{apid}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable int apid) throws InvalidEntityException {
        Appointment appointment = appointmentService.getAppointmentById(apid);
        if(appointment==null) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }
    @PostMapping("/bookAppointment/{doctorId}/{patientId}")
    public ResponseEntity<?> createAppointment(@PathVariable("doctorId") int doctorId, 
                                               @PathVariable("patientId") int patientId, 
                                               @Valid @RequestBody Appointment appointment) throws InvalidEntityException {
        
        Appointment savedAppointment = appointmentService.saveAppointment(appointment, doctorId, patientId);
            return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
    }
    @PutMapping("/reschedule/{appointmentId}")
    public ResponseEntity<String> rescheduleAppointment(
            @PathVariable int appointmentId,
            @RequestParam LocalDate newDate,
            @RequestParam LocalTime newTime) throws InvalidEntityException {
        String response = appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@PathVariable("appointmentId") int appointmentId)throws InvalidEntityException {
            appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.ok("Appointment has been successfully cancelled.");   
       
    }
    @GetMapping("/patientsWithAppointmentCurrentDay")
    public ResponseEntity<List<Patient>> getPatientsWithAppointmentCurrentDay() throws InvalidEntityException {
        LocalDate currentDate = LocalDate.now();
        List<Patient> patients = appointmentService.getPatientsWithAppointmentsOnDate(currentDate);
        if(patients==null || patients.isEmpty())
        	throw new InvalidEntityException("No patients with appointment for today");
        return ResponseEntity.ok(patients);
    }
    @GetMapping("/appointmentsForDate/{date}")
    public List<Appointment> viewAppointmentsForDate(@PathVariable("date") String date) throws InvalidEntityException {
        LocalDate requestedDate = LocalDate.parse(date); // Convert string date to LocalDate
        List<Appointment> patients = appointmentService.getAppointmentsForDate(requestedDate);
        if(patients==null || patients.isEmpty())
        	throw new InvalidEntityException("No appointments for today");
        return patients;
    }
    @PutMapping("/update/{appointmentId}")
    public ResponseEntity<String> updateAppointment(@PathVariable("appointmentId") int appointmentId, @RequestBody Appointment appointment)throws InvalidEntityException {
        try {
        	appointmentService.updateAppointmentDetails(appointmentId, appointment.getDoctorReport(), appointment.getMedicineSuggested());
            return ResponseEntity.ok("Appointment updated successfully.");
        } catch (InvalidEntityException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/all")
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments(); // Fetches all appointments
    }
    @GetMapping("/lowConsultationDoctors")
    public ResponseEntity<List<Doctor>> getLowConsultationDoctors(@RequestParam("startDate") String startDate,
                                                                   @RequestParam("endDate") String endDate) {
        try {
            // Convert the string dates to LocalDate
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // Fetch the low consultation doctors from the service
            List<Doctor> lowFeeDoctors = appointmentService.getDoctorsWithLowConsultationFee(start, end);

            // If no doctors found, return an empty list
            if (lowFeeDoctors.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content if no doctors found
            }

            // Return the list of doctors as JSON with OK status
            return new ResponseEntity<>(lowFeeDoctors, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Return the error message if the dates are invalid
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Return 400 BAD_REQUEST if there's an issue with input
        }
    }
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsForDoctor(
            @PathVariable int doctorId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws InvalidEntityException {
        
            List<Appointment> appointments = appointmentService.getAppointmentsForDoctor(doctorId, startDate, endDate);

            // Return 204 No Content if no appointments found
            if (appointments.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(appointments);
       
    


}
}