package org.example.autopark.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!reactive")
@RequiredArgsConstructor
public class ControllerHelper {

}
