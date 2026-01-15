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

    // Método main para testar

}