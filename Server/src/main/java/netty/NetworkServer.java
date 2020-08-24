package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import netty.handlers.AuthHandler;
import services.AuthService;
import services.DBConnector;
import services.SQLiteDBConnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NetworkServer {

    private static final String SERVER_DIR = "./server";
    private static DBConnector dbConnector;
    private static AuthService authService;
    private final int port;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new AuthHandler(authService, SERVER_DIR)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Сервер успешно запущен на порту: " + port + ".");
            dbConnector = new SQLiteDBConnector();
            dbConnector.start();
            authService = new AuthService(dbConnector);
            createMainDirectory();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            System.err.println("Ошибка в работе сервера!");
            e.printStackTrace();
        } finally {
            System.out.println("Сервер остановлен.");
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void createMainDirectory() throws IOException {
        if (!Files.exists(Paths.get(SERVER_DIR))) {
            Files.createDirectories(Paths.get(SERVER_DIR));
        }
    }
}
