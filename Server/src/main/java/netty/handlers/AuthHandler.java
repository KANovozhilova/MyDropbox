package netty.handlers;

import commands.AuthCommand;
import commands.SignUpCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.AuthService;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static final boolean authOk = false;
    private final String serverDir;
    private final AuthService authService;
    private String userId;

    public AuthHandler(AuthService authService, String serverDir) {
        this.authService = authService;
        this.serverDir = serverDir;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.printf("Клиент подключился по адресу %s%n", ctx.channel().remoteAddress().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.printf("Клиент отключился по адресу %s%n", ctx.channel().remoteAddress().toString());
        authService.setIsLogin(userId, false);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof AuthCommand) {
            authProcessing(ctx, (AuthCommand) msg);
        } else if (msg instanceof SignUpCommand) {
            signUpProcessing(ctx, (SignUpCommand) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void signUpProcessing(ChannelHandlerContext ctx, SignUpCommand command) {
        String login = command.getLogin();
        if (authService.checkIsUsedUserId(login)) {
            String password = command.getPassword();
            authService.registerNewUser(login, password);
            command.setSignUp(true);
        } else {
            command.setMessage("Указанный логин уже используется. Введите новый логин.");
        }
        ctx.writeAndFlush(command);
    }

    private void authProcessing(ChannelHandlerContext ctx, AuthCommand command) {
        String login = command.getLogin();
        String password = command.getPassword();
        userId = authService.getUserIDByLoginAndPassword(login, password);
        if (userId != null) {
            if (checkAlreadyLogin(userId)) {
                command.setMessage("Клиент с таким логином уже авторизован. Попробуйте ввести другой логин.");
                ctx.writeAndFlush(command);
                return;
            }
            command.setAuthorized(true);
            command.setUserID(userId);
            authService.setIsLogin(userId, true);
            ctx.pipeline().addLast(new ClientHandler(authService, userId, serverDir));
            System.out.println("Добавлен обработчик для нового клиента.");
        } else {
            command.setMessage("Неверный логин или пароль!");
        }
        ctx.writeAndFlush(command);
    }

    private boolean checkAlreadyLogin(String userId) {
        return authService.isLogin(userId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        authService.setIsLogin(userId, false);
        cause.printStackTrace();
        ctx.close();
    }
}
