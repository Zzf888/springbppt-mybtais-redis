package com.example.Lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
/*import io.lettuce.core.api.sync.RedisCommands;

import java.util.concurrent.CompletableFuture;*/

public class RedisCli {

	public static RedisClient redisClient = RedisClient
			  .create("redis://123456@localhost:6379/");
	
	public static StatefulRedisConnection<String, String> connection
			 = redisClient.connect();
	
    
}
