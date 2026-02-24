package com.totvs.base_manager_protheus.services;

import lombok.Getter;

import java.io.Console;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import com.totvs.base_manager_protheus.model.ConfigureBaseModel;
import com.totvs.base_manager_protheus.utills.RecursiveExtractor;

@Getter
public class FileEstructureService {

    // Constantes
    private static final String ROOT_PATH = "C:\\totvs_protheus_manager\\automacao\\configs";
    private static final List<String> STANDARD_FOLDERS = Arrays.asList("dbaccess", "protheus", "protheus_data");
    private static final String EXTERNAL_RESOURCES_PATH = "C:\\base_manager\\external-resources.zip";
    private final String baseName;
    private final Path basePath;
    private final ConfigureBaseModel BaseData;

    // CONSTRUTOR: Recebe o nome da base (ex: "cliente_01")
    public FileEstructureService(ConfigureBaseModel BaseData) {
        this.baseName = BaseData.getBaseName();
        this.BaseData = BaseData;
        // Já deixamos o caminho base pronto: C:\totvs...\configs\cliente_01
        this.basePath = Paths.get(ROOT_PATH, baseName);
    }

    /**
     * Cria a estrutura principal e as pastas padrão.
     */
    public boolean createDirectoryStructure() {
        try {
            // 1. Cria o diretório base (e os pais se não existirem)
            if (Files.notExists(basePath)) {
                Files.createDirectories(basePath);
                System.out.println("Diretório base criado: " + basePath);
            }

            // 2. Cria as subpastas padrão (dbaccess, protheus, protheus_data)
            for (String folder : STANDARD_FOLDERS) {
                Path folderPath = basePath.resolve(folder); // resolve junta os caminhos
                if (Files.notExists(folderPath)) {
                    Files.createDirectories(folderPath);
                    System.out.println("Subpasta criada: " + folderPath);
                }
            }
            this.createProtheusStructure(this.BaseData.getVersionProtheus());
            this.createDbAccessStructure();
            this.createProtheusDataStructure(this.BaseData.getVersionProtheus());
            this.createODBCDriversStructure(this.BaseData.getDatabaseType());
            this.createDatabaseInstallerStructure(this.BaseData.getDatabaseType());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createProtheusStructure(String versionProtheus) {
        try {
            Path protheusRoot = basePath.resolve("protheus");

            // Lista de pastas para criar
            List<String> protheusSubFolders = Arrays.asList("apo", "appserver");

            for (String sub : protheusSubFolders) {
                // 1. Cria a pasta de destino (ex: .../protheus/appserver)
                Path targetPath = protheusRoot.resolve(sub);
                if (Files.notExists(targetPath)) {
                    Files.createDirectories(targetPath);

                }
                if (sub.equals("appserver")) {
                    String textTargetPath = targetPath.toString();
                    String serverAppText = "external-resources\\appserver";
                    RecursiveExtractor.RecursiveFolderExtractor(EXTERNAL_RESOURCES_PATH, serverAppText, textTargetPath);
                    System.out.println("Conteúdo copiado para: " + textTargetPath);
                }
                if (sub.equals("apo")) {
                    Path customRpoPath = targetPath.resolve("custom_rpo");
                    if (Files.notExists(customRpoPath)) {
                        Files.createDirectories(customRpoPath);
                    }

                    sub.equals("apo");
                    String textTargetPath = targetPath.toString();
                    String apoText = "external-resources\\rpo\\";
                    switch (versionProtheus) {
                        case "24.10":
                            apoText += "2410\\tttm120.rpo";
                            break;
                        case "25.10":
                            apoText += "2510\\tttm120.rpo";
                            break;

                    }
                    RecursiveExtractor.RecursiveFileExtractor(EXTERNAL_RESOURCES_PATH, apoText, textTargetPath);
                    System.out.println("Conteúdo copiado para: " + textTargetPath);
                }

            }
            return true;
        } catch (IOException e) { // | URISyntaxException se usar a Opção B
            e.printStackTrace();
            System.out.println("ERRO ao criar estrutura do Protheus: " + e.getMessage());
            return false;
        }
    }

    private void copyDirectoryRecursively(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (Files.notExists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Cria estrutura específica do DBAccess
     */
    public boolean createDbAccessStructure() {
        try {
            Path dbAccessRoot = basePath.resolve("dbaccess");
            // Se precisar criar algo como 'multi' ou 'monitor'
            String dbAcessText = "external-resources\\dbaccess";
            String textTargetPath = dbAccessRoot.toString();
            Files.createDirectories(dbAccessRoot.resolve("multi"));
            RecursiveExtractor.RecursiveFolderExtractor(EXTERNAL_RESOURCES_PATH, dbAcessText, textTargetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createProtheusDataStructure(String versionProtheus) {
        try {
            Path dataRoot = basePath.resolve("protheus_data");
            // Exemplo
            String datarootText = "external-resources\\protheus_data\\";
            String textTargetPath = dataRoot.toString();
            switch (versionProtheus) {
                case "24.10":
                    datarootText += "system_2410";
                    break;
                case "25.10":
                    datarootText += "system_2510";
                    break;

            }
            RecursiveExtractor.RecursiveFolderExtractor(EXTERNAL_RESOURCES_PATH, datarootText, textTargetPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
    }

    public boolean createODBCDriversStructure(String databaseType) {
        try {
            // Caminho raiz para drivers ODBC
            Path odbcDriversRoot = Paths.get("C:\\totvs_protheus_manager\\automacao");

            // Cria a pasta drivers_odbc caso não exista
            Path driversOdbcPath = odbcDriversRoot.resolve("odbc_drivers");
            if (Files.notExists(driversOdbcPath)) {
                Files.createDirectories(driversOdbcPath);
                System.out.println("Pasta drivers_odbc criada: " + driversOdbcPath);
            }

            // Switch case para verificar tipo de banco de dados
            switch (databaseType) {
                case "MSSQL":
                    // Cria a subpasta MSSQL dentro de drivers_odbc
                    Path mssqlPath = driversOdbcPath.resolve("MSSQL");
                    if (Files.notExists(mssqlPath)) {
                        Files.createDirectories(mssqlPath);
                        System.out.println("Pasta MSSQL criada: " + mssqlPath);
                    }
                    String mssqlDriversText = "external-resources\\odbc_drivers\\MSSQL\\msodbcsql.msi";
                    String mssqlTargetPath = mssqlPath.toString();
                    RecursiveExtractor.RecursiveFileExtractor(EXTERNAL_RESOURCES_PATH, mssqlDriversText,
                            mssqlTargetPath);
                    System.out.println("Drivers MSSQL copiados para: " + mssqlTargetPath);
                    break;

                case "POSTGRES":
                    // Cria a subpasta POSTGRES dentro de drivers_odbc
                    Path postgresPath = driversOdbcPath.resolve("POSTGRES");
                    if (Files.notExists(postgresPath)) {
                        Files.createDirectories(postgresPath);
                        System.out.println("Pasta POSTGRES criada: " + postgresPath);
                    }
                    String postgresDriversText = "external-resources\\odbc_drivers\\POSTGRES\\psqlodbc-setup.exe";
                    String postgresTargetPath = postgresPath.toString();
                    RecursiveExtractor.RecursiveFileExtractor(EXTERNAL_RESOURCES_PATH, postgresDriversText,
                            postgresTargetPath);
                    System.out.println("Drivers POSTGRES copiados para: " + postgresTargetPath);
                    break;

                case "ORACLE":
                    System.out.println("WIP");
                    break;

                default:
                    System.out.println("Banco de dados não reconhecido: " + databaseType);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cria a estrutura de instaladores de banco de dados e extrai apenas os
     * executáveis do ZIP
     * Estrutura:
     * C:\totvs_protheus_manager\automacao\database_installer\{POSTGRES|SQLSERVER}\
     */
    public boolean createDatabaseInstallerStructure(String databaseType) {
        try {
            // Caminho raiz para instaladores de banco de dados
            Path databaseInstallerRoot = Paths.get("C:\\totvs_protheus_manager\\automacao\\database_installer");

            // Cria a pasta database_installer caso não exista
            if (Files.notExists(databaseInstallerRoot)) {
                Files.createDirectories(databaseInstallerRoot);
                System.out.println("Pasta database_installer criada: " + databaseInstallerRoot);
            }

            // Switch case para o tipo de banco de dados
            switch (databaseType != null ? databaseType.toUpperCase() : "") {
                case "POSTGRES": {
                    Path postgresInstallerPath = databaseInstallerRoot.resolve("POSTGRES");
                    if (Files.notExists(postgresInstallerPath)) {
                        Files.createDirectories(postgresInstallerPath);
                        System.out.println("Pasta POSTGRES criada: " + postgresInstallerPath);
                    }

                    // Prefer copy from external-resources folder if available
                    Path externalExe = Paths.get(
                            "C:\\base_manager\\external-resources\\database\\POSTGRES\\postgresql-15.15-1-windows-x64.exe");
                    Path targetExe = postgresInstallerPath.resolve(externalExe.getFileName());
                    if (Files.exists(externalExe)) {
                        Files.copy(externalExe, targetExe, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Instalador POSTGRES já existe no caminho: " + targetExe);
                    } else {
                        // Fallback: extract from ZIP
                        String postgresExePath = "external-resources\\database\\POSTGRES\\postgresql-15.15-1-windows-x64.exe";
                        RecursiveExtractor.RecursiveFileExtractor(EXTERNAL_RESOURCES_PATH, postgresExePath,
                                postgresInstallerPath.toString());
                        System.out.println("Instalador POSTGRES extraído para: " + postgresInstallerPath);
                    }
                    break;
                }

                case "MSSQL":
                case "SQLSERVER": {
                    Path sqlserverInstallerPath = databaseInstallerRoot.resolve("SQLSERVER");
                    if (Files.notExists(sqlserverInstallerPath)) {
                        Files.createDirectories(sqlserverInstallerPath);
                        System.out.println("Pasta SQLSERVER criada: " + sqlserverInstallerPath);
                    }

                    Path externalExe = Paths
                            .get("C:\\base_manager\\external-resources\\database\\SQLSERVER\\SQL2022-SSEI-Dev.exe");
                    Path targetExe = sqlserverInstallerPath.resolve(externalExe.getFileName());
                    if (Files.exists(externalExe)) {
                        Files.copy(externalExe, targetExe, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Instalador SQLSERVER copiado para: " + targetExe);
                    } else {
                        String sqlserverExePath = "external-resources\\database\\SQLSERVER\\SQL2022-SSEI-Dev.exe";
                        RecursiveExtractor.RecursiveFileExtractor(EXTERNAL_RESOURCES_PATH, sqlserverExePath,
                                sqlserverInstallerPath.toString());
                        System.out.println("Instalador SQLSERVER extraído para: " + sqlserverInstallerPath);
                    }
                    break;
                }

                default:
                    System.out.println("Banco de dados não reconhecido para instalador: " + databaseType);
                    return false;
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ERRO ao criar estrutura de instaladores: " + e.getMessage());
            return false;
        }
    }

}