package netty.handlers;

import commands.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.AuthService;
import util.FileInfo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final String CLIENT_DIR_PREFIX = "client";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024 * 10;
    private final String serverDir;
    private final AuthService authService;
    private final String userId;
    private final String rootClientDirectory;
    private final String clientDir;
    private String currentClientDir;

    private FileOutputStream fileWriter;

    public ClientHandler(AuthService authService, String userId, String serverDir) {
        this.authService = authService;
        this.userId = userId;
        this.serverDir = serverDir;
        clientDir = CLIENT_DIR_PREFIX + userId;
        rootClientDirectory = serverDir + "/" + clientDir;
        createClientDirectory();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.printf("Клиент отключился по адресу %s%n", ctx.channel().remoteAddress().toString());
        authService.setIsLogin(userId, false);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FileRequestCommand) {
            sendFileToClient(ctx, (FileRequestCommand) msg);
        } else if (msg instanceof FileMessageCommand) {
            getFileFromClient((FileMessageCommand) msg);
        } else if (msg instanceof DeleteFileCommand) {
            deleteFile(ctx, (DeleteFileCommand) msg);
        } else if (msg instanceof RenameFileCommand) {
            renameFile(ctx, (RenameFileCommand) msg);
        } else if (msg instanceof GetFilesListCommand) {
            sendFilesListToClient(ctx, (GetFilesListCommand) msg);
        } else {
            System.err.printf("Получен неизвестный объект %s от клиента %s%n", msg.toString(), userId);
        }
    }

    private void sendFilesListToClient(ChannelHandlerContext ctx, GetFilesListCommand command) {
        System.out.println("Запрос на список файлов от клиента.");
        final FilesListCommand filesList;
        try {
            if (command.getCurrentPath() == null) {
                Path rootClientPath = Paths.get(clientDir);
                Path folder = Paths.get(rootClientDirectory);
                filesList = new FilesListCommand(Files.list(folder).map(FileInfo::new).collect(Collectors.toList()), rootClientPath);
                filesList.setRootServerPath(rootClientPath);
            } else {
                currentClientDir = command.getCurrentPath();
                Path currentClientPath = Paths.get(serverDir, currentClientDir);
                filesList = new FilesListCommand(Files.list(currentClientPath).map(FileInfo::new).collect(Collectors.toList()), Paths.get(currentClientDir));
            }
            ctx.writeAndFlush(filesList);
            System.out.println("Список файлов отправлен клиенту.");
        } catch (IOException e) {
            System.err.printf("Ошибка получения списка файлов клиента %s%n", userId);
            ctx.writeAndFlush(new ErrorCommand("Невозможно получить список файлов с сервера, попробуйте повторить позже."));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            System.out.printf("Соединение с клиентом %s по адресу %s%n", userId, ctx.channel().remoteAddress().toString());
        } else {
            System.err.printf("Ошибка обработчика клиента %s по адресу %s%n", userId, ctx.channel().remoteAddress().toString());
            cause.printStackTrace();
        }
        ctx.close();
    }

    private void deleteFile(ChannelHandlerContext ctx, DeleteFileCommand command) {
        System.out.printf("Получена команда на удаление файла %s от клиента %s%n", command.getFileName(), userId);
        try {
            Path deletePath = Paths.get(serverDir, command.getFileName());
            Files.delete(deletePath);
            ctx.writeAndFlush(new MessageCommand("Файл " + deletePath.getFileName() + " успешно удален с сервера."));
            System.out.println("Файл " + deletePath.getFileName() + " успешно удален с сервера.");
        } catch (IOException e) {
            System.err.println("Ошибка удаления файла с сервера " + command.getFileName());
            ctx.writeAndFlush(new ErrorCommand("Невозможно удалить файл с сервера, попробуйте повторить позже."));
            e.printStackTrace();
        }
    }

    private void renameFile(ChannelHandlerContext ctx, RenameFileCommand command) {
        System.out.printf("Получена команда на переименование файла %s на %s от клиента %s%n", command.getOldFileName(), command.getNewFileName(), userId);
        try {
            Path oldFile = Paths.get(serverDir, command.getOldFileName());
            Path newFile = Paths.get(serverDir, command.getNewFileName());
            Files.move(oldFile, newFile);
            String message = "Файл " + oldFile.getFileName() + " успешно переименован на " + newFile.getFileName();
            ctx.writeAndFlush(new MessageCommand(message));
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Ошибка переименования файла на сервере " + command.getOldFileName());
            ctx.writeAndFlush(new ErrorCommand("Невозможно переименовать файл на сервере, попробуйте повторить позже."));
            e.printStackTrace();
        }
    }

    private void getFileFromClient(FileMessageCommand command) {
        try {
            if (fileWriter == null) {
                System.out.printf("Начало получения файла %s от клиента %s%n", command.getFileName(), userId);
                Path destPath = Paths.get(serverDir, command.getDestPath(), command.getFileName());
                if (Files.exists(destPath)) {
                    System.out.printf("Файл с именем %s уже существует на сервере, выполняется его перезапись%n", command.getFileName());
                    Files.delete(destPath);
                    System.out.println("Файл перезаписан.");
                }
                createFileWriter(destPath);
                System.out.println("Абсолютный путь загрузки: " + destPath);
            }
            fileWriter.write(command.getData());
            if (command.getPartNumber() == command.getPartsOfFile()) {
                fileWriter.close();
                fileWriter = null;
                System.out.printf("Файл %s успешно загружен на сервер от клиента %s%n", command.getFileName(), userId);
            }
        } catch (IOException e) {
            System.err.printf("Ошибка записи файла %s на сервер от клиента %s%n", command.getFileName(), userId);
            e.printStackTrace();
        }
    }

    private void createFileWriter(Path destPath) {
        try {
            fileWriter = new FileOutputStream(destPath.toFile(), true);
        } catch (IOException e) {
            System.err.printf("Невозможно начать запись файла %s на диск%n", destPath);
            e.printStackTrace();
        }
    }

    private void sendFileToClient(ChannelHandlerContext ctx, FileRequestCommand command) {
        System.out.printf("Начало передачи файла %s клиенту %s%n", command.getFileToDownload(), userId);
        new Thread(() -> {
            final Path fileToSend = Paths.get(serverDir, command.getFileToDownload());
            final long fileSize = fileToSend.toFile().length();
            int partsOfFile = (int) fileSize / DEFAULT_BUFFER_SIZE;
            if (fileToSend.toFile().length() % DEFAULT_BUFFER_SIZE != 0) {
                partsOfFile++;
            }
            System.out.printf("Размер файла %d, количество пакетов %d при размере буфера %d%n", fileSize, partsOfFile, DEFAULT_BUFFER_SIZE);
            FileMessageCommand fileToClientCommand = new FileMessageCommand(
                    fileToSend.getFileName().toString(),
                    null,
                    fileSize,
                    partsOfFile,
                    0,
                    new byte[DEFAULT_BUFFER_SIZE]
            );
            try (FileInputStream fileReader = new FileInputStream(fileToSend.toFile())) {
                int readBytes;
                for (int i = 0; i < partsOfFile; i++) {
                    readBytes = fileReader.read(fileToClientCommand.getData());
                    fileToClientCommand.setPartNumber(i + 1);
                    if (readBytes < DEFAULT_BUFFER_SIZE) {
                        fileToClientCommand.setData(Arrays.copyOfRange(fileToClientCommand.getData(), 0, readBytes));
                    }
                    ctx.writeAndFlush(fileToClientCommand);
                    Thread.sleep(100);
                }
                System.out.printf("Файл %s успешно передан клиенту %s%n", command.getFileToDownload(), userId);
            } catch (IOException | InterruptedException e) {
                System.err.printf("Ошибка передачи файла %s клиенту %s%n", command.getFileToDownload(), userId);
                e.printStackTrace();
            }
        }).start();
    }

    private void createClientDirectory() {
        Path path = Paths.get(rootClientDirectory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                System.err.println("Ошибка создания папки клиента!");
                e.printStackTrace();
            }
        }
    }
}
