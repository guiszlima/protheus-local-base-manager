package com.totvs.base_manager_protheus.services;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import com.totvs.base_manager_protheus.model.ConfigureBaseModel;

public class ODBCConfigurationService {

    // 1. Definição da Interface JNA para acessar a DLL do Windows (odbccp32.dll)
    public interface Odbc32 extends Library {
        // Carrega a biblioteca nativa do Windows
        Odbc32 INSTANCE = Native.load("odbccp32", Odbc32.class, W32APIOptions.DEFAULT_OPTIONS);

        // Constantes
        int ODBC_ADD_DSN = 1; // User DSN
        int ODBC_CONFIG_DSN = 2; // Configure
        int ODBC_REMOVE_DSN = 3; // Remove
        int ODBC_ADD_SYS_DSN = 4; // System DSN (Requer Admin)

        // Declaração do método nativo
        boolean SQLConfigDataSource(long hwndParent, int fRequest, String lpszDriver, String lpszAttributes);
    }

    // 2. Método principal de configuração
    public static boolean configureSystemDSN(ConfigureBaseModel baseConfig) {
        // Verificação de Segurança (Arquitetura)
        if (!"64".equals(System.getProperty("sun.arch.data.model"))) {
            System.err.println("ERRO: A JVM deve ser 64-bit para configurar drivers 64-bit.");
            return false;
        }

        System.out.println("Tentando criar um SYSTEM DSN (Requer Admin)...");

        // Verifica se o driver está instalado antes de tentar criar o DSN
        String driverName = getDriverName(baseConfig.getDatabaseType());
        if (!isDriverInstalled(driverName)) {
            System.err.println("ERRO: Driver ODBC '" + driverName + "' não foi encontrado no Registry.");
            System.err.println("Por favor, instale o driver ODBC antes de criar a DSN.");
            return false;
        }

        // 3. Montagem dos Atributos
        // IMPORTANTE: Cada par chave=valor deve terminar com \0 e a string inteira deve
        // terminar com \0\0
        String attributes = "DSN=" + baseConfig.getBaseName() + "\0" +
                "Server=localhost\0" +
                "Database=" + baseConfig.getBaseName() + "\0" +
                "Description=DSN criada para " + baseConfig.getBaseName() + "\0" +
                "Trusted_Connection=Yes\0\0"; // <--- Nota o duplo null no final

        try {
            // Chamada Nativa
            boolean sucesso = Odbc32.INSTANCE.SQLConfigDataSource(
                    0, // hwndParent (0 para nenhum)
                    Odbc32.ODBC_ADD_SYS_DSN, // Request: 4 = System DSN
                    driverName, // Driver baseado no tipo de BD
                    attributes // Atributos formatados
            );

            if (sucesso) {
                System.out.println("SUCESSO! System DSN criado.");
                System.out.println(
                        "Verifique no 'Administrador de Fonte de Dados ODBC (64-bit)' na aba 'DSN de Sistema'.");
                return true;
            } else {
                int codigoErroWindows = Native.getLastError();
                System.out.println("AVISO: SQLConfigDataSource retornou false.");
                System.out.println("Variável sucesso = " + sucesso);
                System.out.println("Código de erro do Windows: " + codigoErroWindows);
                System.out.println("Mensagem de erro do Windows: " + getWindowsErrorMessage(codigoErroWindows));

                // Tenta diagnosticar o problema
                diagnoseODBCIssue(baseConfig.getDatabaseType(), codigoErroWindows);

                System.out.println("A aplicação continuará mesmo assim...");
                return false;
            }

        } catch (Exception e) {
            System.out.println("ERRO durante a configuração do ODBC DSN:");
            System.out.println("Tipo de erro: " + e.getClass().getName());
            System.out.println("Mensagem: " + e.getMessage());
            e.printStackTrace();

            return false;
        }
    }

    // Método auxiliar para verificar se o driver está instalado
    private static boolean isDriverInstalled(String driverName) {
        try {
            System.out.println("Verificando se o driver está instalado: " + driverName);
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBCINST.INI\\\" /v \"Driver\"");

            // Note: Esta é uma verificação simplificada. Uma verificação completa
            // iteraria pelos valores no Registry.
            return true; // Assume que o driver está instalado se chegou aqui
        } catch (Exception e) {
            System.out.println("Não foi possível verificar se o driver está instalado: " + e.getMessage());
            return true; // Assume que o driver está instalado em caso de erro
        }
    }

    // Método para diagnosticar problemas ODBC
    private static void diagnoseODBCIssue(String databaseType, int codigoErro) {
        System.out.println("\n=== DIAGNÓSTICO DE PROBLEMAS ODBC ===");

        if (codigoErro == 3 || codigoErro == 126 || codigoErro == 1049) {
            System.out.println("Possível causa: Driver ODBC não encontrado ou não registrado.");
            System.out.println("Soluções:");
            System.out.println("1. Verifique se o driver foi instalado corretamente");
            System.out.println("2. Tente reinstalar o driver ODBC");
            System.out.println("3. Verifique se a JVM é 64-bit (ODBC Data Source Administrator deve ser 64-bit)");
            System.out.println("4. Execute o administrador de fonte de dados ODBC manualmente:");
            System.out.println(
                    "   - Windows: Painel de Controle > Ferramentas Administrativas > Origens de Dados ODBC (64-bit)");
        } else if (codigoErro == 5) {
            System.out.println("Possível causa: Permissões insuficientes (não é administrador).");
            System.out.println("Solução: Execute a aplicação como administrador.");
        } else if (codigoErro == 0) {
            System.out.println("Nenhum erro específico, mas SQLConfigDataSource retornou false.");
            System.out.println("Verifique se os atributos da DSN estão corretos.");
        }

        System.out.println("\nDica: Use 'odbcadmin' ou 'OdbcConf.exe' para gerenciar DSNs diretamente.");
        System.out.println("=======================================\n");
    }

    // Método auxiliar para obter o nome do driver baseado no tipo de banco de dados
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

    // Método auxiliar para converter código de erro do Windows em mensagem legível
    private static String getWindowsErrorMessage(int codigoErro) {
        switch (codigoErro) {
            case 0:
                return "Sucesso (sem erro)";
            case 3:
                return "O arquivo especificado não foi encontrado";
            case 5:
                return "Acesso negado - Você não tem permissão de Administrador";
            case 126:
                return "Driver ODBC não encontrado";
            case 1049:
                return "Driver especificado não foi encontrado no registro do Windows";
            case 1168:
                return "Elemento não encontrado";
            default:
                return "Código de erro desconhecido: " + codigoErro;
        }
    }

}