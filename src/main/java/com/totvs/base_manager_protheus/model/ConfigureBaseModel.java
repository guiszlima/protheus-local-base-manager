package com.totvs.base_manager_protheus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Gera Getters, Setters, toString, equals e hashCode automaticamente
@NoArgsConstructor // Gera o construtor vazio (essencial para o Spring instanciar o objeto)
@AllArgsConstructor
public class ConfigureBaseModel {
    private String baseName;
    private String versionProtheus;
    private String databaseType;
    private String serverPort;

    // Getters e Setters são obrigatórios!

}
