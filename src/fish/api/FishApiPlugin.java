package fish.api;

import arc.util.*;
import fish.api.http.Api;
import mindustry.mod.*;

import java.io.IOException;

public class FishApiPlugin extends Plugin {

	//called when game initializes
	@Override
	public void init(){
		try {
			Api.setupServer(8070);
		} catch(IOException e){
			Log.err("Failed to setup the API server");
			e.printStackTrace();
		}
	}

}
