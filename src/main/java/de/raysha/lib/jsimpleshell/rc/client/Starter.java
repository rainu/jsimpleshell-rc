package de.raysha.lib.jsimpleshell.rc.client;

public class Starter {

	public static void main(String[] args) throws Exception {
		if(args.length < 2 || args.length > 3){
			showHelp();
			System.exit(1);
		}

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String secret = "";

		if(args.length == 3) {
			secret = args[2];
		}else{
			//ASK
		}

		new ShellClientBuilder()
				.setTargetEndpoint(host, port)
				.setPassword(secret)
			.build()
		.start();
	}

	private static void showHelp() {
		System.out.println("(c) Rainu");
		System.out.println("Establish a secure connection to a JSimpleShell-Server.");
		System.out.println();
		System.out.println("java -jar jss-client.jar <host> <port> [<secret>]");
	}
}
