package fish.api.http;

import arc.ApplicationListener;
import arc.Core;
import arc.util.Log;
import arc.util.Threads;
import arc.util.serialization.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.net.Administration.*;


public class Api {
	static HttpServer server;

	public static void respondString(HttpExchange t, int status, String data) throws IOException {
		var bytes = data.getBytes();
		t.getResponseHeaders().add("Content-Type", "application/json");
		t.sendResponseHeaders(status, bytes.length);
		OutputStream os = t.getResponseBody();
		os.write(bytes);
		os.close();
		Log.info("Done");
	}

	public static void respondBlank(HttpExchange t, int status) throws IOException {
		t.sendResponseHeaders(status, 0);
		t.getResponseBody().close();
	}

	public static void writeStatus(JsonWriter json) throws IOException {
		String description = !Config.desc.string().equals("off") ? Config.desc.string() : "";

		json.object()
			.name("name").value(Config.serverName.string())
			.name("description").value(description)
			.name("mapName").value(Vars.state.map.name())
			.name("playerCount").value(Groups.player.size())
			.name("limit").value(Vars.netServer.admins.getPlayerLimit())
			.name("wave").value(Vars.state.wave)
			.name("version").value(Version.build)
			.name("gamemode").value(Vars.state.rules.mode());
	}

	public static void setupServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/api", t -> {
			try {
				switch(t.getRequestMethod()){
					case "GET" -> {
						switch(t.getRequestURI().getPath()){
							case "/api/v1/status" -> {
								var buffer = new StringWriter();
								var writer = new JsonWriter(buffer);
								writeStatus(writer);
								writer.close();
								Log.info(buffer.toString());
								respondString(t, 200, buffer.toString());
							}
							default -> respondBlank(t, 404);
						}
					}
					default -> respondBlank(t, 405);
				}
			} catch(Exception e){
				e.printStackTrace();
				respondBlank(t, 500);
			}
		});
		server.setExecutor(Threads.executor("http", 1));
		server.start();
		Core.app.addListener(new ApplicationListener() {
			@Override
			public void dispose() {
				server.stop(0);
			}
		});
	}
}
