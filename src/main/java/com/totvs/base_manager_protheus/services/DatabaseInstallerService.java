package com.totvs.base_manager_protheus.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import com.totvs.base_manager_protheus.utills.ProgramExecutor;

public class DatabaseInstallerService {

    // Caminhos dos instaladores
    private static final String PATH_SQLSERVER_INSTALLER = "C:\\base_manager\\external-resources\\database\\SQLSERVER\\SQLServer2017-SSEI-Dev.exe";
    private static final String PATH_POSTGRES_INSTALLER = "C:\\base_manager\\external-resources\\database\\POSTGRES\\postgresql-15.15-1-windows-x64.exe";

    // Diretório customizado onde SUA ferramenta instala o Postgres
    private static final String TARGET_DIR_POSTGRES = "C:\\base_manager\\data\\postgres";
    private static final String TARGET_EXE_POSTGRES = TARGET_DIR_POSTGRES + "\\bin\\postgres.exe";

    // Caminho do binário SQL Server
    private static final String TARGET_EXE_SQLSERVER_2017 = "C:\\Program Files\\Microsoft SQL Server\\MSSQL14.MSSQLSERVER\\MSSQL\\Binn\\sqlservr.exe";

    public boolean installDatabase(String databaseType) {
        if (checkIfInstalled(databaseType)) {
            System.out.println("AVISO: O banco de dados " + databaseType + " já parece estar instalado.");
            return true;
        }

        try {
            switch (databaseType) {
                case "MSSQL":
                    System.out.println("Iniciando instalador do SQL Server 2017 (Interface Visual)...");
                    ProgramExecutor.executeProcess(PATH_SQLSERVER_INSTALLER);
                    break;

                case "POSTGRES":
                    System.out.println("Iniciando instalador do PostgreSQL...");
                    String[] pgArgs = { "--datadir", TARGET_DIR_POSTGRES, "--serverport", "5432" };
                    ProgramExecutor.executeProcess(PATH_POSTGRES_INSTALLER, pgArgs);
                    break;

                default:
                    System.err.println("ERRO: Tipo desconhecido: " + databaseType);
                    return false;
            }

            System.out.println("Processo de instalação do " + databaseType + " finalizado.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkIfInstalled(String databaseType) {
        File fileCheck;

        switch (databaseType) {
            case "POSTGRES":
                // 1. Verifica se já instalamos na nossa pasta customizada
                fileCheck = new File(TARGET_EXE_POSTGRES);
                if (fileCheck.exists()) {
                    System.out.println("DEBUG: Postgres encontrado na pasta base_manager.");
                    return true;
                }

                // 2. Verifica se existe um SERVIÇO do Windows rodando (Instalação padrão)
                // O nome do serviço varia, então testamos os mais comuns para a versão 15 ou
                // genérico
                if (isWindowsServiceInstalled("postgresql-x64-15") ||
                        isWindowsServiceInstalled("postgresql-15") ||
                        isWindowsServiceInstalled("postgresql")) {
                    System.out.println("DEBUG: Serviço do PostgreSQL detectado no Windows.");
                    return true;
                }

                // 3. (Opcional) Verifica a pasta padrão do Windows para o Postgres 15
                File standardPath = new File("C:\\Program Files\\PostgreSQL\\15\\bin\\postgres.exe");
                if (standardPath.exists()) {
                    System.out.println("DEBUG: Postgres encontrado em Program Files.");
                    return true;
                }

                return false;

            case "MSSQL":
                // 1. Verifica serviço
                if (isWindowsServiceInstalled("MSSQLSERVER")) {
                    System.out.println("DEBUG: Serviço 'MSSQLSERVER' encontrado.");
                    return true;
                }

                // 2. Verifica executável
                fileCheck = new File(TARGET_EXE_SQLSERVER_2017);
                if (fileCheck.exists()) {
                    System.out.println("DEBUG: Executável do SQL Server 2017 encontrado.");
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    private boolean isWindowsServiceInstalled(String serviceName) {
        try {
            // O comando 'sc query' retorna erro se o serviço não existir, ou status se
            // existir
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "sc query \"" + serviceName + "\"");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                // Se o comando retornar informações de ESTADO, o serviço existe
                if (line.contains("STATE") || line.contains("ESTADO")) {
                    found = true;
                }
                // Se retornar erro 1060, o serviço não existe
            }
            process.waitFor();
            return found;
        } catch (Exception e) {
            return false;
        }
    }
}