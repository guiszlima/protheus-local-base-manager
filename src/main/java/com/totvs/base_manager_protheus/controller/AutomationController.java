package com.totvs.base_manager_protheus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.totvs.base_manager_protheus.model.ConfigureBaseModel;
import com.totvs.base_manager_protheus.services.AutomationService;

@Controller // <--- 1. Define que isso é um Controller MVC
@RequestMapping("/") // <--- 2. Define o prefixo da URL (opcional)
public class AutomationController {

    @Autowired
    private AutomationService automationService;

    // Acessado via: http://localhost:8080/automacao/painel
    @GetMapping("/automacao")
    public String mostrarFormulario(Model model) {
        model.addAttribute("configuracao", new ConfigureBaseModel());
        return "formAutomation"; // Nome do arquivo HTML em templates
    }

    // Recebe os dados
    @PostMapping("/processar-base")
    public String processarFormulario(@ModelAttribute ConfigureBaseModel configuracao) {
        automationService.realizarProcessosAutomacao(configuracao);
        return "sucesso"; // Redireciona para uma página de sucesso
    }
}