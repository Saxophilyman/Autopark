package org.example.autopark.gpx;

import lombok.RequiredArgsConstructor;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.example.autopark.entity.Vehicle;
import org.example.autopark.repository.VehicleRepository;
import org.example.autopark.trip.TripService;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Profile("!reactive")
@RequestMapping("/managers")
public class TripUploadController {

    private final TripUploadService tripUploadService;
    private final VehicleRepository vehicleRepository;

    /**
     * Открытие формы загрузки поездки (по vehicleId для возврата назад)
     */
    @GetMapping("/uploadTripGpx")
    public String showUploadFormWithReturn(@CurrentManagerId @RequestParam("vehicleId") Long vehicleId, Model model) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("ТС не найдено"));

        model.addAttribute("vehicleId", vehicle.getVehicleId());
        model.addAttribute("enterpriseId", vehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId());
        return "trip/upload-form";
    }



    @PostMapping("/upload")
    public String handleUpload(@RequestParam String licensePlate,
                               @RequestParam Long vehicleIdBack,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                               @RequestParam MultipartFile gpxFile,
                               RedirectAttributes redirectAttributes) {

        try {
            tripUploadService.uploadTripFromGpx(licensePlate, start, end, gpxFile);
            redirectAttributes.addFlashAttribute("message", "Поездка успешно загружена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки: " + e.getMessage());
            return "redirect:/trips/uploadTripGpxBack?vehicleId=" + vehicleIdBack;
        }

        // Перенаправление назад на страницу ТС
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new RuntimeException("Машина не найдена"));

        Long enterpriseId = vehicle.getEnterpriseOwnerOfVehicle().getEnterpriseId();
        Long vehicleId = vehicle.getVehicleId();

        return "redirect:/managers/enterprises/" + enterpriseId + "/vehicles/" + vehicleId;
    }





}
