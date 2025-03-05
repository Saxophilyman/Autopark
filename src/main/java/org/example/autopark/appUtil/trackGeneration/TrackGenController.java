package org.example.autopark.appUtil.trackGeneration;

import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/generate")
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
    @PostMapping("track")
    public ResponseEntity<Void> generateTrack(@CurrentManagerId @RequestBody TrackGenDTO request,
                                              BindingResult bindingResult) {
        System.out.println("Запрос дошёл до контроллера!");
        ValidationBindingUtil.Binding(bindingResult);
        trackGenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
