package br.com.urnacidada.urna_backend.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class StatusController {

    @GetMapping("/status")
    public String verificarStatus() {
        return "Urna Cidadã funcionando!";
    }
}