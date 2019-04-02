package cern.lhc.commons.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("status")
public class StatusController {

    @GetMapping("alive")
    public ResponseEntity<Void> alive() {
        return ResponseEntity.ok().build();
    }
}
