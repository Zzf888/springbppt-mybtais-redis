package com.example.controller;


import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Lettuce.RedisCli;
import com.example.mapper.AuthorMapper;
import com.example.model.Author;

@RestController
public class AuthorController {
	
	Logger logger = LoggerFactory.getLogger(AuthorController.class);
	
	@Autowired
	AuthorMapper authorMapper;
	
	@GetMapping("/author/{id}")
	public Author getOneAuthor(@PathVariable int id){
/*		Author a = new Author();
		a.setId(1);
		a.setName("aa");
		a.setPhone("123456");
		return a;*/
		logger.debug("-----aa-----");
		return authorMapper.findOneAuthor(id);
	}
	
	@GetMapping("/author")
	public List<Author> getAllAuthor() throws Exception{
		List<Author> author = null;
		author = readFromRedis();
		if(author.size()==0){
			logger.debug("----read from database");
			author = authorMapper.findAllAuthor();		
			writeToRedis(author);
		}
		return author;
	}
	
	private List<Author> readFromRedis() throws Exception {
		// TODO Auto-generated method stub
		RedisAsyncCommands<String, String> asyncCommands = RedisCli.connection.async();
		final List<Author> authors = new ArrayList<Author>();
		RedisFuture<List<String>> futureKeys = asyncCommands.keys("author*");
		List<String> keys = futureKeys.get();
		if(keys.size()==0)
			return authors;
		for(String key: keys){
			RedisFuture<Map<String, String>> futureMap = asyncCommands.hgetall(key);
			Map<String, String> map = futureMap.get(); //它阻塞和等待直到承诺的结果是可用状态
			Author author = new Author(); 
			author.setId( Integer.valueOf(map.get("id")) );
			author.setName( map.get("name") );
			author.setPhone( map.get("phone") );
			authors.add(author);
		}
		logger.debug("----------read from redis-------------------");
		return authors;
/*		RedisCommands<String,String> syncCommands = RedisCli.connection.sync();
		List<Author> authors = new ArrayList<Author>();
		List<String> keys = syncCommands.keys("author*");
		if(keys.size()==0)
			return authors;
		
		for(String key: keys){
			Map<String,String> map = syncCommands.hgetall(key);
			Author author = new Author();
			author.setId( Integer.valueOf(map.get("id")) );
			author.setName( map.get("name") );
			author.setPhone( map.get("phone") );
			authors.add(author);
		}
		logger.debug("----read from redis");
		return authors;*/
	}

	private void writeToRedis(List<Author> author) {
		// TODO Auto-generated method stub
		//RedisCommands<String, String> syncCommands = RedisCli.connection.sync();
		RedisAsyncCommands<String, String> asyncCommands = RedisCli.connection.async();
		for(Author auth: author){
			Map<String, String> map = new HashMap<String,String>();
			map.put("id", String.valueOf(auth.getId()));
			map.put("name", auth.getName());
			map.put("phone", auth.getPhone());
			//syncCommands.hmset("author:"+auth.getId(), map);
			asyncCommands.hmset("author:"+auth.getId(), map);
		}
	}

	@PostMapping(value="/authoradd",consumes="application/json")
	public int addAuthor(@RequestBody Author author){
		//System.out.println(author.getName());
		return authorMapper.insertAuthor(author);
	}
	
	@DeleteMapping("/delete/{id}")
	public int deleteAuthor(@PathVariable int id){
		return authorMapper.deleteAuthor(id);
	}
}
