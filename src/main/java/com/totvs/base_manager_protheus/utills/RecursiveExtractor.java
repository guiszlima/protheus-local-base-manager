package com.totvs.base_manager_protheus.utills;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class RecursiveExtractor {

    public static void RecursiveFolderExtractor(String caminhoZip, String pastaNoZip, String diretorioSaida)
            throws IOException {
        Path zipPath = Paths.get(caminhoZip);
        final Path targetRoot = Paths.get(diretorioSaida);

        // Configuração: "create=false" diz que queremos ler, não criar um zip novo
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        // O prefixo "jar:" é obrigatório para o Java entender que é um arquivo
        // compactado
        URI uri = URI.create("jar:" + zipPath.toUri());

        // try-with-resources: Garante que o ZIP feche sozinho ao terminar
        try (FileSystem zipFs = FileSystems.newFileSystem(uri, env)) {

            final Path pastaOrigem = zipFs.getPath(pastaNoZip);

            if (Files.notExists(pastaOrigem)) {
                throw new IOException("Pasta não encontrada no ZIP: " + pastaNoZip);
            }
            if (!Files.isDirectory(pastaOrigem)) {
                throw new IOException("O caminho não é uma pasta: " + pastaNoZip);
            }

            // O WalkFileTree percorre a árvore de diretórios automaticamente (Recursão)
            Files.walkFileTree(pastaOrigem, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Lógica para criar as pastas vazias no destino
                    Path caminhoRelativo = pastaOrigem.relativize(dir);
                    Path caminhoFinal = targetRoot.resolve(caminhoRelativo.toString());

                    if (Files.notExists(caminhoFinal)) {
                        Files.createDirectories(caminhoFinal);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Lógica para copiar os arquivos
                    Path caminhoRelativo = pastaOrigem.relativize(file);
                    Path caminhoFinal = targetRoot.resolve(caminhoRelativo.toString());

                    Files.copy(file, caminhoFinal, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        }

    }

    public static void RecursiveFileExtractor(String caminhoZip, String arquivoNoZip, String diretorioSaida)
            throws IOException {
        Path zipPath = Paths.get(caminhoZip);
        Path targetRoot = Paths.get(diretorioSaida);

        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        URI uri = URI.create("jar:" + zipPath.toUri());

        try (FileSystem zipFs = FileSystems.newFileSystem(uri, env)) {
            Path arquivoOrigem = zipFs.getPath(arquivoNoZip);

            if (Files.notExists(arquivoOrigem)) {
                throw new IOException("Arquivo não encontrado no ZIP: " + arquivoNoZip);
            }
            if (!Files.isRegularFile(arquivoOrigem)) {
                throw new IOException("O caminho não é um arquivo: " + arquivoNoZip);
            }

            // Copia o arquivo diretamente
            Path caminhoFinal = targetRoot.resolve(Paths.get(arquivoNoZip).getFileName());
            Files.copy(arquivoOrigem, caminhoFinal, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
