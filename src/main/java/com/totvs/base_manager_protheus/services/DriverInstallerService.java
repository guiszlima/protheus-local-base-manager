package com.totvs.base_manager_protheus.services;

import com.totvs.base_manager_protheus.utills.ProgramExecutor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DriverInstallerService {

    final static String SQLSERVER_DRIVER_64BIT_PATH = "C:\\totvs_protheus_manager\\automacao\\odbc_drivers\\MSSQL\\msodbcsql.msi";
    final static String POSTGRESQL_DRIVER_64BIT_PATH = "C:\\totvs_protheus_manager\\automacao\\odbc_drivers\\POSTGRES\\psqlodbc-setup.exe";
    final static String[] SILENT_INSTALL_ARGS_EXE = { "/S", "/norestart" };
    final static String[] SILENT_INSTALL_ARGS_MSI = { "/i", "", "/quiet", "/norestart" };

    // Chave do Registry onde os drivers 64-bit estão registrados
    private static final String REGISTRY_ODBC_DRIVERS_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBCINST.INI";

    public static boolean installDriver(String databaseType) {
        String driverName = getDriverName(databaseType);
        String driverPath = getDriverPath(databaseType);

        // Verifica se o driver já está instalado
        if (isDriverInstalled(driverName)) {
            System.out.println("Driver ODBC já está instalado: " + driverName);
            return true;
        }

        System.out.println("Driver não encontrado. Proceedendo com a instalação...");

        // Se não estiver instalado, executa a instalação
        switch (databaseType) {
            case "MSSQL":
                System.out.println("Iniciando a instalação do driver ODBC para SQL Server...");
                ProgramExecutor.executeMSI(driverPath, null, 1);
                System.out.println("Driver ODBC para SQL Server instalado com sucesso.");
                return true;
            case "POSTGRES":
                System.out.println("Iniciando a instalação do driver ODBC para PostgreSQL...");
                ProgramExecutor.executeProcess(driverPath, SILENT_INSTALL_ARGS_EXE);
                System.out.println("Driver ODBC para PostgreSQL instalado com sucesso.");
                return true;
            case "ORACLE":
                System.out.println("WIP");
                return true;
            default:
                System.err.println("ERRO: Banco de dados não suportado para instalação de driver: " + databaseType);
                return false;
        }
    }

    // Verifica se o driver está instalado no Windows (via Registry)
    private static boolean isDriverInstalled(String driverName) {
        try {
            System.out.println("Verificando se o driver está instalado: " + driverName);

            // Comando para listar todos os drivers do Registry
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBCINST.INI\"");

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            System.out.println("Drivers encontrados no Registry:");
            boolean driverFound = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Procura por linhas que começam com "HKEY_LOCAL_MACHINE" (essas linhas contêm
                // os nomes dos drivers)
                if (line.startsWith("HKEY_LOCAL_MACHINE")) {
                    // Extrai o nome do driver da linha
                    // Formato: HKEY_LOCAL_MACHINE\SOFTWARE\ODBC\ODBCINST.INI\PostgreSQL ANSI
                    String[] parts = line.split("\\\\");
                    if (parts.length > 0) {
                        String driver = parts[parts.length - 1].trim();
                        if (!driver.isEmpty()) {
                            System.out.println("  - " + driver);
                            if (driver.equalsIgnoreCase(driverName)) {
                                System.out.println("✓ Driver encontrado no Registry: " + driverName);
                                driverFound = true;
                            }
                        }
                    }
                }
            }

            // Lê erros se houver
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                // Ignora erros comuns do reg query
                if (!errorLine.contains("operação solicitada falhou")) {
                    System.out.println("Erro: " + errorLine);
                }
            }

            process.waitFor();

            if (driverFound) {
                return true;
            }

            System.out.println("✗ Driver não encontrado no Registry: " + driverName);
            return false;

        } catch (Exception e) {
            System.out.println("Erro ao verificar drivers instalados: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Retorna o nome do driver baseado no tipo de banco de dados
    private static String getDriverName(String databaseType) {
        switch (databaseType) {
            case "MSSQL":

                return "ODBC Driver 17 for SQL Server";
            case "POSTGRES":
                return "PostgreSQL ANSI";
            case "ORACLE":
                return "Oracle in OraDB21Home1";
            default:
                return "";
        }
    }

    // Verifica se um nome específico de driver existe no registry
    private static boolean isDriverNameInRegistry(String driverName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBCINST.INI\" /s | findstr /i \"" + driverName
                            + "\"");

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(driverName)) {
                    reader.close();
                    process.waitFor();
                    return true;
                }
            }

            reader.close();
            process.waitFor();
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    // Retorna o caminho do instalador baseado no tipo de banco de dados
    private static String getDriverPath(String databaseType) {
        switch (databaseType) {
            case "MSSQL":
                return SQLSERVER_DRIVER_64BIT_PATH;
            case "POSTGRES":
                return POSTGRESQL_DRIVER_64BIT_PATH;
            default:
                return "";
        }
    }

}
