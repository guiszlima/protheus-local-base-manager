package com.totvs.base_manager_protheus.controller;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.totvs.base_manager_protheus.services.FileEstructureService;
import com.totvs.base_manager_protheus.model.ConfigureBaseModel;

@Controller // <--- 1. Define que isso é um Controller MVC
@RequestMapping("/") // <--- 2. Define o prefixo da URL (opcional)
public class AutomationController {

    // Acessado via: http://localhost:8080/automacao/painel
    @GetMapping("/automacao")
    public String mostrarFormulario(Model model) {
        model.addAttribute("configuracao", new ConfigureBaseModel());
        return "formAutomation"; // Nome do arquivo HTML em templates
    }

    // Recebe os dados
    @PostMapping("/processar-base")
    public String processarFormulario(@ModelAttribute ConfigureBaseModel configuracao) {
        System.out.println(ToStringBuilder.reflectionToString(configuracao, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println("Processando a criação da base: " + configuracao.getVersionProtheus());
        FileEstructureService fileEstructureConstructor = new FileEstructureService(configuracao);
        fileEstructureConstructor.createDirectoryStructure();

        return "sucesso"; // Redireciona para uma página de sucesso
    }
}