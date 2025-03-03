package org.example.autopark.appUtil.trackGeneration;

import org.example.autopark.appUtil.ValidationBindingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController("api/track/generate")
@RequestMapping
public class TrackGenController {
    private final TrackGenService trackGenService;

    @Autowired
    public TrackGenController(TrackGenService trackGenService) {
        this.trackGenService = trackGenService;
    }

    /**
     * Пока что делаем по аналогии с DataGenController
     * @param request
     * @return Void
     */
    @PostMapping
    public ResponseEntity<Void> generateTrack(@RequestBody TrackGenDTO request,
                                              BindingResult bindingResult) {
        ValidationBindingUtil.Binding(bindingResult);
        trackGenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
