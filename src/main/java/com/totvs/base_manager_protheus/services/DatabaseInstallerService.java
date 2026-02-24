package com.totvs.base_manager_protheus.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.totvs.base_manager_protheus.utills.ProgramExecutor;

public class DatabaseInstallerService {

    // Caminhos dos instaladores (extraídos em FileEstructureService)
    private static final String DATABASE_INSTALLER_ROOT = "C:\\totvs_protheus_manager\\automacao\\database_installer";
    private static final String PATH_SQLSERVER_INSTALLER_DIR = DATABASE_INSTALLER_ROOT + "\\SQLSERVER";
    private static final String PATH_POSTGRES_INSTALLER_DIR = DATABASE_INSTALLER_ROOT + "\\POSTGRES";

    // Diretório customizado onde SUA ferramenta instala o Postgres
    private static final String TARGET_DIR_POSTGRES = "C:\\base_manager\\data\\postgres";
    private static final String TARGET_EXE_POSTGRES = TARGET_DIR_POSTGRES + "\\bin\\postgres.exe";

    // Caminho do binário SQL Server
    private static final String TARGET_EXE_SQLSERVER_2017 = "C:\\Program Files\\Microsoft SQL Server\\MSSQL14.MSSQLSERVER\\MSSQL\\Binn\\sqlservr.exe";

    // Evita execuções concorrentes por tipo de banco
    private static final ConcurrentHashMap<String, AtomicBoolean> installing = new ConcurrentHashMap<>();

    public boolean installDatabase(String databaseType) {
        if (checkIfInstalled(databaseType)) {
            System.out.println("DEBUG: " + databaseType + " já está instalado.");
            return true;
        }

        // lock in-memory to avoid concurrent installs of same DB type
        AtomicBoolean lock = installing.computeIfAbsent(databaseType, k -> new AtomicBoolean(false));
        if (!lock.compareAndSet(false, true)) {
            System.out.println("INFO: Instalação de " + databaseType + " já em andamento. Ignorando requisição.");
            return false;
        }

        try {
            // Obtém o caminho do instalador (já extraído em FileEstructureService)
            String installerDir;
            if (databaseType.equalsIgnoreCase("SQLSERVER")) {
                installerDir = PATH_SQLSERVER_INSTALLER_DIR;
            } else if (databaseType.equalsIgnoreCase("POSTGRES")) {
                installerDir = PATH_POSTGRES_INSTALLER_DIR;
            } else {
                throw new IllegalArgumentException("Tipo de banco de dados desconhecido: " + databaseType);
            }

            // Encontra o executável dentro do diretório
            String installerPath = findInstallerExecutable(installerDir, databaseType);
            if (installerPath == null) {
                System.err.println("ERRO: Executável do instalador não encontrado em: " + installerDir);
                return false;
            }

            String installerName = new File(installerPath).getName();

            // Se o instalador já estiver rodando no sistema (outro processo), não inicia
            // outro
            if (isProcessRunning(installerName)) {
                System.out.println("INFO: Instalador já em execução: " + installerName + ". Ignorando novo start.");
                return false;
            }

            ProcessBuilder processBuilder;
            if (databaseType.equalsIgnoreCase("SQLSERVER")) {
                processBuilder = new ProcessBuilder(installerPath, "/QS");
                System.out.println("DEBUG: Iniciando instalação do SQL Server em segundo plano...");
            } else if (databaseType.equalsIgnoreCase("POSTGRES")) {
                processBuilder = new ProcessBuilder(installerPath, "--mode", "unattended");
                System.out.println("DEBUG: Iniciando instalação do PostgreSQL em segundo plano...");
            } else {
                throw new IllegalArgumentException("Tipo de banco de dados desconhecido: " + databaseType);
            }

            // Configurações do processo
            processBuilder.redirectErrorStream(true);

            // Cria o diretório se não existir
            File targetDir = new File(TARGET_DIR_POSTGRES);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
                System.out.println("DEBUG: Diretório criado: " + targetDir.getAbsolutePath());
            }

            // Inicia o processo em segundo plano
            Process process = processBuilder.start();

            // Monitora a saída do processo
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[INSTALLER] " + line);
            }

            // Aguarda a conclusão do processo
            int exitCode = process.waitFor();
            System.out.println("DEBUG: Instalação concluída com código: " + exitCode);

            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("ERRO durante instalação: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // libera lock
            AtomicBoolean current = installing.get(databaseType);
            if (current != null)
                current.set(false);
        }
    }

    /**
     * Encontra o executável do instalador dentro do diretório
     * 
     * @param installerDir Diretório do instalador
     * @param databaseType Tipo de banco de dados
     * @return Caminho completo do executável ou null se não encontrado
     */
    private String findInstallerExecutable(String installerDir, String databaseType) {
        File dir = new File(installerDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("ERRO: Diretório de instalador não encontrado: " + installerDir);
            return null;
        }

        if (databaseType.equalsIgnoreCase("SQLSERVER")) {
            String exePath = installerDir + "\\SQL2022-SSEI-Dev.exe";
            File file = new File(exePath);
            if (file.exists()) {
                System.out.println("DEBUG: Executável SQL Server encontrado: " + exePath);
                return exePath;
            }
        } else if (databaseType.equalsIgnoreCase("POSTGRES")) {
            String exePath = installerDir + "\\postgresql-15.15-1-windows-x64.exe";
            File file = new File(exePath);
            if (file.exists()) {
                System.out.println("DEBUG: Executável PostgreSQL encontrado: " + exePath);
                return exePath;
            }
        }
        return null;
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

    /**
     * Verifica se um processo com o nome fornecido está em execução no Windows
     * (tasklist)
     */
    private boolean isProcessRunning(String processName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
                    "tasklist /FI \"IMAGENAME eq " + processName + "\"");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase())) {
                    return true;
                }
            }
            p.waitFor();
        } catch (Exception e) {
            // se falhar na verificação, assume que não está rodando
        }
        return false;
    }
}