package com.totvs.base_manager_protheus.utills;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitário para resolver caminhos de recursos de forma portável
 * e independente do usuário/computador.
 */
public class PathResolver {

    /**
     * Resolve o caminho do external-resources.zip de forma dinâmica.
     * Procura recursivamente na estrutura de pastas até encontrar a pasta
     * external-resources.
     * 
     * @return Caminho absoluto para o external-resources.zip
     */
    public static String getExternalResourcesPath() {
        String userDir = System.getProperty("user.dir");
        Path currentDir = Paths.get(userDir);

        // Tenta encontrar external-resources subindo na hierarquia de pastas
        return findExternalResourcesZip(currentDir);
    }

    /**
     * Procura recursivamente por external-resources.zip subindo na hierarquia.
     * 
     * @param startPath Caminho inicial para busca
     * @return Caminho absoluto para external-resources.zip ou null se não
     *         encontrado
     */
    private static String findExternalResourcesZip(Path startPath) {
        Path currentPath = startPath;

        // Sobe até 10 níveis na hierarquia procurando por external-resources.zip
        for (int i = 0; i < 10; i++) {
            Path externalResourcesZip = currentPath.resolve("external-resources.zip");

            if (Files.exists(externalResourcesZip)) {
                return externalResourcesZip.toString();
            }

            // Sobe um nível
            Path parentPath = currentPath.getParent();
            if (parentPath == null || parentPath.equals(currentPath)) {
                // Chegou na raiz do sistema de arquivos
                break;
            }
            currentPath = parentPath;
        }

        // Se não encontrou, retorna um caminho padrão (fallback)
        return startPath.resolve("external-resources.zip").toString();
    }
}
