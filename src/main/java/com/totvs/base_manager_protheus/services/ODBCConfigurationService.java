package com.totvs.base_manager_protheus.services;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

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
    public static boolean configureSystemDSN(String driverName) {
        // Verificação de Segurança (Arquitetura)
        if (!"64".equals(System.getProperty("sun.arch.data.model"))) {
            System.err.println("ERRO: A JVM deve ser 64-bit para configurar drivers 64-bit.");
            return false;
        }

        System.out.println("Tentando criar um SYSTEM DSN (Requer Admin)...");

        // 3. Montagem dos Atributos
        // IMPORTANTE: Cada par chave=valor deve terminar com \0 e a string inteira deve
        // terminar com \0\0
        String attributes = "DSN=MeuSistemaDSN\0" +
                "Server=localhost\0" +
                "Database=Master\0" +
                "Description=DSN de Sistema via Java\0" +
                "Trusted_Connection=Yes\0\0"; // <--- Nota o duplo null no final

        try {
            // Chamada Nativa
            boolean sucesso = Odbc32.INSTANCE.SQLConfigDataSource(
                    0, // hwndParent (0 para nenhum)
                    Odbc32.ODBC_ADD_SYS_DSN, // Request: 4 = System DSN
                    driverName, // Driver: "SQL Server"
                    attributes // Atributos formatados
            );

            if (sucesso) {
                System.out.println("SUCESSO! System DSN criado.");
                System.out.println(
                        "Verifique no 'Administrador de Fonte de Dados ODBC (64-bit)' na aba 'DSN de Sistema'.");
                return true;
            } else {
                System.err.println("FALHA ao criar DSN.");
                System.err.println("Possíveis causas: Falta de permissão de Administrador ou Driver incorreto.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

   

}