package org.example.autopark.appUtil.dataGeneration;

import org.example.autopark.appUtil.ValidationBindingUtil;
import org.example.autopark.customAnnotation.currentManagerId.CurrentManagerId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@RestController
@Profile("!reactive")
@RequestMapping("/api/generate/data")
public class DataGenController {

    private final DataGenService dataGenService;

    @Autowired
    public DataGenController(DataGenService dataGenService) {
        this.dataGenService = dataGenService;
    }

    @PostMapping
    public ResponseEntity<Void> generateData(@CurrentManagerId @RequestBody DataGenDTO request,
                                             BindingResult bindingResult) {
        ValidationBindingUtil.Binding(bindingResult);
        dataGenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
