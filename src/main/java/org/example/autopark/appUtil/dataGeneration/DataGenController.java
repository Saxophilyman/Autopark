package org.example.autopark.appUtil.dataGeneration;

import org.example.autopark.appUtil.ValidationBindingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate")
public class DataGenController {

    private final DataGenService dataGenService;

    @Autowired
    public DataGenController(DataGenService dataGenService) {
        this.dataGenService = dataGenService;
    }

    @PostMapping
    public ResponseEntity<Void> generateData(@RequestBody DataGenDTO request,
                                             BindingResult bindingResult) {
        ValidationBindingUtil.Binding(bindingResult);
        dataGenService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
