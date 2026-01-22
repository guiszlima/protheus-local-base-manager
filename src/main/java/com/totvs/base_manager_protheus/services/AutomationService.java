package com.totvs.base_manager_protheus.services;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Service;
import com.totvs.base_manager_protheus.model.ConfigureBaseModel;

@Service
public class AutomationService {

    public void realizarProcessosAutomacao(ConfigureBaseModel configuracao) {
        System.out.println(ToStringBuilder.reflectionToString(configuracao, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println("Processando a criação da base: " + configuracao.getVersionProtheus());

        FileEstructureService fileEstructureConstructor = new FileEstructureService(configuracao);
        fileEstructureConstructor.createDirectoryStructure();

        DriverInstallerService.installDriver(configuracao.getDatabaseType());

        ODBCConfigurationService.configureSystemDSN(configuracao);
    }
}
