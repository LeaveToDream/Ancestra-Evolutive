package org.klv.john.core;

import org.klv.john.realm.RealmServer;

import org.klv.john.common.World;

import org.klv.john.enums.EmulatorInfos;
import org.klv.john.game.GameServer;

public class Main {
	
	static {
		//reboot
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                closeServers();
            }
        });
	}
	
	public static void main(String[] args) {
		//création de la console
		Console console = new Console();
		Console.instance = console;
		
		//démarrage de l'émualteur
		console.writeln(EmulatorInfos.SOFT_NAME.toString());
		console.writeln("\n ~ Initialisation des variables : config.conf");
		Server.config.initialize();
		console.writeln(" ~ Connexion a la base de donnees : "+Server.config.getHost());
		
		if(!World.database.initializeConnection()) {
			console.writeln("> Identifiants de connexion invalides");
			console.writeln("> Redemarrage...");
			Main.closeServers();
			System.exit(0);
		}
		
		console.writeln(" > Creation du monde");
		int time = World.data.initialize();
		
		Server.config.setRunning(true);


		//gameserver
		GameServer gameServer = new GameServer();
		gameServer.initialize();
		gameServer.scheduleActions();
		Server.config.setGameServer(gameServer);
		console.writeln(" ~ Lancement du monde sur le port "+Server.config.getGamePort());

		//realmserver
		RealmServer realmServer = new RealmServer();
		realmServer.initialize();
		Server.config.setRealmServer(realmServer);
		console.writeln(" ~ Lancement du royaume sur le port "+Server.config.getRealmPort());

		
		//serveur lancé
		console.writeln(" > Lancement du serveur termine : "+ time +" ms");
		console.writeln(" > HELP pour la liste de commandes");
		
		//lancement de la console
		console.initialize();
	}

	public synchronized static void closeServers() {
		if(Server.config.isRunning()) {
			Console.instance.writeln(" <> Fermeture du jeu <>");
			
			Server.config.setRunning(false);
			Server.config.getRealmServer().close();
			Server.config.getGameServer().close();
			
			World.data.saveData(-1);
			World.database.getAccountData().updateState(false);
			World.database.close();
			
			Console.instance.writeln(" <> Redemmarage <>");
		}
	}
}
