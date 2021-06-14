package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	// ������ Ǯ ���(������ �ڿ����� �������̰� ������ ����ϱ� ���ؼ� threadPool ����� ���)
	public static ExecutorService threadPool;
	
	// ������ Ŭ���̾�Ʈ���� ���� �� �� �ֵ��� ����.
	public static Vector<Client> clients = new Vector<>();
	
	// ���� ���� ����
	ServerSocket serverSocket;
	
	// ������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	public void startServer(String IP, int port) {
		
		try {
			serverSocket = new ServerSocket();
			
			// Ư���� ip��ȣ�� port��ȣ�� Ư���� Ŭ���̾�Ʈ���� ������ ��ٸ��� ����
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			
			// ���� ������ �����ִ� ��찡 �ƴ϶��
			if(!serverSocket.isClosed()) {
				stopServer(); // ������ ����
			}
			
			return;
		}
		
		// Ŭ���̾�Ʈ�� ������ �� ���� ��� ��ٸ��� ������
		Runnable thread = new Runnable() {
			
			@Override
			public void run() {
				// ����ؼ� ���ο� Ŭ���̾�Ʈ�� ���� �� �� �ֵ��� ����
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						
						System.out.println(" [Ŭ���̾�Ʈ ����]"
											+ socket.getRemoteSocketAddress()
											+ " : "
											+ Thread.currentThread().getName());
					} catch (Exception e) {
						// ���� ���Ͽ� ������ ����Ŵ� ������ ���� ��Ű�� break���� Ȱ���ؼ� ���� ����
						
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		
		// ������ Ǯ�� �ʱ�ȭ
		threadPool = Executors.newCachedThreadPool();
		
		// Ŭ���̾�Ʈ�� ������ ���ϴ� �����带 �־���
		threadPool.submit(thread);
		
	}
	
	// ������ �۵��� ���������ִ� �޼ҵ�
	public void stopServer() {
		
		try {
			// ���� �۾����� ��� ���� �ݱ�
			Iterator<Client> iterator = clients.iterator();
			
			// �Ѹ� �Ѹ��� Ŭ���̾�Ʈ���� ����
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			
			// ���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			
			// ������Ǯ ����
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼���
	@Override
	public void start(Stage primaryStage) {
		// �ϳ��� ��ü ������ Ʋ�� ���� �� �ִ� �ϳ��� �г��� ����
		BorderPane root = new BorderPane();
		
		// ���� ��� 5
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));
		
		root.setCenter(textArea);
		
		// ��� ��ư�� ����ġ��� �����ϸ� ��
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		// �ڽ��� ���� ����
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				
				// �ڹ� fx���� ���� �ٷ� textArea�� ���� �ȵǰ� runLator�� ���� �Լ��� �̿��Ͽ� ��� gui��Ҹ� ����� �� �ֵ��� �ؾ���.
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			} else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		
		// ũ��
		Scene scene = new Scene(root, 500, 500);
		primaryStage.setTitle("[ ä�� ���� ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// ���α׷��� ���� �޼���
	public static void main(String[] args) {
		launch(args);
	}
}
