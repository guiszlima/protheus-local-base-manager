package com.totvs.base_manager_protheus.utills;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramExecutor {

    /**
     * Executa um arquivo .exe localizado no caminho especificado (Sem argumentos).
     * 
     * @param caminhoArquivo O caminho absoluto ou relativo para o arquivo .exe
     */
    public static void executeProcess(String caminhoArquivo) {
        // Redireciona para o método principal passando null nos argumentos
        executeProcess(caminhoArquivo, null);
    }

    /**
     * Executa um arquivo .exe com argumentos (flags, parâmetros, etc).
     * 
     * @param caminhoArquivo O caminho absoluto ou relativo para o arquivo .exe
     * @param argumentos     Array de Strings com os parâmetros (ex: install flags)
     */
    public static void executeProcess(String caminhoArquivo, String[] argumentos) {
        // 1. Verificação básica se o arquivo existe
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.err.println("Erro: O arquivo não foi encontrado no caminho: " + caminhoArquivo);
            return;
        }

        try {
            // 2. Construção da lista de comandos
            // O ProcessBuilder exige uma List<String> onde o 1º elemento é o executável
            List<String> comandoCompleto = new ArrayList<>();
            comandoCompleto.add(caminhoArquivo);

            // Se houver argumentos, adiciona-os à lista
            if (argumentos != null && argumentos.length > 0) {
                comandoCompleto.addAll(Arrays.asList(argumentos));
            }

            // 3. Criação do ProcessBuilder com a lista completa
            ProcessBuilder builder = new ProcessBuilder(comandoCompleto);

            // 4. Redireciona a saída do .exe para o console do Java.
            builder.inheritIO();

            // 5. Inicia o processo
            System.out.println("Iniciando processo: " + caminhoArquivo);
            if (argumentos != null) {
                System.out.println("Argumentos: " + Arrays.toString(argumentos));
            }

            Process processo = builder.start();

            // 6. Espera o processo terminar
            int codigoSaida = processo.waitFor();

            System.out.println("O processo terminou com o código: " + codigoSaida);

        } catch (IOException e) {
            System.err.println("Erro de E/S ao tentar executar o arquivo: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("O processo foi interrompido inesperadamente.");
            Thread.currentThread().interrupt(); // Restaura o status de interrupção
        }
    }

    /**
     * Executa um arquivo MSI usando msiexec.exe com argumentos.
     * 
     * @param caminhoMSI     O caminho absoluto para o arquivo .msi
     * @param argumentos     Array de Strings com argumentos adicionais (ex:
     *                       ACCEPT_EULA=YES)
     * @param tipoInstalacao 1 = SQL Server ODBC (com parâmetros específicos), 0 =
     *                       MSI genérico
     */
    public static void executeMSI(String caminhoMSI, String[] argumentos, int tipoInstalacao) {
        File arquivo = new File(caminhoMSI);
        if (!arquivo.exists() || !arquivo.isFile()) {
            System.err.println("Erro: O arquivo MSI não foi encontrado no caminho: " + caminhoMSI);
            return;
        }

        try {
            // Constrói a lista de comandos para msiexec
            List<String> comandoCompleto = new ArrayList<>();
            comandoCompleto.add("msiexec.exe");
            comandoCompleto.add("/i");
            comandoCompleto.add(caminhoMSI);

            // Flags para evitar erro 1603 e permitir instalação sem interação
            comandoCompleto.add("/quiet");
            comandoCompleto.add("/norestart");

            // Se for tipo 1, adiciona parâmetros específicos para SQL Server ODBC
            if (tipoInstalacao == 1) {
                comandoCompleto.add("IACCEPTMSODBCSQLLICENSETERMS=YES");
                comandoCompleto.add("ACCEPT_EULA=YES");
            }

            // Adiciona argumentos adicionais se fornecidos
            if (argumentos != null && argumentos.length > 0) {
                comandoCompleto.addAll(Arrays.asList(argumentos));
            }

            ProcessBuilder pb = new ProcessBuilder(comandoCompleto);
            pb.inheritIO(); // Redireciona a saída para o console

            System.out.println("Iniciando processo: msiexec.exe");
            if (tipoInstalacao == 1) {
                System.out.println(
                        "Argumentos: /i \"" + caminhoMSI
                                + "\" /quiet /norestart IACCEPTMSODBCSQLDRIVERLICENSETERMS=YES");
            } else {
                System.out.println("Argumentos: /i \"" + caminhoMSI + "\" /quiet /norestart");
            }
            if (argumentos != null && argumentos.length > 0) {
                System.out.println("Argumentos adicionais: " + Arrays.toString(argumentos));
            }

            Process process = pb.start();
            int codigoSaida = process.waitFor();

            System.out.println("O processo MSI terminou com o código: " + codigoSaida);

            if (codigoSaida == 0) {
                System.out.println("✓ Instalação do MSI concluída com sucesso.");
            } else if (codigoSaida == 1602) {
                System.out.println("⚠ Instalação do MSI foi cancelada pelo usuário.");
            } else if (codigoSaida == 1603) {
                System.err.println("✗ ERRO: Falha fatal durante a instalação do MSI.");
                System.err.println("  Motivos possíveis:");
                System.err.println("  - Falta de permissões de administrador");
                System.err.println("  - Pré-requisitos não instalados (Visual C++ Redistributable)");
                System.err.println("  - Espaço em disco insuficiente");
                System.err.println("  - Versão anterior do driver conflitando");
                System.err.println("  - Arquivo MSI corrompido ou inválido");
                System.err.println("\nTente executar o MSI manualmente: msiexec.exe /i \"" + caminhoMSI + "\"");
            } else {
                System.out.println("⚠ Instalação do MSI retornou código: " + codigoSaida);
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar MSI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executa um arquivo MSI usando msiexec.exe com argumentos (tipo 0 = genérico).
     * 
     * @param caminhoMSI O caminho absoluto para o arquivo .msi
     * @param argumentos Array de Strings com argumentos adicionais
     */
    public static void executeMSI(String caminhoMSI, String[] argumentos) {
        executeMSI(caminhoMSI, argumentos, 0);
    }

    /**
     * Executa um arquivo MSI usando msiexec.exe (sem argumentos adicionais).
     * 
     * @param caminhoMSI O caminho absoluto para o arquivo .msi
     */
    public static void executeMSI(String caminhoMSI) {
        executeMSI(caminhoMSI, null, 0);
    }

    // Método main para testar

}